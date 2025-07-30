package com.example.backend.community.controller;

import com.example.backend.community.dto.UserDto;
import com.example.backend.community.service.FriendsService;
import com.example.backend.community.service.StoriesService;
import com.example.backend.community.dto.StoryDto;
import com.example.backend.community.dto.response.StoriesFeedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/community/stories")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class StoriesController {

    @Autowired
    private StoriesService storiesService;

    @Autowired
    private FriendsService friendsService;

    @GetMapping("/feed")
    public ResponseEntity<StoriesFeedResponse> getStoriesFeed(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) List<String> friendIds,
            Authentication authentication) {

        // Add null check for authentication
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String currentUserId = authentication.getName(); // This is now userId
        StoriesFeedResponse feed = storiesService.getStoriesFeed(currentUserId);
        return ResponseEntity.ok(feed);
    }

    @PostMapping
    public ResponseEntity<StoryDto> createStory(
            @RequestParam String name,
            @RequestParam String profilePic,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) MultipartFile img,
            Authentication authentication) {

        // Add null check for authentication
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String currentUserId = authentication.getName(); // This is now userId

        try {
            StoryDto story = storiesService.createStory(currentUserId, name, profilePic, text, img);
            return ResponseEntity.ok(story);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StoryDto[]> getUserStories(@PathVariable String userId) {
        StoryDto[] stories = storiesService.getUserStories(userId);
        return ResponseEntity.ok(stories);
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(@PathVariable String storyId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String userId = authentication.getName(); // This is now userId
        storiesService.deleteStory(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getFriends(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        return ResponseEntity.ok(friendsService.getFriends(authentication.getName()));
    }

    @GetMapping("/debug/auth")
    public ResponseEntity<String> debugAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("Authentication is null");
        }
        return ResponseEntity.ok("Authenticated user: " + authentication.getName() +
                ", Principal: " + authentication.getPrincipal());
    }
}