package com.example.backend.eduSphere.service;

import com.example.backend.eduSphere.dto.request.EnrollmentRequest;
import com.example.backend.eduSphere.dto.response.CourseDetailsResponse;
import com.example.backend.eduSphere.dto.response.CourseSummaryResponse;
import com.example.backend.eduSphere.entity.Course;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    List<CourseSummaryResponse> findAllCoursesForUser(UserDetails userDetails);
    Optional<Course> findCourseById(String id);
    Course createCourse(Course course);
    Course updateCourse(String id, Course courseDetails);
    void deleteCourse(String id);
    Course enrollStudent(String courseId, EnrollmentRequest enrollmentRequest);
    CourseDetailsResponse findCourseDetailsById(String id);
    Course unenrollStudents(String courseId, List<String> studentIds);

}
