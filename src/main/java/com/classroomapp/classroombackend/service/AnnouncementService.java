package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.AnnouncementDto;
import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AnnouncementService {
    
    /**
     * Create a new announcement
     */
    AnnouncementDto createAnnouncement(CreateAnnouncementDto createDto, Long createdBy);
    
    /**
     * Create announcement with attachments
     */
    AnnouncementDto createAnnouncementWithAttachments(CreateAnnouncementDto createDto, 
                                                    List<MultipartFile> attachments, Long createdBy);
    
    /**
     * Get announcement by ID
     */
    AnnouncementDto getAnnouncementById(Long announcementId);
    
    /**
     * Get active announcements for a classroom
     */
    List<AnnouncementDto> getActiveAnnouncementsByClassroom(Long classroomId);
    
    /**
     * Get global announcements
     */
    List<AnnouncementDto> getGlobalAnnouncements();
    
    /**
     * Get pinned announcements
     */
    List<AnnouncementDto> getPinnedAnnouncements(Long classroomId);
    
    /**
     * Get announcements by priority
     */
    List<AnnouncementDto> getAnnouncementsByPriority(Long classroomId, String priority);
    
    /**
     * Update announcement
     */
    AnnouncementDto updateAnnouncement(Long announcementId, CreateAnnouncementDto updateDto);
    
    /**
     * Delete announcement
     */
    void deleteAnnouncement(Long announcementId);
    
    /**
     * Archive announcement
     */
    AnnouncementDto archiveAnnouncement(Long announcementId);
    
    /**
     * Pin/unpin announcement
     */
    AnnouncementDto togglePinAnnouncement(Long announcementId);
    
    /**
     * Mark announcement as read by user
     */
    void markAsRead(Long announcementId, Long userId);
    
    /**
     * Get unread announcements for a user
     */
    List<AnnouncementDto> getUnreadAnnouncements(Long classroomId, Long userId);
    
    /**
     * Count unread announcements for a user
     */
    long countUnreadAnnouncements(Long classroomId, Long userId);
    
    /**
     * Search announcements
     */
    List<AnnouncementDto> searchAnnouncements(Long classroomId, String searchTerm);
    
    /**
     * Get announcements created by a user
     */
    List<AnnouncementDto> getAnnouncementsByCreator(Long createdBy);
    
    /**
     * Process scheduled announcements
     */
    void processScheduledAnnouncements();
    
    /**
     * Archive expired announcements
     */
    void archiveExpiredAnnouncements();
}
