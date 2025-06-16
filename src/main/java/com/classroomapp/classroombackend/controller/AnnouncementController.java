package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.NotificationDto;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "http://localhost:3000")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    @Autowired
    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    // Get announcements for specific user with filters
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getAnnouncementsForUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "all") String filter) {
        System.out.println("Yêu cầu lấy thông báo cho user ID: " + userId + ", filter: " + filter);
        
        List<Announcement> announcements;
        
        // For now, return all announcements since we don't have user-specific logic yet
        try {
            announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        } catch (Exception e) {
            System.err.println("Error fetching announcements: " + e.getMessage());
            announcements = new ArrayList<>();
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(announcements));
    }
    
    // Get all announcements
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllAnnouncements() {
        System.out.println("Yêu cầu lấy tất cả thông báo");
        List<Announcement> announcements;
        
        try {
            announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        } catch (Exception e) {
            System.err.println("Error fetching all announcements: " + e.getMessage());
            announcements = new ArrayList<>();
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(announcements));
    }
    
    // Mark announcement as read
    @PutMapping("/{announcementId}/read")
    public ResponseEntity<Void> markAnnouncementAsRead(@PathVariable Long announcementId) {
        System.out.println("Yêu cầu đánh dấu thông báo ID: " + announcementId + " là đã đọc");
        
        try {
            Optional<Announcement> announcementOpt = announcementRepository.findById(announcementId);
            
            if (announcementOpt.isPresent()) {
                Announcement announcement = announcementOpt.get();
                // Using isPinned as a temporary field for isRead until proper field is added
                announcement.setIsPinned(true); 
                announcementRepository.save(announcement);
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            System.err.println("Error marking announcement as read: " + e.getMessage());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    // Get unread count for user
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Long userId) {
        System.out.println("Yêu cầu đếm số thông báo chưa đọc cho user ID: " + userId);
        
        // For now, return a default count since we don't have user-specific read tracking yet
        int unreadCount = 0;
        try {
            List<Announcement> allAnnouncements = announcementRepository.findAllByOrderByCreatedAtDesc();
            unreadCount = allAnnouncements.size(); // For now, assume all are unread
        } catch (Exception e) {
            System.err.println("Error counting unread announcements: " + e.getMessage());
            unreadCount = 0;
        }
        
        return ResponseEntity.ok(unreadCount);
    }
    
    // Helper method to convert entity to DTO
    private List<NotificationDto> convertToNotificationDtoList(List<Announcement> announcements) {
        return announcements.stream()
            .map(announcement -> new NotificationDto(
                announcement.getId(),
                announcement.getContent(),
                announcement.getCreatedAt(),
                announcement.getIsPinned(), // Using isPinned as a temporary field for isRead
                "System" // For now, use default sender name since we don't have user relationship set up
            ))
            .collect(Collectors.toList());
    }
}
