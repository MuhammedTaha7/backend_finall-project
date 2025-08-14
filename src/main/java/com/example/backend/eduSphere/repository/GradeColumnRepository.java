package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.GradeColumn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeColumnRepository extends MongoRepository<GradeColumn, String> {

    /**
     * Find all grade columns for a specific course
     */
    List<GradeColumn> findByCourseId(String courseId);

    /**
     * ✅ FIXED: Method that GradeServiceImpl is looking for (line 30)
     * Find all active grade columns for a specific course
     */
    List<GradeColumn> findByCourseIdAndIsActiveTrue(String courseId);

    /**
     * Find all active grade columns for a specific course, ordered by display order
     */
    List<GradeColumn> findByCourseIdAndIsActiveTrueOrderByDisplayOrderAsc(String courseId);

    /**
     * ✅ FIXED: Method that GradeServiceImpl is looking for (line 62)
     * Find all grade columns for a course ordered by display order
     */
    List<GradeColumn> findByCourseIdOrderByDisplayOrderAsc(String courseId);

    /**
     * Find grade column by course ID and linked assignment ID
     */
    Optional<GradeColumn> findByCourseIdAndLinkedAssignmentId(String courseId, String linkedAssignmentId);

    /**
     * Find all auto-created grade columns for a course
     */
    List<GradeColumn> findByCourseIdAndAutoCreatedTrue(String courseId);

    /**
     * Find all manually created grade columns for a course
     */
    @Query("{ 'courseId': ?0, '$or': [{ 'autoCreated': false }, { 'autoCreated': null }] }")
    List<GradeColumn> findByCourseIdAndManuallyCreated(String courseId);

    /**
     * Find grade column by name and course ID (case-insensitive)
     */
    @Query("{ 'courseId': ?0, 'name': { $regex: ?1, $options: 'i' } }")
    Optional<GradeColumn> findByCourseIdAndNameIgnoreCase(String courseId, String name);

    /**
     * Find all grade columns for a course by type
     */
    List<GradeColumn> findByCourseIdAndType(String courseId, String type);

    /**
     * Check if a grade column exists for a specific assignment
     */
    boolean existsByCourseIdAndLinkedAssignmentId(String courseId, String linkedAssignmentId);

    /**
     * Find all grade columns created by a specific instructor
     */
    List<GradeColumn> findByCreatedBy(String instructorId);

    /**
     * Count total grade columns for a course
     */
    long countByCourseId(String courseId);

    /**
     * Count auto-created grade columns for a course
     */
    long countByCourseIdAndAutoCreatedTrue(String courseId);

    /**
     * Get sum of all percentages for a course
     */
    @Query(value = "{ 'courseId': ?0, 'isActive': true }", fields = "{ 'percentage': 1 }")
    List<GradeColumn> findActiveByCourseIdWithPercentageOnly(String courseId);

    /**
     * Find grade columns that exceed max display order for a course
     */
    @Query("{ 'courseId': ?0, 'displayOrder': { $gte': ?1 } }")
    List<GradeColumn> findByCourseIdAndDisplayOrderGreaterThanEqual(String courseId, int displayOrder);

    /**
     * Find orphaned grade columns (linked to non-existent assignments)
     * This would require a more complex query or service-level logic
     */
    List<GradeColumn> findByCourseIdAndLinkedAssignmentIdIsNotNull(String courseId);

    /**
     * Custom query to find grade columns that might be duplicates
     */
    @Query("{ 'courseId': ?0, 'name': { $regex: ?1, $options: 'i' }, '_id': { $ne: ?2 } }")
    List<GradeColumn> findPotentialDuplicates(String courseId, String name, String excludeId);

    /**
     * Delete all grade columns for a course (cleanup method)
     */
    void deleteByCourseId(String courseId);

    /**
     * Delete all auto-created grade columns for a course
     */
    void deleteByCourseIdAndAutoCreatedTrue(String courseId);

    /**
     * Find next available display order for a course
     */
    @Query(value = "{ 'courseId': ?0 }", sort = "{ 'displayOrder': -1 }")
    Optional<GradeColumn> findTopByCourseIdOrderByDisplayOrderDesc(String courseId);

    // ✅ ADDITIONAL: Useful methods for grade management

    /**
     * Find all grade columns for a course ordered by creation date
     */
    List<GradeColumn> findByCourseIdOrderByCreatedAtAsc(String courseId);

    /**
     * Find active grade columns by course and type
     */
    List<GradeColumn> findByCourseIdAndIsActiveTrueAndType(String courseId, String type);

    /**
     * Count active grade columns for a course
     */
    long countByCourseIdAndIsActiveTrue(String courseId);

    /**
     * Find grade columns with percentage greater than specified value
     */
    @Query("{ 'courseId': ?0, 'percentage': { $gt: ?1 } }")
    List<GradeColumn> findByCourseIdAndPercentageGreaterThan(String courseId, double percentage);

    /**
     * Find all grade columns excluding a specific ID
     */
    @Query("{ 'courseId': ?0, '_id': { $ne: ?1 } }")
    List<GradeColumn> findByCourseIdExcludingId(String courseId, String excludeId);
}