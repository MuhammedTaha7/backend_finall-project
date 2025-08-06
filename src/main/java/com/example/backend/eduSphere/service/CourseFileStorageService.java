package com.example.backend.eduSphere.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface CourseFileStorageService {

    /**
     * Stores a file on the server.
     * @param file The file uploaded by the user.
     * @return The unique filename under which the file was stored.
     */
    String storeFile(MultipartFile file);

    /**
     * Loads a file from the server as a resource for downloading.
     * @param filename The unique name of the file to load.
     * @return The file as a downloadable Resource.
     */
    Resource loadFileAsResource(String filename);

    /**
     * Deletes a file from the server.
     * @param filename The unique name of the file to delete.
     */
    void deleteFile(String filename);
}