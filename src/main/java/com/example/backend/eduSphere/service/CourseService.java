package com.example.backend.eduSphere.service;

import com.example.backend.eduSphere.dto.request.EnrollmentRequest;
import com.example.backend.eduSphere.entity.Course;
import java.util.List;
import java.util.Optional;

public interface CourseService {

    List<Course> findAllCourses();
    Optional<Course> findCourseById(String id);
    Course createCourse(Course course);
    Course updateCourse(String id, Course courseDetails);
    void deleteCourse(String id);

    Course enrollStudent(String courseId, EnrollmentRequest enrollmentRequest);}
