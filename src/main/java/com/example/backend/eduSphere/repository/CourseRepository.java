package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Course documents.
 * Extends MongoRepository to provide standard CRUD operations.
 */
@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByDepartment(String department);
    
    // Finds all courses where the student's ID is in an enrollment list.
    List<Course> findByEnrollments_StudentIds(String studentId);
}
