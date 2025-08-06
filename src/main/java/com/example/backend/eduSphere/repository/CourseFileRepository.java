package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.CourseFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseFileRepository extends MongoRepository<CourseFile, String> {

    /**
     * Finds all file metadata records associated with a specific category ID.
     *
     * @param categoryId The ID of the file category.
     * @return A list of course files.
     */
    List<CourseFile> findByCategoryId(String categoryId);

    /**
     * Deletes all file metadata records associated with a specific category ID.
     * We'll use this when a category is deleted to clean up its associated file records.
     *
     * @param categoryId The ID of the file category to delete files from.
     */
    void deleteByCategoryId(String categoryId);
}