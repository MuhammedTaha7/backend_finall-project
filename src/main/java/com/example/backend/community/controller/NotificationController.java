package com.example.backend.community.controller;

import com.example.backend.community.entity.Notification;
import com.example.backend.community.service.NotificationService;
import com.example.backend.community.dto.NotificationDto;
import com.example.backend.community.dto.request.SendNotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(
            @RequestBody SendNotificationRequest request,
            Authentication authentication) {
        String senderId = authentication.getName();
        notificationService.sendNotification(request, senderId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String notificationId,
            Authentication authentication) {
        String userId = authentication.getName();
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}