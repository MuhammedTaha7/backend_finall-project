package com.example.backend.eduSphere.service.impl;

import com.example.backend.eduSphere.dto.response.LoginResponse;
import com.example.backend.eduSphere.repository.UserRepository;
import com.example.backend.eduSphere.entity.UserEntity;
import com.example.backend.eduSphere.dto.request.LoginRequest;
import com.example.backend.eduSphere.dto.request.RegisterRequest;
import com.example.backend.common.exceptions.BadRequestException;
import com.example.backend.common.security.JwtUtil;
import com.example.backend.eduSphere.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void registerUser(@NonNull RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getEmail() == null || registerRequest.getPassword() == null) {
            throw new BadRequestException("Username, email, and password are required.");
        }
        if (userRepository.findByEmailOrUsername(registerRequest.getEmail(), registerRequest.getUsername()).isPresent()) {
            throw new BadRequestException("User already exists");
        }

        String encryptedPassword = passwordEncoder.encode(registerRequest.getPassword());

        UserEntity user = new UserEntity();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encryptedPassword);
        user.setRole("USER"); // Default role

        userRepository.save(user);
    }

    @Override
    public LoginResponse authenticateUser(@NonNull LoginRequest loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getEmail(), user.getRole());
    }

    @Override
    public String getUserIdByEmail(@NonNull String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return user.getId();
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserEntity getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserEntity> findUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * --- ADDED THIS METHOD ---
     * Implementation for finding users by a list of IDs.
     * It uses the findAllById method provided by MongoRepository.
     * @param userIds A list of user IDs.
     * @return A list of matching users.
     */
    @Override
    public List<UserEntity> findUsersByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }
}
