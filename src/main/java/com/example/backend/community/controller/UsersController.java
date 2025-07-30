package com.example.backend.community.controller;

import com.example.backend.community.service.UsersService;
import com.example.backend.community.dto.UserDto;
import com.example.backend.community.dto.request.UpdateProfileRequest;
import com.example.backend.community.dto.request.ReportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable String userId) {
        UserDto user = usersService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserDto user = usersService.updateProfile(request, userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String q) {
        List<UserDto> users = usersService.searchUsers(q);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            Authentication authentication) {
        String userId = authentication.getName();
        Map<String, String> result = usersService.uploadAvatar(file, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/report/{userId}")
    public ResponseEntity<Void> reportUser(
            @PathVariable String userId,
            @RequestBody ReportRequest request,
            Authentication authentication) {
        String reporterId = authentication.getName();
        usersService.reportUser(userId, request, reporterId);
        return ResponseEntity.ok().build();
    }
}