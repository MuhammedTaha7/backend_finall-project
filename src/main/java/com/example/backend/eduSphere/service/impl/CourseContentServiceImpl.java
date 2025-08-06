package com.example.backend.eduSphere.service.impl;

import com.example.backend.eduSphere.dto.request.CreateCategoryRequest;
import com.example.backend.eduSphere.entity.CourseFile;
import com.example.backend.eduSphere.entity.FileCategory;
import com.example.backend.eduSphere.repository.CourseFileRepository;
import com.example.backend.eduSphere.repository.FileCategoryRepository;
import com.example.backend.eduSphere.service.CourseContentService;
import com.example.backend.eduSphere.service.CourseFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class CourseContentServiceImpl implements CourseContentService {

    private final FileCategoryRepository fileCategoryRepository;
    private final CourseFileRepository courseFileRepository;
    private final CourseFileStorageService courseFileStorageService; // <-- Declare with the new name

    // Inject all dependencies via constructor
    public CourseContentServiceImpl(FileCategoryRepository fileCategoryRepository,
                                    CourseFileRepository courseFileRepository,
                                    CourseFileStorageService courseFileStorageService) { // <-- Inject the new specific service
        this.fileCategoryRepository = fileCategoryRepository;
        this.courseFileRepository = courseFileRepository;
        this.courseFileStorageService = courseFileStorageService; // <-- Assign the new specific service
    }

    // --- Category Implementations ---

    @Override
    public FileCategory createCategory(String courseId, int year, CreateCategoryRequest request) {
        FileCategory newCategory = new FileCategory();
        newCategory.setName(request.getName());
        newCategory.setDescription(request.getDescription());
        newCategory.setColor(request.getColor());
        newCategory.setCourseId(courseId);
        newCategory.setAcademicYear(year); // <-- Set the year securely
        return fileCategoryRepository.save(newCategory);
    }
    @Override
    public List<FileCategory> getCategoriesByCourse(String courseId, int year) {
        return fileCategoryRepository.findByCourseIdAndAcademicYear(courseId, year);
    }

    @Override
    @Transactional // Ensures operations are treated as a single unit
    public FileCategory updateCategory(String categoryId, FileCategory categoryDetails) {
        FileCategory existingCategory = fileCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());
        existingCategory.setColor(categoryDetails.getColor());

        return fileCategoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(String categoryId) {
        // 1. Find all files associated with the category
        List<CourseFile> filesToDelete = courseFileRepository.findByCategoryId(categoryId);

        // 2. Delete each physical file from storage
        for (CourseFile file : filesToDelete) {
            courseFileStorageService.deleteFile(file.getStoredFileName());
        }

        // 3. Delete all file metadata records from the database for this category
        courseFileRepository.deleteByCategoryId(categoryId);

        // 4. Finally, delete the category itself
        fileCategoryRepository.deleteById(categoryId);
    }

    // --- File Implementations ---

    @Override
    @Transactional
    public CourseFile storeFile(String categoryId, MultipartFile file) {
        // Ensure the category exists before storing a file in it
        if (!fileCategoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }

        // 1. Store the physical file and get its unique stored name
        String storedFileName = courseFileStorageService.storeFile(file);

        // 2. Create the file metadata object
        CourseFile courseFile = new CourseFile();
        courseFile.setFileName(file.getOriginalFilename());
        courseFile.setStoredFileName(storedFileName);
        courseFile.setFileType(file.getContentType());
        courseFile.setSize(file.getSize());
        courseFile.setCategoryId(categoryId);

        // 3. Save the metadata to the database
        return courseFileRepository.save(courseFile);
    }

    @Override
    @Transactional
    public void deleteFile(String fileId) {
        // 1. Find the file metadata to get the stored filename
        CourseFile file = courseFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        // 2. Delete the physical file from storage
        courseFileStorageService.deleteFile(file.getStoredFileName());

        // 3. Delete the file metadata record from the database
        courseFileRepository.deleteById(fileId);
    }

    @Override
    public Optional<CourseFile> getFileMetadata(String fileId) {
        return courseFileRepository.findById(fileId);
    }

    @Override
    public List<CourseFile> getFilesByCategory(String categoryId) {
        return courseFileRepository.findByCategoryId(categoryId);
    }
}