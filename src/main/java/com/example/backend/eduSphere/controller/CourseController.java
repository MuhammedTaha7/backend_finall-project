package com.example.backend.eduSphere.controller;

import com.example.backend.eduSphere.dto.request.EnrollmentRequest;
import com.example.backend.eduSphere.dto.request.UnenrollmentRequest;
import com.example.backend.eduSphere.dto.response.CourseDetailsResponse;
import com.example.backend.eduSphere.dto.response.CourseSummaryResponse;
import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
     * GET /api/courses : Gets a list of courses based on the logged-in user's role.
     */
    @GetMapping
    public List<CourseSummaryResponse> getAllCourses(@AuthenticationPrincipal UserDetails userDetails) {
        return courseService.findAllCoursesForUser(userDetails);
    }

    /**
     * GET /api/courses/{id} : Get a single course by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailsResponse> getCourseById(@PathVariable String id) {
        try {
            CourseDetailsResponse courseDetails = courseService.findCourseDetailsById(id);
            return ResponseEntity.ok(courseDetails);
        } catch (RuntimeException e) {
            // This will catch the "Course not found" exception from the service
            return ResponseEntity.notFound().build();
        }
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
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/courses/{courseId}/enrollments : Unenroll one or more students from a course.
     */
    @DeleteMapping("/{courseId}/enrollments")
    public ResponseEntity<Course> unenrollStudents(
            @PathVariable String courseId,
            @RequestBody UnenrollmentRequest request) {
        try {
            if (request == null || request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Course updatedCourse = courseService.unenrollStudents(courseId, request.getStudentIds());
            return ResponseEntity.ok(updatedCourse);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}