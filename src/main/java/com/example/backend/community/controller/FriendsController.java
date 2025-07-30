package com.example.backend.community.controller;

import com.example.backend.community.dto.*;
import com.example.backend.community.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FriendsController {

    @Autowired
    private FriendsService friendsService;

//    @GetMapping
//    public ResponseEntity<List<UserDto>> getFriends(Authentication authentication) {
//        return ResponseEntity.ok(friendsService.getFriends(authentication.getName()));
//    }
    @GetMapping
    public ResponseEntity<List<UserDto>> getFriends(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        return ResponseEntity.ok(friendsService.getFriends(authentication.getName()));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<UserDto>> getFriendSuggestions(Authentication authentication) {
        return ResponseEntity.ok(friendsService.getFriendSuggestions(authentication.getName()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<UserDto>> getFriendRequests(Authentication authentication) {
        return ResponseEntity.ok(friendsService.getFriendRequests(authentication.getName()));
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<Void> sendFriendRequest(@PathVariable String userId, Authentication authentication) {
        friendsService.sendFriendRequest(authentication.getName(), userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accept/{userId}")
    public ResponseEntity<Void> acceptFriendRequest(@PathVariable String userId, Authentication authentication) {
        friendsService.acceptFriendRequest(userId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reject/{userId}")
    public ResponseEntity<Void> rejectFriendRequest(@PathVariable String userId, Authentication authentication) {
        friendsService.rejectFriendRequest(userId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{userId}")
    public ResponseEntity<Void> removeFriend(@PathVariable String userId, Authentication authentication) {
        friendsService.removeFriend(authentication.getName(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<FriendshipStatusDto> getFriendshipStatus(@PathVariable String userId, Authentication authentication) {
        return ResponseEntity.ok(friendsService.getFriendshipStatus(authentication.getName(), userId));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityDto>> getFriendsActivities(Authentication authentication) {
        return ResponseEntity.ok(friendsService.getFriendsActivities(authentication.getName()));
    }

    @PostMapping("/dismiss-suggestion/{userId}")
    public ResponseEntity<Void> dismissSuggestion(@PathVariable String userId, Authentication authentication) {
        friendsService.dismissSuggestion(authentication.getName(), userId);
        return ResponseEntity.ok().build();
    }
}
