package com.classroomapp.classroombackend.service.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler service for handling announcement lifecycle automation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementSchedulerService {

    private final AnnouncementRepository announcementRepository;

    /**
     * Process scheduled announcements every 5 minutes
     * Activates announcements that have reached their scheduled time
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void processScheduledAnnouncements() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find announcements that should be activated
            List<Announcement> scheduledAnnouncements = announcementRepository.findScheduledAnnouncements(now);
            
            if (!scheduledAnnouncements.isEmpty()) {
                log.info("🔄 Processing {} scheduled announcements", scheduledAnnouncements.size());
                
                for (Announcement announcement : scheduledAnnouncements) {
                    if (announcement.getScheduledDate() != null && 
                        announcement.getScheduledDate().isEqual(now.withSecond(0).withNano(0)) &&
                        announcement.getStatus() == Announcement.AnnouncementStatus.ACTIVE) {
                        
                        // Could add notification logic here
                        log.info("✅ Scheduled announcement is now active: {} (ID: {})", 
                               announcement.getTitle(), announcement.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Error processing scheduled announcements: {}", e.getMessage(), e);
        }
    }

    /**
     * Archive expired announcements every hour
     * Archives announcements that have passed their expiry date
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void archiveExpiredAnnouncements() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find announcements that should be archived
            List<Announcement> expiredAnnouncements = announcementRepository.findExpiredAnnouncements(now);
            
            if (!expiredAnnouncements.isEmpty()) {
                log.info("🗂️ Processing {} expired announcements", expiredAnnouncements.size());
                
                for (Announcement announcement : expiredAnnouncements) {
                    if (announcement.getExpiryDate() != null && 
                        announcement.getExpiryDate().isBefore(now) &&
                        announcement.getStatus() == Announcement.AnnouncementStatus.ACTIVE) {
                        
                        announcement.setStatus(Announcement.AnnouncementStatus.ARCHIVED);
                        announcementRepository.save(announcement);
                        
                        log.info("📦 Archived expired announcement: {} (ID: {})", 
                               announcement.getTitle(), announcement.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Error archiving expired announcements: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old archived announcements every day at 2 AM
     * Optionally delete very old archived announcements to save space
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    @Transactional
    public void cleanupOldArchivedAnnouncements() {
        try {
            // Delete announcements archived more than 1 year ago
            LocalDateTime cutoffDate = LocalDateTime.now().minusYears(1);
            
            // Use simple query to find old archived announcements
            List<Announcement> oldArchivedAnnouncements = announcementRepository
                .findAll()
                .stream()
                .filter(a -> a.getStatus() == Announcement.AnnouncementStatus.ARCHIVED)
                .filter(a -> a.getUpdatedAt().isBefore(cutoffDate))
                .collect(java.util.stream.Collectors.toList());
            
            if (!oldArchivedAnnouncements.isEmpty()) {
                log.info("🧹 Cleaning up {} old archived announcements", oldArchivedAnnouncements.size());
                
                for (Announcement announcement : oldArchivedAnnouncements) {
                    announcementRepository.delete(announcement);
                    log.debug("🗑️ Deleted old archived announcement: {} (ID: {})", 
                           announcement.getTitle(), announcement.getId());
                }
                
                log.info("✅ Cleanup completed. Deleted {} old archived announcements", 
                       oldArchivedAnnouncements.size());
            }
        } catch (Exception e) {
            log.error("❌ Error cleaning up old archived announcements: {}", e.getMessage(), e);
        }
    }
}
