package com.example.backend.eduSphere.service;

import com.example.backend.eduSphere.dto.request.LoginRequest;
import com.example.backend.eduSphere.dto.request.RegisterRequest;
import com.example.backend.eduSphere.dto.response.LoginResponse;
import com.example.backend.eduSphere.entity.UserEntity;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    LoginResponse authenticateUser(@NonNull LoginRequest loginRequest);
    void registerUser(@NonNull RegisterRequest registerRequest);

    String getUserIdByEmail(@NonNull String email);

    UserEntity getUserByEmail(String email);

    UserEntity getUserById(String userId);
}