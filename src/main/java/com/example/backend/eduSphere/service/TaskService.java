
package com.example.backend.eduSphere.service;

import com.example.backend.eduSphere.dto.request.TaskCreateRequest;
import com.example.backend.eduSphere.dto.request.TaskUpdateRequest;
import com.example.backend.eduSphere.dto.response.TaskDetailResponse;
import com.example.backend.eduSphere.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    // CRUD operations
    TaskResponse createTask(TaskCreateRequest request, String instructorId);

    TaskResponse updateTask(String taskId, TaskUpdateRequest request, String instructorId);

    void deleteTask(String taskId, String instructorId);

    Optional<TaskResponse> getTaskById(String taskId);

    TaskDetailResponse getTaskDetails(String taskId, String instructorId);

    // Query operations
    List<TaskResponse> getTasksByCourse(String courseId);

    Page<TaskResponse> getTasksByCourse(String courseId, Pageable pageable);

    List<TaskResponse> getTasksByInstructor(String instructorId);

    List<TaskResponse> getTasksByStatus(String courseId, String status);

    List<TaskResponse> getOverdueTasks(String courseId);

    List<TaskResponse> getUpcomingTasks(String courseId, int daysAhead);

    List<TaskResponse> searchTasks(String courseId, String searchTerm);

    List<TaskResponse> getTasksByCategory(String courseId, String category);

    List<TaskResponse> getTasksByPriority(String courseId, String priority);

    List<TaskResponse> getTasksNeedingGrading(String courseId);

    // Student-facing operations
    List<TaskResponse> getAvailableTasksForStudents(String courseId);

    // Statistics and analytics
    TaskDetailResponse.TaskStatistics getTaskStatistics(String taskId);

    // File operations
    TaskResponse attachFileToTask(String taskId, String fileUrl, String fileName, Long fileSize, String instructorId);

    void removeFileFromTask(String taskId, String instructorId);

    // Batch operations
    void updateTaskVisibility(String courseId, List<String> taskIds, boolean visible, String instructorId);

    void updateTaskStatus(String courseId, List<String> taskIds, String status, String instructorId);

    // Validation
    boolean canUserAccessTask(String taskId, String userId, String userRole);

    boolean canUserModifyTask(String taskId, String userId, String userRole);

    // Statistics calculation
    void recalculateTaskStatistics(String taskId);

    void recalculateAllTaskStatisticsForCourse(String courseId);
}