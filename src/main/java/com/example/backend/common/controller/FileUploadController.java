package com.example.backend.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")  // Changed to /api/files for better organization
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FileUploadController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base.url}")
    private String baseUrl;

    // Support both eduSphere and community contexts
    @PostMapping("/upload/{context}/{type}")
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable String context,  // "community" or "edusphere"
            @PathVariable String type,     // "image" or "file"
            @RequestParam("file") MultipartFile file) {

        // Validate context
        if (!context.equals("community") && !context.equals("edusphere")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid context"));
        }

        // Validate type
        if (!type.equals("image") && !type.equals("file")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid upload type"));
        }

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            // Validate file type based on upload type
            String contentType = file.getContentType();
            if (type.equals("image")) {
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "File must be an image"));
                }
            } else if (type.equals("file")) {
                // Allow common document types
                if (contentType == null || (!contentType.equals("application/pdf") &&
                        !contentType.equals("application/msword") &&
                        !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") &&
                        !contentType.equals("application/vnd.ms-powerpoint") &&
                        !contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") &&
                        !contentType.equals("text/plain"))) {
                    return ResponseEntity.badRequest().body(Map.of("error", "File type not supported"));
                }
            }

            // Create context and type specific directory structure
            Path contextPath = Paths.get(uploadDir, context, type + "s"); // "images" or "files"
            if (!Files.exists(contextPath)) {
                Files.createDirectories(contextPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) :
                    (type.equals("image") ? ".jpg" : ".pdf");
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file in context/type directory
            Path filePath = contextPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the file URL with context and type
            String fileUrl = baseUrl + "/api/files/" + context + "/" + type + "s/" + uniqueFilename;
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("context", context);
            response.put("type", type);
            response.put("size", String.valueOf(file.getSize()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{context}/{type}s/{filename}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String context,
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, context, type + "s", filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Try to determine file content type
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                // For files, suggest download; for images, display inline
                String disposition = type.equals("image") ? "inline" : "attachment";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{context}/{type}s/{filename}")
    public ResponseEntity<Map<String, String>> deleteFile(
            @PathVariable String context,
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, context, type + "s", filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete file"));
        }
    }
}