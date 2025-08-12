package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.File;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends MongoRepository<File, String> {

    // New simplified method to fetch a base list for service-side filtering
    @Query("{$or: ["
            + "{'accessType': 'public'},"
            + "{'accessType': 'students'},"
            + "{'accessType': 'lecturers'},"
            + "{'accessType': 'course'}"
            + "]}")
    List<File> findFilesForBaseFiltering();

    List<File> findAllByOrderByUploadDateDesc();
}