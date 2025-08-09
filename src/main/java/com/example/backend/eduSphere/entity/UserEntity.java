package com.example.backend.eduSphere.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@Document(collection = "users")
public class UserEntity implements UserDetails { // --- 1. IMPLEMENTS UserDetails ---

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
    private Map<String, String> socialLinks;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // --- 2. UserDetails METHODS ---
    // These methods are required by Spring Security.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName;
        switch (this.role) {
            case "1100":
                roleName = "ROLE_ADMIN";
                break;
            case "1200":
                roleName = "ROLE_LECTURER";
                break;
            case "1300":
                roleName = "ROLE_STUDENT";
                break;
            default:
                roleName = "ROLE_USER"; // A default fallback role
                break;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getUsername() {
        // Spring Security's "username" is the unique identifier used for login.
        // In your case, it seems to be the username field.
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or add logic for this
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or add logic for this
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or add logic for this
    }

    @Override
    public boolean isEnabled() {
        return true; // Or add logic for this
    }
}