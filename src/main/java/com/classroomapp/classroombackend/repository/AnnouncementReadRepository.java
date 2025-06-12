package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
    
    /**
     * Check if a user has read an announcement
     */
    boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);
    
    /**
     * Find read record for user and announcement
     */
    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(Long announcementId, Long userId);
    
    /**
     * Find all read announcements for a user
     */
    List<AnnouncementRead> findByUserIdOrderByReadAtDesc(Long userId);
    
    /**
     * Find who read a specific announcement
     */
    List<AnnouncementRead> findByAnnouncementIdOrderByReadAtDesc(Long announcementId);
    
    /**
     * Count how many users read an announcement
     */
    long countByAnnouncementId(Long announcementId);
    
    /**
     * Get unread announcements for a user in a classroom
     */
    @Query("SELECT a.id FROM Announcement a " +
           "WHERE a.classroomId = :classroomId " +
           "AND a.status = 'ACTIVE' " +
           "AND a.id NOT IN (SELECT ar.announcementId FROM AnnouncementRead ar WHERE ar.userId = :userId)")
    List<Long> findUnreadAnnouncementIds(@Param("classroomId") Long classroomId,
                                       @Param("userId") Long userId);
    
    /**
     * Count unread announcements for a user in a classroom
     */
    @Query("SELECT COUNT(a) FROM Announcement a " +
           "WHERE a.classroomId = :classroomId " +
           "AND a.status = 'ACTIVE' " +
           "AND a.id NOT IN (SELECT ar.announcementId FROM AnnouncementRead ar WHERE ar.userId = :userId)")
    long countUnreadAnnouncements(@Param("classroomId") Long classroomId,
                                 @Param("userId") Long userId);
}
