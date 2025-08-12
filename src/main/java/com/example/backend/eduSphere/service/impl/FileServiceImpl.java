package com.example.backend.eduSphere.service.impl;

import com.example.backend.common.controller.FileUploadController;
import com.example.backend.common.exceptions.ResourceNotFoundException;
import com.example.backend.eduSphere.dto.request.FileUploadRequest;
import com.example.backend.eduSphere.dto.response.FileResponse;
import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.entity.File;
import com.example.backend.eduSphere.repository.FileRepository;
import com.example.backend.eduSphere.repository.CourseRepository;
import com.example.backend.eduSphere.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileUploadController fileUploadController;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public FileResponse uploadFileWithMetadata(MultipartFile file, FileUploadRequest fileMetadata, String uploaderId, String uploaderName) {
        // ... (this method remains the same) ...
        ResponseEntity<Map<String, String>> uploadResponse = fileUploadController.uploadFile("edusphere", "file", file);

        if (!uploadResponse.getStatusCode().is2xxSuccessful() || uploadResponse.getBody() == null) {
            throw new RuntimeException("File upload failed via FileUploadController");
        }

        Map<String, String> body = uploadResponse.getBody();

        File fileEntity = new File();
        fileEntity.setName(fileMetadata.getName());
        fileEntity.setDescription(fileMetadata.getDescription());
        fileEntity.setCategory(fileMetadata.getCategory());

        String originalFilename = body.get("originalName");
        fileEntity.setType(originalFilename != null && originalFilename.lastIndexOf(".") != -1
                ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
                : "Unknown");
        fileEntity.setSize(body.get("size"));
        fileEntity.setFileUrl(body.get("url"));
        fileEntity.setFilename(body.get("filename"));

        fileEntity.setUploadedByUserId(uploaderId);
        fileEntity.setUploadedByUserName(uploaderName);
        fileEntity.setUploadDate(LocalDateTime.now());
        fileEntity.setDownloadCount(0);

        fileEntity.setAccessType(fileMetadata.getAccessType());
        fileEntity.setAccessBy(fileMetadata.getAccessBy());
        fileEntity.setAccessValue(fileMetadata.getAccessValue());

        File savedFile = fileRepository.save(fileEntity);

        return mapToResponse(savedFile);
    }

    @Override
    public List<FileResponse> getAccessibleFiles(String userId, String userRole, String userDepartment) {
        List<File> files;

        if ("1100".equals(userRole)) { // Admin
            files = fileRepository.findAllByOrderByUploadDateDesc();
        } else if ("1200".equals(userRole)) { // Lecturer
            Set<String> lecturerDepartments = courseRepository.findByLecturerId(userId).stream()
                    .map(Course::getDepartment)
                    .collect(Collectors.toSet());

            Set<String> lecturerCourseIds = courseRepository.findByLecturerId(userId).stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());

            files = fileRepository.findFilesForBaseFiltering().stream()
                    .filter(file -> {
                        String accessType = file.getAccessType();
                        String accessValue = file.getAccessValue();

                        if ("public".equals(accessType) || "lecturers".equals(accessType)) {
                            return true;
                        }

                        if ("students".equals(accessType) && "Department".equals(file.getAccessBy()) && lecturerDepartments.contains(accessValue)) {
                            return true;
                        }

                        if ("course".equals(accessType) && lecturerCourseIds.contains(accessValue)) {
                            return true;
                        }

                        return false;
                    })
                    .collect(Collectors.toList());
        } else { // Student
            Set<String> studentDepartments = courseRepository.findByEnrollments_StudentIds(userId).stream()
                    .map(Course::getDepartment)
                    .collect(Collectors.toSet());

            Set<String> studentCourseIds = courseRepository.findByEnrollments_StudentIds(userId).stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());

            files = fileRepository.findFilesForBaseFiltering().stream()
                    .filter(file -> {
                        String accessType = file.getAccessType();
                        String accessValue = file.getAccessValue();

                        if ("public".equals(accessType)) {
                            return true;
                        }

                        if ("students".equals(accessType) && "Department".equals(file.getAccessBy()) && studentDepartments.contains(accessValue)) {
                            return true;
                        }

                        if ("lecturers".equals(accessType) && "Department".equals(file.getAccessBy()) && studentDepartments.contains(accessValue)) {
                            return true;
                        }

                        if ("course".equals(accessType) && studentCourseIds.contains(accessValue)) {
                            return true;
                        }

                        return false;
                    })
                    .collect(Collectors.toList());
        }

        return files.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public FileResponse getFileMetadata(String fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + fileId));
        return mapToResponse(file);
    }

    @Override
    public void deleteFile(String fileId, String deleterId, String userRole) {
        File fileToDelete = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + fileId));

        if (!"1100".equals(userRole) && !fileToDelete.getUploadedByUserId().equals(deleterId)) {
            throw new AccessDeniedException("You do not have permission to delete this file.");
        }

        String context = "edusphere";
        String type = "file";
        fileUploadController.deleteFile(context, type, fileToDelete.getFilename());

        fileRepository.delete(fileToDelete);
    }

    private FileResponse mapToResponse(File file) {
        // ... (this method remains the same) ...
        return new FileResponse(
                file.getId(),
                file.getName(),
                file.getType(),
                file.getSize(),
                file.getCategory(),
                file.getDescription(),
                file.getFileUrl(),
                file.getUploadedByUserId(),
                file.getUploadedByUserName(),
                file.getUploadDate(),
                file.getDownloadCount(),
                file.getAccessType(),
                file.getAccessBy(),
                file.getAccessValue()
        );
    }
}