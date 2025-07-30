package com.example.backend.eduSphere.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String name; // Display name
    private String role; // "1300", "1200", "1100" (student, lecturer, admin)
    private String profilePic;
    private String coverPic; // For profile cover image
    private String title; // Job title or academic title
    private String university;
    private String bio; // User bio/summary
    private String location;
    private String website;
    private String phoneNumber;

    // Social media links
    private Map<String, String> socialLinks; // {"facebook": "url", "linkedin": "url", ...}

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}