package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.FileCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileCategoryRepository extends MongoRepository<FileCategory, String> {
    List<FileCategory> findByCourseIdAndAcademicYear(String courseId, int academicYear);
}