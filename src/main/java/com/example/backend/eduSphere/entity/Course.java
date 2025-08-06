package com.example.backend.eduSphere.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Course document in the MongoDB 'courses' collection.
 */
@Data
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String name;
    private String code;
    private String description;
    private String imageUrl;

    private String academicYear;
    private String semester;
    private Integer year; // e.g., 2023, 2025
    private Boolean selectable; // true for elective, false for mandatory

    @Field("lecturer_id")
    private String lecturerId;

    private List<YearlyEnrollment> enrollments = new ArrayList<>();

    private String department;
    private int credits;

}