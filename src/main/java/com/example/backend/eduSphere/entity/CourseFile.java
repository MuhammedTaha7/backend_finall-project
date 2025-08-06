package com.example.backend.eduSphere.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "course_files") // Maps this class to the "course_files" collection
public class CourseFile {

    @Id
    private String id;

    @Field("file_name")
    private String fileName; // The original name of the file, e.g., "lecture1.pdf"

    @Field("stored_file_name")
    private String storedFileName; // The unique name on the server, e.g., "uuid-lecture1.pdf"

    @Field("file_type")
    private String fileType; // The MIME type, e.g., "application/pdf"

    @Field("size")
    private long size; // File size in bytes

    @CreatedDate // Automatically sets the date and time when the document is created
    @Field("upload_date")
    private LocalDateTime uploadDate;

    @Field("category_id")
    private String categoryId; // Crucial link to the FileCategory this file belongs to
}