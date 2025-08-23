package com.example.backend.extension.controller;

import com.example.backend.extension.dto.response.ExtensionDashboardResponse;
import com.example.backend.extension.service.ExtensionService;
import com.example.backend.eduSphere.entity.UserEntity;
import com.example.backend.eduSphere.entity.Meeting;
import com.example.backend.eduSphere.repository.UserRepository;
import com.example.backend.eduSphere.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/extension")
public class ExtensionController {

    private final ExtensionService extensionService;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;

    public ExtensionController(ExtensionService extensionService, UserRepository userRepository, MeetingRepository meetingRepository) {
        this.extensionService = extensionService;
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
    }

    /**
     * GET /api/extension/dashboard : Get dashboard data for extension
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(@RequestParam String email) {
        try {
            System.out.println("📚 === EXTENSION DASHBOARD REQUEST ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get dashboard data
            ExtensionDashboardResponse dashboardData = extensionService.getDashboardData(user.getId(), user.getRole());

            System.out.println("✅ Dashboard data retrieved for: " + user.getName());
            System.out.println("📊 Total items: " + dashboardData.getItems().size());

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
            System.out.println("🎥 === EXTENSION MEETING DETAILS REQUEST ===");
            System.out.println("Meeting ID: " + id + ", Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Meeting ID is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Find meeting by ID
            Optional<Meeting> meetingOpt = meetingRepository.findById(id.trim());
            if (meetingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Meeting not found with ID: " + id));
            }

            Meeting meeting = meetingOpt.get();

            // Check if user can access this meeting
            boolean canAccess = extensionService.canUserAccessCourse(user.getId(), user.getRole(), meeting.getCourseId()) ||
                    user.getId().equals(meeting.getCreatedBy()) ||
                    user.getId().equals(meeting.getLecturerId()) ||
                    (meeting.getParticipants() != null && meeting.getParticipants().contains(user.getId()));

            if (!canAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to access this meeting"));
            }

            // Ensure invitation link is generated if not present
            if (meeting.getInvitationLink() == null || meeting.getInvitationLink().trim().isEmpty()) {
                // Generate invitation link with appropriate base URL
                String baseUrl = "http://localhost:3000"; // Update this to your actual frontend URL
                meeting.generateInvitationLink(baseUrl);

                // Save the updated meeting with invitation link
                try {
                    meetingRepository.save(meeting);
                    System.out.println("✅ Generated and saved invitation link for meeting: " + meeting.getId());
                } catch (Exception e) {
                    System.err.println("⚠️ Could not save invitation link: " + e.getMessage());
                }
            }

            // Return meeting details with invitation link
            // Fixed version using HashMap to handle null values properly
            Map<String, Object> meetingDetails = new HashMap<>();
            meetingDetails.put("id", meeting.getId() != null ? meeting.getId() : "");
            meetingDetails.put("title", meeting.getTitle() != null ? meeting.getTitle() : "");
            meetingDetails.put("description", meeting.getDescription() != null ? meeting.getDescription() : "");
            meetingDetails.put("roomId", meeting.getRoomId() != null ? meeting.getRoomId() : "");
            meetingDetails.put("invitationLink", meeting.getInvitationLink() != null ? meeting.getInvitationLink() : "");
            meetingDetails.put("status", meeting.getStatus() != null ? meeting.getStatus() : "scheduled");
            meetingDetails.put("courseId", meeting.getCourseId() != null ? meeting.getCourseId() : "");
            meetingDetails.put("courseName", meeting.getCourseName() != null ? meeting.getCourseName() : "");
            meetingDetails.put("courseCode", meeting.getCourseCode() != null ? meeting.getCourseCode() : "");
            meetingDetails.put("datetime", meeting.getDatetime() != null ? meeting.getDatetime().toString() : "");
            meetingDetails.put("duration", meeting.getDuration() != null ? meeting.getDuration() : 60);
            meetingDetails.put("location", meeting.getType() != null ? meeting.getType() : "Online Meeting");

            System.out.println("✅ Meeting details retrieved for: " + meeting.getTitle());
            System.out.println("🔗 Invitation link: " + meeting.getInvitationLink());

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
            System.out.println("✅ === EXTENSION TASKS REQUEST ===");
            System.out.println("Email: " + email + ", Status: " + status + ", Priority: " + priority);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get filtered tasks
            var tasks = extensionService.getTasks(user.getId(), user.getRole(), status, priority, type, limit);

            System.out.println("✅ Tasks retrieved: " + tasks.size());
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
            System.out.println("📢 === EXTENSION ANNOUNCEMENTS REQUEST ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get announcements
            var announcements = extensionService.getAnnouncements(user.getId(), user.getRole(), limit);

            System.out.println("✅ Announcements retrieved: " + announcements.size());
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
            System.out.println("📊 === EXTENSION STATS REQUEST ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get stats
            var stats = extensionService.getUserStats(user.getId(), user.getRole());

            System.out.println("✅ Stats retrieved for: " + user.getName());
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
            System.out.println("🚨 === EXTENSION URGENT ITEMS REQUEST ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email parameter is required"));
            }

            // Find user by email
            UserEntity user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get urgent items
            var urgentItems = extensionService.getUrgentItems(user.getId(), user.getRole());

            System.out.println("✅ Urgent items retrieved: " + urgentItems.size());
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