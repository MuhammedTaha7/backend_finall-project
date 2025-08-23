package com.example.backend.extension.service.impl;

import com.example.backend.extension.dto.response.ExtensionDashboardResponse;
import com.example.backend.extension.dto.response.ExtensionItemResponse;
import com.example.backend.extension.dto.response.ExtensionStatsResponse;
import com.example.backend.extension.service.ExtensionService;
import com.example.backend.eduSphere.entity.Course;
import com.example.backend.eduSphere.entity.Task;
import com.example.backend.eduSphere.entity.TaskSubmission;
import com.example.backend.eduSphere.entity.Meeting;
import com.example.backend.eduSphere.repository.CourseRepository;
import com.example.backend.eduSphere.repository.TaskRepository;
import com.example.backend.eduSphere.repository.TaskSubmissionRepository;
import com.example.backend.eduSphere.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtensionServiceImpl implements ExtensionService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final MeetingRepository meetingRepository;

    public ExtensionServiceImpl(TaskRepository taskRepository,
                                CourseRepository courseRepository,
                                TaskSubmissionRepository taskSubmissionRepository,
                                MeetingRepository meetingRepository) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public ExtensionDashboardResponse getDashboardData(String userId, String userRole) {
        System.out.println("📚 === GETTING EXTENSION DASHBOARD DATA ===");
        System.out.println("User ID: " + userId + ", Role: " + userRole);

        try {
            // Get user's courses based on role
            List<Course> userCourses = getUserCourses(userId, userRole);
            List<String> courseIds = userCourses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());

            System.out.println("📋 User has access to " + courseIds.size() + " courses");

            if (courseIds.isEmpty()) {
                return new ExtensionDashboardResponse(new ArrayList<>(), new ExtensionStatsResponse());
            }

            List<ExtensionItemResponse> items = new ArrayList<>();

            // Get all tasks from user's courses
            List<Task> allTasks = new ArrayList<>();
            for (String courseId : courseIds) {
                List<Task> courseTasks = taskRepository.findByCourseIdOrderByDueDateAsc(courseId);

                // Filter based on user role and visibility
                List<Task> filteredTasks = courseTasks.stream()
                        .filter(task -> canUserSeeTask(task, userId, userRole))
                        .collect(Collectors.toList());

                allTasks.addAll(filteredTasks);
            }

            System.out.println("✅ Found " + allTasks.size() + " accessible tasks");

            // Convert tasks to extension items
            List<ExtensionItemResponse> taskItems = allTasks.stream()
                    .map(task -> convertTaskToExtensionItem(task, userId, userRole))
                    .collect(Collectors.toList());

            items.addAll(taskItems);

            // Get all meetings from user's courses using your repository method
            List<Meeting> allMeetings = new ArrayList<>();
            if (!courseIds.isEmpty()) {
                List<Meeting> courseMeetings = meetingRepository.findByCourseIdIn(courseIds);

                // Filter based on user role and meeting access
                List<Meeting> filteredMeetings = courseMeetings.stream()
                        .filter(meeting -> canUserSeeMeeting(meeting, userId, userRole))
                        .collect(Collectors.toList());

                allMeetings.addAll(filteredMeetings);
            }

            System.out.println("✅ Found " + allMeetings.size() + " accessible meetings");

            // Convert meetings to extension items and ensure invitation links are generated
            List<ExtensionItemResponse> meetingItems = allMeetings.stream()
                    .map(meeting -> {
                        // Ensure invitation link is generated
                        if (meeting.getInvitationLink() == null || meeting.getInvitationLink().trim().isEmpty()) {
                            String baseUrl = "http://localhost:3000"; // Update this to your actual frontend URL
                            meeting.generateInvitationLink(baseUrl);

                            // Save the updated meeting with invitation link
                            try {
                                meetingRepository.save(meeting);
                                System.out.println("✅ Generated invitation link for meeting: " + meeting.getId());
                            } catch (Exception e) {
                                System.err.println("⚠️ Could not save invitation link for meeting " + meeting.getId() + ": " + e.getMessage());
                            }
                        }

                        return convertMeetingToExtensionItem(meeting, userId, userRole);
                    })
                    .collect(Collectors.toList());

            items.addAll(meetingItems);

            // Sort all items by priority and due date
            items = items.stream()
                    .sorted(this::compareItemsByPriority)
                    .collect(Collectors.toList());

            // Add mock announcements
            items.addAll(generateMockAnnouncements(userCourses));

            // Calculate statistics
            ExtensionStatsResponse stats = calculateStats(items, userId, userRole);

            ExtensionDashboardResponse response = new ExtensionDashboardResponse(items, stats);

            System.out.println("✅ Dashboard data prepared with " + items.size() + " total items");
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error getting dashboard data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get dashboard data: " + e.getMessage());
        }
    }

    @Override
    public List<ExtensionItemResponse> getTasks(String userId, String userRole, String status,
                                                String priority, String type, int limit) {
        try {
            System.out.println("✅ === GETTING EXTENSION TASKS ===");
            System.out.println("Filters - Status: " + status + ", Priority: " + priority + ", Type: " + type);

            // Get user's courses
            List<Course> userCourses = getUserCourses(userId, userRole);
            List<String> courseIds = userCourses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());

            if (courseIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<ExtensionItemResponse> items = new ArrayList<>();

            // Get tasks from all courses
            List<Task> allTasks = new ArrayList<>();
            for (String courseId : courseIds) {
                List<Task> courseTasks = taskRepository.findByCourseIdOrderByDueDateAsc(courseId);
                allTasks.addAll(courseTasks.stream()
                        .filter(task -> canUserSeeTask(task, userId, userRole))
                        .collect(Collectors.toList()));
            }

            // Convert tasks to extension items
            List<ExtensionItemResponse> taskItems = allTasks.stream()
                    .map(task -> convertTaskToExtensionItem(task, userId, userRole))
                    .filter(item -> "task".equals(item.getType()))
                    .collect(Collectors.toList());

            items.addAll(taskItems);

            // Get meetings from all courses if type allows
            if (type == null || "all".equals(type) || "meeting".equals(type)) {
                List<Meeting> allMeetings = new ArrayList<>();
                if (!courseIds.isEmpty()) {
                    List<Meeting> courseMeetings = meetingRepository.findByCourseIdIn(courseIds);
                    allMeetings.addAll(courseMeetings.stream()
                            .filter(meeting -> canUserSeeMeeting(meeting, userId, userRole))
                            .collect(Collectors.toList()));
                }

                // Convert meetings to extension items and ensure invitation links
                List<ExtensionItemResponse> meetingItems = allMeetings.stream()
                        .map(meeting -> {
                            // Ensure invitation link is generated
                            if (meeting.getInvitationLink() == null || meeting.getInvitationLink().trim().isEmpty()) {
                                String baseUrl = "http://localhost:3000"; // Update this to your actual frontend URL
                                meeting.generateInvitationLink(baseUrl);

                                try {
                                    meetingRepository.save(meeting);
                                } catch (Exception e) {
                                    System.err.println("⚠️ Could not save invitation link: " + e.getMessage());
                                }
                            }

                            return convertMeetingToExtensionItem(meeting, userId, userRole);
                        })
                        .filter(item -> "meeting".equals(item.getType()))
                        .collect(Collectors.toList());

                items.addAll(meetingItems);
            }

            // Apply filters and limit
            List<ExtensionItemResponse> filteredItems = items.stream()
                    .filter(item -> matchesStatusFilter(item, status))
                    .filter(item -> matchesPriorityFilter(item, priority))
                    .sorted(this::compareItemsByPriority)
                    .limit(limit)
                    .collect(Collectors.toList());

            System.out.println("✅ Filtered items: " + filteredItems.size());
            return filteredItems;

        } catch (Exception e) {
            System.err.println("❌ Error getting tasks: " + e.getMessage());
            throw new RuntimeException("Failed to get tasks: " + e.getMessage());
        }
    }

    @Override
    public List<ExtensionItemResponse> getAnnouncements(String userId, String userRole, int limit) {
        try {
            System.out.println("📢 === GETTING EXTENSION ANNOUNCEMENTS ===");

            // Get user's courses
            List<Course> userCourses = getUserCourses(userId, userRole);

            // Generate mock announcements (implement real announcements later)
            List<ExtensionItemResponse> announcements = generateMockAnnouncements(userCourses)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            System.out.println("✅ Generated " + announcements.size() + " announcements");
            return announcements;

        } catch (Exception e) {
            System.err.println("❌ Error getting announcements: " + e.getMessage());
            throw new RuntimeException("Failed to get announcements: " + e.getMessage());
        }
    }

    @Override
    public ExtensionStatsResponse getUserStats(String userId, String userRole) {
        try {
            System.out.println("📊 === CALCULATING EXTENSION STATS ===");

            // Get dashboard data to calculate stats
            ExtensionDashboardResponse dashboardData = getDashboardData(userId, userRole);
            return dashboardData.getStats();

        } catch (Exception e) {
            System.err.println("❌ Error getting stats: " + e.getMessage());
            throw new RuntimeException("Failed to get stats: " + e.getMessage());
        }
    }

    @Override
    public List<ExtensionItemResponse> getUrgentItems(String userId, String userRole) {
        try {
            System.out.println("🚨 === GETTING URGENT ITEMS ===");

            // Get all items and filter for urgent ones
            ExtensionDashboardResponse dashboardData = getDashboardData(userId, userRole);
            List<ExtensionItemResponse> urgentItems = dashboardData.getItems().stream()
                    .filter(item -> "urgent".equals(item.getPriority()))
                    .sorted(this::compareItemsByDueDate)
                    .collect(Collectors.toList());

            System.out.println("✅ Found " + urgentItems.size() + " urgent items");
            return urgentItems;

        } catch (Exception e) {
            System.err.println("❌ Error getting urgent items: " + e.getMessage());
            throw new RuntimeException("Failed to get urgent items: " + e.getMessage());
        }
    }

    @Override
    public boolean canUserAccessCourse(String userId, String userRole, String courseId) {
        try {
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) return false;

            Course course = courseOpt.get();

            // Admin can access any course
            if ("1100".equals(userRole)) return true;

            // Lecturer can access their own courses
            if ("1200".equals(userRole)) {
                return userId.equals(course.getLecturerId());
            }

            // Students can access enrolled courses
            if ("1300".equals(userRole)) {
                return course.getEnrollments().stream()
                        .anyMatch(enrollment -> enrollment.getStudentIds().contains(userId));
            }

            return false;
        } catch (Exception e) {
            System.err.println("❌ Error checking course access: " + e.getMessage());
            return false;
        }
    }

    // Helper methods

    private List<Course> getUserCourses(String userId, String userRole) {
        try {
            List<Course> allCourses = courseRepository.findAll();

            return allCourses.stream()
                    .filter(course -> canUserAccessCourse(userId, userRole, course.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error getting user courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean canUserSeeTask(Task task, String userId, String userRole) {
        // Admin can see all tasks
        if ("1100".equals(userRole)) return true;

        // Lecturer can see tasks in their courses
        if ("1200".equals(userRole)) {
            return canUserAccessCourse(userId, userRole, task.getCourseId());
        }

        // Students can only see published, visible tasks
        if ("1300".equals(userRole)) {
            return Boolean.TRUE.equals(task.getVisibleToStudents()) &&
                    task.isPublished() &&
                    canUserAccessCourse(userId, userRole, task.getCourseId());
        }

        return false;
    }

    private boolean canUserSeeMeeting(Meeting meeting, String userId, String userRole) {
        // Admin can see all meetings
        if ("1100".equals(userRole)) return true;

        // Lecturer can see meetings in their courses or meetings they created
        if ("1200".equals(userRole)) {
            return canUserAccessCourse(userId, userRole, meeting.getCourseId()) ||
                    userId.equals(meeting.getCreatedBy()) ||
                    userId.equals(meeting.getLecturerId());
        }

        // Students can see meetings in courses they're enrolled in or meetings they're participating in
        if ("1300".equals(userRole)) {
            return canUserAccessCourse(userId, userRole, meeting.getCourseId()) ||
                    (meeting.getParticipants() != null && meeting.getParticipants().contains(userId));
        }

        return false;
    }

    private ExtensionItemResponse convertTaskToExtensionItem(Task task, String userId, String userRole) {
        ExtensionItemResponse item = new ExtensionItemResponse();

        item.setId(task.getId());
        item.setName(task.getTitle());
        item.setDescription(task.getDescription());
        item.setType("task");
        item.setDueDate(task.getDueDate().toString());
        item.setCourse(getCourseName(task.getCourseId()));

        // Determine status based on submissions and due date
        String status = determineTaskStatus(task, userId, userRole);
        item.setStatus(status);

        // Calculate priority based on due date and current status
        String priority = calculatePriority(task.getDueDate(), status);
        item.setPriority(priority);

        // Additional task fields
        item.setCategory(task.getCategory());
        item.setMaxPoints(task.getMaxPoints());
        item.setFileUrl(task.getFileUrl());
        item.setFileName(task.getFileName());

        // For students, check submission status
        if ("1300".equals(userRole)) {
            Optional<TaskSubmission> submission = taskSubmissionRepository
                    .findByTaskIdAndStudentId(task.getId(), userId);
            item.setHasSubmission(submission.isPresent());
            if (submission.isPresent()) {
                item.setSubmissionGrade(submission.get().getGrade());
            }
        }

        return item;
    }

    private ExtensionItemResponse convertMeetingToExtensionItem(Meeting meeting, String userId, String userRole) {
        ExtensionItemResponse item = new ExtensionItemResponse();

        item.setId(meeting.getId());
        item.setName(meeting.getTitle());
        item.setDescription(meeting.getDescription());
        item.setType("meeting");

        // Use meeting datetime for due date
        if (meeting.getDatetime() != null) {
            item.setDueDate(meeting.getDatetime().toLocalDate().toString());
        } else if (meeting.getScheduledAt() != null) {
            item.setDueDate(meeting.getScheduledAt().toLocalDate().toString());
        } else {
            item.setDueDate(LocalDate.now().toString());
        }

        item.setCourse(getCourseName(meeting.getCourseId()));

        // Determine meeting status
        String status = determineMeetingStatus(meeting);
        item.setStatus(status);

        // Calculate priority based on meeting date
        LocalDate meetingDate = meeting.getDatetime() != null ?
                meeting.getDatetime().toLocalDate() :
                (meeting.getScheduledAt() != null ? meeting.getScheduledAt().toLocalDate() : LocalDate.now());

        String priority = calculatePriority(meetingDate, status);
        item.setPriority(priority);

        // Additional meeting fields
        item.setCategory("meeting");
        item.setAnnouncementType(meeting.getType());
        item.setLocation("Online Meeting");
        item.setIsImportant("active".equals(meeting.getStatus()));

        // IMPORTANT: Include invitation link in the response
        item.setFileUrl(meeting.getInvitationLink()); // Use fileUrl field to store invitation link

        return item;
    }

    private String determineTaskStatus(Task task, String userId, String userRole) {
        try {
            // For students, check if they have submitted
            if ("1300".equals(userRole)) {
                Optional<TaskSubmission> submission = taskSubmissionRepository
                        .findByTaskIdAndStudentId(task.getId(), userId);

                if (submission.isPresent()) {
                    TaskSubmission sub = submission.get();
                    if (sub.getGrade() != null) {
                        return "completed";
                    } else {
                        return "in-progress"; // Submitted but not graded
                    }
                }
            }

            // Check if overdue (but only if more than 2 days past due)
            if (task.getDueDate() != null) {
                long daysOverdue = ChronoUnit.DAYS.between(task.getDueDate(), LocalDate.now());
                if (daysOverdue > 2) {
                    return "overdue";
                }
            }

            return "pending";
        } catch (Exception e) {
            return "pending";
        }
    }

    private String determineMeetingStatus(Meeting meeting) {
        if (meeting.getStatus() != null) {
            switch (meeting.getStatus().toLowerCase()) {
                case "active":
                    return "in-progress";
                case "ended":
                    return "completed";
                case "cancelled":
                    return "overdue";
                case "scheduled":
                default:
                    // Check if meeting date has passed by more than 2 days
                    LocalDate meetingDate = meeting.getDatetime() != null ?
                            meeting.getDatetime().toLocalDate() :
                            (meeting.getScheduledAt() != null ? meeting.getScheduledAt().toLocalDate() : LocalDate.now());

                    long daysOverdue = ChronoUnit.DAYS.between(meetingDate, LocalDate.now());
                    if (daysOverdue > 2) {
                        return "overdue";
                    }
                    return "pending";
            }
        }
        return "pending";
    }

    private String calculatePriority(LocalDate dueDate, String status) {
        if ("overdue".equals(status)) {
            return "urgent";
        }

        if (dueDate != null) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

            if (daysUntilDue < 0) return "urgent";
            if (daysUntilDue <= 3) return "urgent";
            if (daysUntilDue <= 7) return "warning";
        }

        return "safe";
    }

    private String getCourseName(String courseId) {
        try {
            return courseRepository.findById(courseId)
                    .map(Course::getName)
                    .orElse("Unknown Course");
        } catch (Exception e) {
            return "Unknown Course";
        }
    }

    private List<ExtensionItemResponse> generateMockAnnouncements(List<Course> courses) {
        List<ExtensionItemResponse> announcements = new ArrayList<>();

        // Generate some mock announcements based on current date and courses
        LocalDate today = LocalDate.now();

        for (Course course : courses.subList(0, Math.min(3, courses.size()))) {
            // Career fair announcement
            ExtensionItemResponse careerFair = new ExtensionItemResponse();
            careerFair.setId("ann_" + UUID.randomUUID().toString().substring(0, 8));
            careerFair.setName("Campus Career Fair");
            careerFair.setDescription("Annual career fair with 50+ companies recruiting students");
            careerFair.setType("announcement");
            careerFair.setDueDate(today.plusDays(10).toString());
            careerFair.setStatus("pending");
            careerFair.setPriority("warning");
            careerFair.setCourse("Career Services");
            careerFair.setAnnouncementType("event");
            careerFair.setLocation("Main Campus Hall");
            careerFair.setIsImportant(true);
            announcements.add(careerFair);

            // Library maintenance
            ExtensionItemResponse libraryMaint = new ExtensionItemResponse();
            libraryMaint.setId("ann_" + UUID.randomUUID().toString().substring(0, 8));
            libraryMaint.setName("Library System Maintenance");
            libraryMaint.setDescription("Digital library will be offline for system updates");
            libraryMaint.setType("announcement");
            libraryMaint.setDueDate(today.plusDays(5).toString());
            libraryMaint.setStatus("pending");
            libraryMaint.setPriority("safe");
            libraryMaint.setCourse("Library");
            libraryMaint.setAnnouncementType("maintenance");
            libraryMaint.setLocation("Digital Library");
            libraryMaint.setIsImportant(false);
            announcements.add(libraryMaint);

            break; // Only add announcements for first course to avoid duplicates
        }

        return announcements;
    }

    private ExtensionStatsResponse calculateStats(List<ExtensionItemResponse> items, String userId, String userRole) {
        ExtensionStatsResponse stats = new ExtensionStatsResponse();

        int totalItems = items.size();
        int urgentItems = (int) items.stream().filter(item -> "urgent".equals(item.getPriority())).count();
        int pendingItems = (int) items.stream().filter(item -> "pending".equals(item.getStatus())).count();
        int completedItems = (int) items.stream().filter(item -> "completed".equals(item.getStatus())).count();
        int overdueItems = (int) items.stream().filter(item -> "overdue".equals(item.getStatus())).count();
        int tasksCount = (int) items.stream().filter(item -> "task".equals(item.getType())).count();
        int meetingsCount = (int) items.stream().filter(item -> "meeting".equals(item.getType())).count();
        int announcementsCount = (int) items.stream().filter(item -> "announcement".equals(item.getType())).count();

        // Calculate completion rate
        double completionRate = totalItems > 0 ? (completedItems * 100.0) / totalItems : 0.0;

        // Calculate upcoming items
        LocalDate today = LocalDate.now();

        int thisWeekDue = (int) items.stream()
                .filter(item -> {
                    try {
                        LocalDate dueDate = LocalDate.parse(item.getDueDate());
                        return !dueDate.isBefore(today) && !dueDate.isAfter(today.plusWeeks(1));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        int nextWeekDue = (int) items.stream()
                .filter(item -> {
                    try {
                        LocalDate dueDate = LocalDate.parse(item.getDueDate());
                        return dueDate.isAfter(today.plusWeeks(1)) && !dueDate.isAfter(today.plusWeeks(2));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        stats.setTotalItems(totalItems);
        stats.setUrgentItems(urgentItems);
        stats.setPendingItems(pendingItems);
        stats.setCompletedItems(completedItems);
        stats.setOverdueItems(overdueItems);
        stats.setTasksCount(tasksCount);
        stats.setAnnouncementsCount(announcementsCount);
        stats.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        stats.setThisWeekDue(thisWeekDue);
        stats.setNextWeekDue(nextWeekDue);

        // Add meetings count to stats response
        stats.setMeetingsCount(meetingsCount);

        return stats;
    }

    private boolean matchesStatusFilter(ExtensionItemResponse item, String statusFilter) {
        return statusFilter == null || "all".equals(statusFilter) || statusFilter.equals(item.getStatus());
    }

    private boolean matchesPriorityFilter(ExtensionItemResponse item, String priorityFilter) {
        return priorityFilter == null || "all".equals(priorityFilter) || priorityFilter.equals(item.getPriority());
    }

    private int compareItemsByPriority(ExtensionItemResponse a, ExtensionItemResponse b) {
        // Priority order: urgent > warning > safe
        Map<String, Integer> priorityOrder = Map.of(
                "urgent", 0,
                "warning", 1,
                "safe", 2
        );

        int aPriority = priorityOrder.getOrDefault(a.getPriority(), 3);
        int bPriority = priorityOrder.getOrDefault(b.getPriority(), 3);

        if (aPriority != bPriority) {
            return Integer.compare(aPriority, bPriority);
        }

        // If same priority, sort by due date
        return compareItemsByDueDate(a, b);
    }

    private int compareItemsByDueDate(ExtensionItemResponse a, ExtensionItemResponse b) {
        try {
            LocalDate dateA = LocalDate.parse(a.getDueDate());
            LocalDate dateB = LocalDate.parse(b.getDueDate());
            return dateA.compareTo(dateB);
        } catch (Exception e) {
            return 0;
        }
    }
}