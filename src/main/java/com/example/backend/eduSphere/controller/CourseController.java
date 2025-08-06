package com.example.backend.eduSphere.controller;

import com.example.backend.eduSphere.dto.request.EnrollmentRequest; // --- ADD THIS IMPORT ---
import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Courses.
 * This class exposes a set of API endpoints for performing CRUD operations on courses.
 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses : Get all courses.
     */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.findAllCourses();
    }

    /**
     * GET /api/courses/{id} : Get a single course by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable String id) {
        return courseService.findCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/courses : Create a new course.
     */
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    /**
     * PUT /api/courses/{id} : Update an existing course.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable String id, @RequestBody Course courseDetails) {
        try {
            Course updatedCourse = courseService.updateCourse(id, courseDetails);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/courses/{id} : Delete a course by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * --- UPDATED ENDPOINT ---
     * POST /{courseId}/enroll : Enroll a student into a course for a specific year.
     * The request body now contains all enrollment details.
     */
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<Course> enrollStudent(@PathVariable String courseId, @RequestBody EnrollmentRequest enrollmentRequest) {
        try {
            Course updatedCourse = courseService.enrollStudent(courseId, enrollmentRequest);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            // Catches exceptions like "Course not found"
            return ResponseEntity.notFound().build();
        }
    }
}