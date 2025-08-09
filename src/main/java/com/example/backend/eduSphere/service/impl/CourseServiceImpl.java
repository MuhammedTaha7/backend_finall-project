package com.example.backend.eduSphere.service.impl;

import com.example.backend.eduSphere.dto.request.EnrollmentRequest;
import com.example.backend.eduSphere.dto.response.AssignmentResponse;
import com.example.backend.eduSphere.dto.response.CourseDetailsResponse;
import com.example.backend.eduSphere.dto.response.CourseSummaryResponse;
import com.example.backend.eduSphere.entity.Assignment;
import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.entity.UserEntity;
import com.example.backend.eduSphere.entity.YearlyEnrollment;
import com.example.backend.eduSphere.repository.AssignmentRepository;
import com.example.backend.eduSphere.repository.CourseRepository;
import com.example.backend.eduSphere.repository.UserRepository;
import com.example.backend.eduSphere.service.CourseService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserRepository userRepository, AssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<CourseSummaryResponse> findAllCoursesForUser(UserDetails userDetails) {
        UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

        String role = currentUser.getRole();
        List<Course> courses;

        if ("1100".equals(role)) { // Admin
            courses = courseRepository.findAll();
        } else if ("1200".equals(role)) { // Lecturer
            courses = courseRepository.findByLecturerId(currentUser.getId());
        } else if ("1300".equals(role)) { // Student
            courses = courseRepository.findByEnrollments_StudentIds(currentUser.getId());
        } else {
            courses = Collections.emptyList();
        }

        return mapCoursesToSummaryResponses(courses);
    }

    private List<CourseSummaryResponse> mapCoursesToSummaryResponses(List<Course> courses) {
        // To avoid N+1 queries, get all unique lecturer IDs first
        List<String> lecturerIds = courses.stream()
                .map(Course::getLecturerId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // Fetch all required lecturers in a single query
        List<UserEntity> lecturers = userRepository.findAllById(lecturerIds);

        // Map courses to DTOs
        return courses.stream().map(course -> {
            CourseSummaryResponse dto = new CourseSummaryResponse();
            dto.setId(course.getId());
            dto.setName(course.getName());
            dto.setCode(course.getCode());
            dto.setImageUrl(course.getImageUrl());
            dto.setDepartment(course.getDepartment());
            dto.setCredits(course.getCredits());
            dto.setEnrollments(course.getEnrollments());

            // Find the lecturer's name from our pre-fetched list
            lecturers.stream()
                    .filter(l -> l.getId().equals(course.getLecturerId()))
                    .findFirst()
                    .ifPresent(l -> dto.setLecturerName(l.getName()));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<Course> findCourseById(String id) {
        return courseRepository.findById(id);
    }

    @Override
    public Course createCourse(Course course) {
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
        if (courseDetails.getLanguage() != null) existingCourse.setLanguage(courseDetails.getLanguage());
        if (courseDetails.getProgress() != null) existingCourse.setProgress(courseDetails.getProgress());
        if (courseDetails.getPrerequisites() != null) existingCourse.setPrerequisites(courseDetails.getPrerequisites());
        if (courseDetails.getFinalExam() != null) existingCourse.setFinalExam(courseDetails.getFinalExam());
        existingCourse.setCredits(courseDetails.getCredits());

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

    @Override
    @Transactional
    public Course unenrollStudents(String courseId, List<String> studentIdsToUnenroll) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        if (course.getEnrollments() != null) {
            // Iterate over each yearly enrollment list
            course.getEnrollments().forEach(yearlyEnrollment -> {
                // Remove all matching student IDs from the list for that year
                yearlyEnrollment.getStudentIds().removeAll(studentIdsToUnenroll);
            });
        }

        // Save the updated course with the students removed
        return courseRepository.save(course);
    }

    @Override
    public CourseDetailsResponse findCourseDetailsById(String id) {
        // Fetch the main course object
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        // Convert the course entity to our base DTO
        CourseDetailsResponse responseDTO = CourseDetailsResponse.fromEntity(course);

        // Fetch the lecturer's name (if lecturerId exists)
        if (course.getLecturerId() != null) {
            userRepository.findById(course.getLecturerId()).ifPresent(lecturer -> {
                responseDTO.setLecturerName(lecturer.getName());
            });
        }

        // Fetch all assignments for this course
        List<Assignment> assignments = assignmentRepository.findByCourse(id);
        List<AssignmentResponse> assignmentDTOs = assignments.stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());
        responseDTO.setAssignments(assignmentDTOs);

        return responseDTO;
    }
}