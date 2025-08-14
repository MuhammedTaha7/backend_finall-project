package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.TaskSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends MongoRepository<TaskSubmission, String> {

    // Find submissions by task ID
    List<TaskSubmission> findByTaskIdOrderBySubmittedAtDesc(String taskId);

    // Simple find by task ID (for TaskServiceImpl compatibility)
    List<TaskSubmission> findByTaskId(String taskId);

    // Find submissions by task ID with pagination
    Page<TaskSubmission> findByTaskIdOrderBySubmittedAtDesc(String taskId, Pageable pageable);

    // Find submissions by student ID
    List<TaskSubmission> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    // Find submissions by course ID
    List<TaskSubmission> findByCourseIdOrderBySubmittedAtDesc(String courseId);

    // Find specific student's submission for a task
    Optional<TaskSubmission> findByTaskIdAndStudentId(String taskId, String studentId);

    // Find all submissions by a student for a task (for multiple attempts)
    List<TaskSubmission> findByTaskIdAndStudentIdOrderByAttemptNumberDesc(String taskId, String studentId);

    // Find latest submission by student for a task
    Optional<TaskSubmission> findFirstByTaskIdAndStudentIdOrderByAttemptNumberDesc(String taskId, String studentId);

    // Find submissions by status
    List<TaskSubmission> findByTaskIdAndStatus(String taskId, String status);

    // Find submissions that need grading
    @Query("{ 'taskId': ?0, 'status': 'submitted', 'grade': null }")
    List<TaskSubmission> findUngraduatedSubmissionsByTask(String taskId);

    // Find late submissions
    List<TaskSubmission> findByTaskIdAndIsLateTrue(String taskId);

    // Find submissions within date range
    List<TaskSubmission> findByTaskIdAndSubmittedAtBetween(String taskId, LocalDateTime start, LocalDateTime end);

    // Find submissions by course and date range
    List<TaskSubmission> findByCourseIdAndSubmittedAtBetween(String courseId, LocalDateTime start, LocalDateTime end);

    // Count submissions by task
    long countByTaskId(String taskId);

    // Count graded submissions by task
    @Query(value = "{ 'taskId': ?0, 'grade': { $ne: null } }", count = true)
    long countGradedSubmissionsByTask(String taskId);

    // Count submissions by student and course
    long countByStudentIdAndCourseId(String studentId, String courseId);

    // Find group submissions
    List<TaskSubmission> findByTaskIdAndIsGroupSubmissionTrue(String taskId);

    // Find submissions with plagiarism check
    List<TaskSubmission> findByTaskIdAndPlagiarismCheckedTrue(String taskId);

    // Find submissions with high plagiarism score
    @Query("{ 'taskId': ?0, 'plagiarismScore': { $gte: ?1 } }")
    List<TaskSubmission> findByTaskIdAndHighPlagiarismScore(String taskId, Double minScore);

    // Find auto-graded submissions
    List<TaskSubmission> findByTaskIdAndAutoGradedTrue(String taskId);

    // Find manually overridden submissions
    List<TaskSubmission> findByTaskIdAndManualOverrideTrue(String taskId);

    // Find submissions with files
    @Query("{ 'taskId': ?0, 'fileUrls': { $exists: true, $not: { $size: 0 } } }")
    List<TaskSubmission> findSubmissionsWithFilesByTask(String taskId);

    // Statistical queries
    @Query("{ 'taskId': ?0, 'grade': { $ne: null } }")
    List<TaskSubmission> findGradedSubmissionsByTask(String taskId);

    // Find submissions by multiple tasks (for instructor dashboard)
    List<TaskSubmission> findByTaskIdInOrderBySubmittedAtDesc(List<String> taskIds);

    // Find recent submissions (last N days)
    @Query("{ 'submittedAt': { $gte: ?0 } }")
    List<TaskSubmission> findRecentSubmissions(LocalDateTime sinceDate);

    // Find submissions by instructor (through task relationship)
    @Query("{ 'courseId': ?0, 'submittedAt': { $gte: ?1, $lte: ?2 } }")
    List<TaskSubmission> findSubmissionsByCourseAndDateRange(String courseId, LocalDateTime start, LocalDateTime end);

    // Delete operations
    void deleteByTaskId(String taskId);

    void deleteByStudentIdAndTaskId(String studentId, String taskId);

    // Advanced analytics queries
    @Query(value = "{ 'taskId': ?0 }",
            fields = "{ 'grade': 1, 'isLate': 1, 'submittedAt': 1, 'timeSpent': 1 }")
    List<TaskSubmission> findSubmissionStatisticsByTask(String taskId);

    // Find submissions needing instructor attention
    @Query("{ 'courseId': ?0, " +
            "$or: [ " +
            "  { 'status': 'submitted', 'grade': null }, " +
            "  { 'plagiarismScore': { $gte: 50 } }, " +
            "  { 'isLate': true, 'grade': null } " +
            "] }")
    List<TaskSubmission> findSubmissionsNeedingAttention(String courseId);

    // Find best performing submissions for a task
    @Query("{ 'taskId': ?0, 'grade': { $ne: null } }")
    List<TaskSubmission> findTopSubmissionsByTask(String taskId);

    // Find submissions by attempt number
    List<TaskSubmission> findByTaskIdAndAttemptNumber(String taskId, Integer attemptNumber);

    // Check if student has existing submission
    boolean existsByTaskIdAndStudentId(String taskId, String studentId);

    // Count attempts by student for a task
    long countByTaskIdAndStudentId(String taskId, String studentId);

    // Find submissions with feedback
    @Query("{ 'taskId': ?0, 'feedback': { $ne: null, $ne: '' } }")
    List<TaskSubmission> findSubmissionsWithFeedback(String taskId);

    // Find submissions without feedback
    @Query("{ 'taskId': ?0, 'grade': { $ne: null }, " +
            "$or: [ { 'feedback': { $exists: false } }, { 'feedback': null }, { 'feedback': '' } ] }")
    List<TaskSubmission> findSubmissionsWithoutFeedback(String taskId);
}