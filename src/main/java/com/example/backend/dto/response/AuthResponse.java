package com.example.backend.dto.response;

public class AuthResponse {
    private String username;
    private String email;
    private String role;

    public AuthResponse(String username,String email, String role) {
        this.username=username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

