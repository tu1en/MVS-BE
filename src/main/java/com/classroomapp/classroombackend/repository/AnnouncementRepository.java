package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    /**
     * Find active announcements for a classroom
     */
    @Query("SELECT a FROM Announcement a WHERE a.classroomId = :classroomId " +
           "AND a.status = 'ACTIVE' " +
           "AND (a.scheduledDate IS NULL OR a.scheduledDate <= :now) " +
           "AND (a.expiryDate IS NULL OR a.expiryDate > :now) " +
           "ORDER BY a.isPinned DESC, a.createdAt DESC")
    List<Announcement> findActiveByClassroom(@Param("classroomId") Long classroomId,
                                           @Param("now") LocalDateTime now);
    
    /**
     * Find global announcements (not classroom-specific)
     */
    @Query("SELECT a FROM Announcement a WHERE a.classroomId IS NULL " +
           "AND a.status = 'ACTIVE' " +
           "AND (a.scheduledDate IS NULL OR a.scheduledDate <= :now) " +
           "AND (a.expiryDate IS NULL OR a.expiryDate > :now) " +
           "ORDER BY a.isPinned DESC, a.createdAt DESC")
    List<Announcement> findGlobalAnnouncements(@Param("now") LocalDateTime now);
    
    /**
     * Find pinned announcements
     */
    List<Announcement> findByClassroomIdAndIsPinnedTrueAndStatusOrderByCreatedAtDesc(
            Long classroomId, Announcement.AnnouncementStatus status);
    
    /**
     * Find announcements by priority
     */
    List<Announcement> findByClassroomIdAndPriorityAndStatusOrderByCreatedAtDesc(
            Long classroomId, Announcement.Priority priority, Announcement.AnnouncementStatus status);
    
    /**
     * Find announcements by target audience
     */
    List<Announcement> findByClassroomIdAndTargetAudienceAndStatusOrderByCreatedAtDesc(
            Long classroomId, Announcement.TargetAudience targetAudience, 
            Announcement.AnnouncementStatus status);
    
    /**
     * Find announcements created by a user
     */
    List<Announcement> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    /**
     * Find scheduled announcements
     */
    @Query("SELECT a FROM Announcement a WHERE a.scheduledDate > :now " +
           "AND a.status = 'ACTIVE' ORDER BY a.scheduledDate ASC")
    List<Announcement> findScheduledAnnouncements(@Param("now") LocalDateTime now);
    
    /**
     * Find expired announcements
     */
    @Query("SELECT a FROM Announcement a WHERE a.expiryDate < :now " +
           "AND a.status = 'ACTIVE'")
    List<Announcement> findExpiredAnnouncements(@Param("now") LocalDateTime now);
    
    /**
     * Search announcements by title or content
     */
    @Query("SELECT a FROM Announcement a WHERE a.classroomId = :classroomId " +
           "AND a.status = 'ACTIVE' " +
           "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.isPinned DESC, a.createdAt DESC")
    List<Announcement> searchAnnouncements(@Param("classroomId") Long classroomId,
                                         @Param("searchTerm") String searchTerm);

    /**
     * Find all announcements ordered by creation date
     */
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
