package com.example.backend.eduSphere.dto.response;

import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.entity.YearlyEnrollment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CourseDetailsResponse {
    // Fields from Course entity
    private String id;
    private String name;
    private String code;
    private String description;
    private String imageUrl;
    private String academicYear;
    private String semester;
    private Integer year;
    private Boolean selectable;
    private String department;
    private int credits;
    private String language;
    private Integer progress;
    private String prerequisites;
    private String finalExam;
    private List<YearlyEnrollment> enrollments;

    // --- Dynamically added data ---
    private String lecturerName;
    private List<AssignmentResponse> assignments;

    // Helper method to convert a Course entity to this DTO
    public static CourseDetailsResponse fromEntity(Course course) {
        CourseDetailsResponse dto = new CourseDetailsResponse();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode());
        dto.setDescription(course.getDescription());
        dto.setImageUrl(course.getImageUrl());
        dto.setAcademicYear(course.getAcademicYear());
        dto.setSemester(course.getSemester());
        dto.setYear(course.getYear());
        dto.setSelectable(course.getSelectable());
        dto.setDepartment(course.getDepartment());
        dto.setCredits(course.getCredits());
        dto.setLanguage(course.getLanguage());
        dto.setProgress(course.getProgress());
        dto.setPrerequisites(course.getPrerequisites());
        dto.setFinalExam(course.getFinalExam());
        dto.setEnrollments(course.getEnrollments());
        return dto;
    }
}