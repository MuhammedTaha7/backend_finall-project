package com.example.backend.eduSphere.service.impl;

import com.example.backend.eduSphere.dto.request.EnrollmentRequest;
import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.entity.YearlyEnrollment;
import com.example.backend.eduSphere.repository.CourseRepository;
import com.example.backend.eduSphere.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> findCourseById(String id) {
        return courseRepository.findById(id);
    }

    @Override
    public Course createCourse(Course course) {
        // Ensure new courses start with an empty, non-null enrollments list
        if (course.getEnrollments() == null) {
            course.setEnrollments(new ArrayList<>());
        }
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(String id, Course courseDetails) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        // Update all metadata fields, but NOT the enrollments list
        if (courseDetails.getName() != null) existingCourse.setName(courseDetails.getName());
        if (courseDetails.getCode() != null) existingCourse.setCode(courseDetails.getCode());
        if (courseDetails.getDescription() != null) existingCourse.setDescription(courseDetails.getDescription());
        if (courseDetails.getImageUrl() != null) existingCourse.setImageUrl(courseDetails.getImageUrl());
        if (courseDetails.getAcademicYear() != null) existingCourse.setAcademicYear(courseDetails.getAcademicYear());
        if (courseDetails.getSemester() != null) existingCourse.setSemester(courseDetails.getSemester());
        if (courseDetails.getYear() != null) existingCourse.setYear(courseDetails.getYear());
        if (courseDetails.getSelectable() != null) existingCourse.setSelectable(courseDetails.getSelectable());
        if (courseDetails.getLecturerId() != null) existingCourse.setLecturerId(courseDetails.getLecturerId());
        if (courseDetails.getDepartment() != null) existingCourse.setDepartment(courseDetails.getDepartment());
        existingCourse.setCredits(courseDetails.getCredits());

        // The old "student_ids" logic is removed, as enrollments are now handled separately.

        return courseRepository.save(existingCourse);
    }

    @Override
    public void deleteCourse(String id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }


    @Override
    @Transactional
    public Course enrollStudent(String courseId, EnrollmentRequest enrollmentRequest) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        int academicYear = enrollmentRequest.getAcademicYear();
        String studentId = enrollmentRequest.getStudentId();

        // Safety check: Initialize enrollments list if it's null (for older data)
        if (course.getEnrollments() == null) {
            course.setEnrollments(new ArrayList<>());
        }

        // Find if an enrollment object for this year already exists
        Optional<YearlyEnrollment> existingEnrollmentOpt = course.getEnrollments().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .findFirst();

        YearlyEnrollment yearlyEnrollment;
        if (existingEnrollmentOpt.isPresent()) {
            // If it exists, use it
            yearlyEnrollment = existingEnrollmentOpt.get();
        } else {
            // If not, create a new one and add it to the course's list
            yearlyEnrollment = new YearlyEnrollment(academicYear);
            course.getEnrollments().add(yearlyEnrollment);
        }

        // Add the student to the list for the correct year, avoiding duplicates
        if (!yearlyEnrollment.getStudentIds().contains(studentId)) {
            yearlyEnrollment.getStudentIds().add(studentId);
        }

        return courseRepository.save(course);
    }
}