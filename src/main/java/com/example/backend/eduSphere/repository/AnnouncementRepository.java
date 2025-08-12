package com.example.backend.eduSphere.repository;

import com.example.backend.eduSphere.entity.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
    // 🆕 This query is now for students only
    @Query("{$or: ["
            + "{'targetAudienceType': 'all'},"
            + "{'targetAudienceType': 'student'},"
            + "{'targetAudienceType': 'course'}"
            + "]}")
    List<Announcement> findAnnouncementsForStudentBase();

    // 🆕 This is the new query to fetch announcements for a lecturer, excluding those they created themselves
    @Query("{$or: ["
            + "{'targetAudienceType': 'all', 'creatorId': {$ne: ?0}},"
            + "{'targetAudienceType': 'lecturer', 'creatorId': {$ne: ?0}},"
            + "{'targetAudienceType': 'course', 'targetCourseId': {$in: ?1}}"
            + "]}")
    List<Announcement> findTargetedAnnouncementsForLecturer(String userId, List<String> courseIds);

    List<Announcement> findByCreatorIdOrderByCreatedAtDesc(String creatorId);
}