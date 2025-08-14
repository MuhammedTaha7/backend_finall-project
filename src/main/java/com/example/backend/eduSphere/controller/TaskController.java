package com.example.backend.eduSphere.controller;

import com.example.backend.eduSphere.dto.request.TaskCreateRequest;
import com.example.backend.eduSphere.dto.request.TaskUpdateRequest;
import com.example.backend.eduSphere.dto.response.TaskDetailResponse;
import com.example.backend.eduSphere.dto.response.TaskResponse;
import com.example.backend.eduSphere.entity.UserEntity;
import com.example.backend.eduSphere.repository.UserRepository;
import com.example.backend.eduSphere.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/tasks : Create a new task
     */
    @PostMapping
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("➕ === CREATING TASK ===");
            System.out.println("User: " + userDetails.getUsername());
            System.out.println("Request: " + request);

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            TaskResponse createdTask = taskService.createTask(request, currentUser.getId());
            System.out.println("✅ Task created successfully: " + createdTask.getId());

            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error creating task: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error creating task: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/tasks/{taskId} : Update an existing task
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔄 === UPDATING TASK ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("User: " + userDetails.getUsername());

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            TaskResponse updatedTask = taskService.updateTask(taskId, request, currentUser.getId());
            System.out.println("✅ Task updated successfully");

            return ResponseEntity.ok(updatedTask);

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error updating task: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error updating task: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/tasks/{taskId} : Delete a task
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🗑️ === DELETING TASK ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("User: " + userDetails.getUsername());

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            taskService.deleteTask(taskId, currentUser.getId());
            System.out.println("✅ Task deleted successfully");

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error deleting task: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error deleting task: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/{taskId} : Get a single task by ID
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<?> getTaskById(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📋 === FETCHING TASK ===");
            System.out.println("Task ID: " + taskId);
            System.out.println("User: " + userDetails.getUsername());

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            // Check access permission
            if (!taskService.canUserAccessTask(taskId, currentUser.getId(), currentUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied: You don't have permission to view this task"));
            }

            Optional<TaskResponse> task = taskService.getTaskById(taskId);
            if (task.isPresent()) {
                System.out.println("✅ Task found: " + task.get().getTitle());
                return ResponseEntity.ok(task.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error fetching task: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error fetching task: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/course/{courseId} : Get all tasks for a course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<?> getTasksByCourse(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📚 === FETCHING TASKS BY COURSE ===");
            System.out.println("Course ID: " + courseId);
            System.out.println("User: " + userDetails.getUsername());
            System.out.println("Filters - Status: " + status + ", Category: " + category + ", Priority: " + priority);

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            // Create sort object
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            List<TaskResponse> tasks;

            // Apply filters based on parameters
            if (status != null) {
                tasks = taskService.getTasksByStatus(courseId, status);
            } else if (category != null) {
                tasks = taskService.getTasksByCategory(courseId, category);
            } else if (priority != null) {
                tasks = taskService.getTasksByPriority(courseId, priority);
            } else {
                // For students, only return available tasks
                if ("1300".equals(currentUser.getRole())) {
                    tasks = taskService.getAvailableTasksForStudents(courseId);
                } else {
                    tasks = taskService.getTasksByCourse(courseId);
                }
            }

            System.out.println("✅ Found " + tasks.size() + " tasks");
            return ResponseEntity.ok(tasks);

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error fetching tasks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error fetching tasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/course/{courseId}/overdue : Get overdue tasks for a course
     */
    @GetMapping("/course/{courseId}/overdue")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOverdueTasks(
            @PathVariable String courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("⏰ === FETCHING OVERDUE TASKS ===");
            System.out.println("Course ID: " + courseId);

            List<TaskResponse> overdueTasks = taskService.getOverdueTasks(courseId);
            System.out.println("✅ Found " + overdueTasks.size() + " overdue tasks");

            return ResponseEntity.ok(overdueTasks);

        } catch (Exception e) {
            System.err.println("❌ Error fetching overdue tasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/course/{courseId}/upcoming : Get upcoming tasks for a course
     */
    @GetMapping("/course/{courseId}/upcoming")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<?> getUpcomingTasks(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "7") int daysAhead,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📅 === FETCHING UPCOMING TASKS ===");
            System.out.println("Course ID: " + courseId);
            System.out.println("Days ahead: " + daysAhead);

            List<TaskResponse> upcomingTasks = taskService.getUpcomingTasks(courseId, daysAhead);
            System.out.println("✅ Found " + upcomingTasks.size() + " upcoming tasks");

            return ResponseEntity.ok(upcomingTasks);

        } catch (Exception e) {
            System.err.println("❌ Error fetching upcoming tasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/course/{courseId}/grading : Get tasks needing grading
     */
    @GetMapping("/course/{courseId}/grading")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTasksNeedingGrading(
            @PathVariable String courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📝 === FETCHING TASKS NEEDING GRADING ===");
            System.out.println("Course ID: " + courseId);

            List<TaskResponse> tasksNeedingGrading = taskService.getTasksNeedingGrading(courseId);
            System.out.println("✅ Found " + tasksNeedingGrading.size() + " tasks needing grading");

            return ResponseEntity.ok(tasksNeedingGrading);

        } catch (Exception e) {
            System.err.println("❌ Error fetching tasks needing grading: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/search : Search tasks
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<?> searchTasks(
            @RequestParam String courseId,
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔍 === SEARCHING TASKS ===");
            System.out.println("Course ID: " + courseId);
            System.out.println("Query: " + query);

            if (query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Search query cannot be empty"));
            }

            List<TaskResponse> searchResults = taskService.searchTasks(courseId, query.trim());
            System.out.println("✅ Found " + searchResults.size() + " tasks matching query");

            return ResponseEntity.ok(searchResults);

        } catch (Exception e) {
            System.err.println("❌ Error searching tasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/instructor/{instructorId} : Get tasks by instructor
     */
    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTasksByInstructor(
            @PathVariable String instructorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("👨‍🏫 === FETCHING TASKS BY INSTRUCTOR ===");
            System.out.println("Instructor ID: " + instructorId);

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            // Lecturers can only see their own tasks, admins can see any
            if ("1200".equals(currentUser.getRole()) && !currentUser.getId().equals(instructorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied: You can only view your own tasks"));
            }

            List<TaskResponse> tasks = taskService.getTasksByInstructor(instructorId);
            System.out.println("✅ Found " + tasks.size() + " tasks for instructor");

            return ResponseEntity.ok(tasks);

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error fetching instructor tasks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error fetching instructor tasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/tasks/batch/visibility : Batch update task visibility
     */
    @PostMapping("/batch/visibility")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTasksVisibility(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("👁️ === BATCH UPDATING TASK VISIBILITY ===");

            String courseId = (String) request.get("courseId");
            @SuppressWarnings("unchecked")
            List<String> taskIds = (List<String>) request.get("taskIds");
            Boolean visible = (Boolean) request.get("visible");

            if (courseId == null || taskIds == null || visible == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: courseId, taskIds, visible"));
            }

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            taskService.updateTaskVisibility(courseId, taskIds, visible, currentUser.getId());
            System.out.println("✅ Updated visibility for " + taskIds.size() + " tasks");

            return ResponseEntity.ok(Map.of(
                    "message", "Task visibility updated successfully",
                    "updatedCount", taskIds.size()
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error updating task visibility: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error updating task visibility: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/tasks/batch/status : Batch update task status
     */
    @PostMapping("/batch/status")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTasksStatus(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📊 === BATCH UPDATING TASK STATUS ===");

            String courseId = (String) request.get("courseId");
            @SuppressWarnings("unchecked")
            List<String> taskIds = (List<String>) request.get("taskIds");
            String status = (String) request.get("status");

            if (courseId == null || taskIds == null || status == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: courseId, taskIds, status"));
            }

            // Validate status
            List<String> validStatuses = List.of("active", "draft", "archived", "completed");
            if (!validStatuses.contains(status)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status. Must be one of: " + validStatuses));
            }

            // Get current user
            UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            taskService.updateTaskStatus(courseId, taskIds, status, currentUser.getId());
            System.out.println("✅ Updated status for " + taskIds.size() + " tasks to: " + status);

            return ResponseEntity.ok(Map.of(
                    "message", "Task status updated successfully",
                    "updatedCount", taskIds.size(),
                    "newStatus", status
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error updating task status: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error updating task status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/tasks/{taskId}/recalculate-stats : Recalculate task statistics
     */
    @PostMapping("/{taskId}/recalculate-stats")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<?> recalculateTaskStatistics(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📊 === RECALCULATING TASK STATISTICS ===");
            System.out.println("Task ID: " + taskId);

            taskService.recalculateTaskStatistics(taskId);
            System.out.println("✅ Task statistics recalculated successfully");

            return ResponseEntity.ok(Map.of(
                    "message", "Task statistics recalculated successfully",
                    "taskId", taskId
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error recalculating statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error recalculating statistics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}