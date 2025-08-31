package com.example.extension.controller;

import com.example.extension.dto.response.ExtensionDashboardResponse;
import com.example.extension.service.ExtensionService;
import com.example.common.entity.UserEntity;
import com.example.common.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/extension")
public class ExtensionController {

    private final ExtensionService extensionService;
    private final UserRepository userRepository;

    // We no longer need the MeetingRepository injected directly
    // private final MeetingRepository meetingRepository;

    public ExtensionController(ExtensionService extensionService, UserRepository userRepository) {
        this.extensionService = extensionService;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/extension/dashboard : Get dashboard data for extension
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(@RequestParam String email) {
        try {


            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get dashboard data
            ExtensionDashboardResponse dashboardData = extensionService.getDashboardData(user.getId(), user.getRole());

            return ResponseEntity.ok(dashboardData);

        } catch (RuntimeException e) {
            System.err.println("❌ Error getting dashboard data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting dashboard data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/extension/meeting/{id} : Get specific meeting details for joining
     */
    @GetMapping("/meeting/{id}")
    public ResponseEntity<?> getMeetingDetails(@PathVariable String id, @RequestParam String email) {
        try {

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Meeting ID is required"));
            }

            // The business logic is now handled by the service, which will use an API client
            Map<String, Object> meetingDetails = extensionService.getMeetingDetails(id, email);
            return ResponseEntity.ok(meetingDetails);

        } catch (RuntimeException e) {
            System.err.println("❌ Error getting meeting details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting meeting details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/extension/tasks : Get tasks for extension
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(
            @RequestParam String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit) {
        try {

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get filtered tasks
            var tasks = extensionService.getTasks(user.getId(), user.getRole(), status, priority, type, limit);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tasks", tasks,
                    "count", tasks.size()
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Error getting tasks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting tasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/extension/announcements : Get announcements for extension
     */
    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements(
            @RequestParam String email,
            @RequestParam(defaultValue = "10") int limit) {
        try {

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get announcements
            var announcements = extensionService.getAnnouncements(user.getId(), user.getRole(), limit);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "announcements", announcements,
                    "count", announcements.size()
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Error getting announcements: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting announcements: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/extension/stats : Get statistics for extension
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam String email) {
        try {

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get stats
            var stats = extensionService.getUserStats(user.getId(), user.getRole());
            return ResponseEntity.ok(stats);

        } catch (RuntimeException e) {
            System.err.println("❌ Error getting stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/extension/urgent : Get urgent items for extension
     */
    @GetMapping("/urgent")
    public ResponseEntity<?> getUrgentItems(@RequestParam String email) {
        try {

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get urgent items
            var urgentItems = extensionService.getUrgentItems(user.getId(), user.getRole());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "urgentItems", urgentItems,
                    "count", urgentItems.size()
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Error getting urgent items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting urgent items: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}