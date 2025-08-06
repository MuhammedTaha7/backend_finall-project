package com.example.backend.eduSphere.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data // Lombok annotation for getters, setters, etc.
@Document(collection = "file_categories") // Maps this class to the "file_categories" collection in MongoDB
public class FileCategory {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("color")
    private String color; // e.g., "#3b82f6"

    @Field("course_id")
    private String courseId;

    @Field("academic_year")
    private int academicYear;
}