package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.NotificationDto;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "http://localhost:3000")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    @Autowired
    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }    // Create a new announcement
    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(@Valid @RequestBody Announcement announcement) {
        System.out.println("Tạo thông báo mới: " + announcement.getTitle());
        try {
            // Set default values if not provided
            if (announcement.getStatus() == null) {
                announcement.setStatus(Announcement.AnnouncementStatus.ACTIVE);
            }
            if (announcement.getIsPinned() == null) {
                announcement.setIsPinned(false);
            }
            if (announcement.getPriority() == null) {
                announcement.setPriority(Announcement.Priority.NORMAL);
            }
            if (announcement.getTargetAudience() == null) {
                announcement.setTargetAudience(Announcement.TargetAudience.ALL);
            }
            
            Announcement savedAnnouncement = announcementRepository.save(announcement);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAnnouncement);
        } catch (Exception e) {
            System.err.println("Error creating announcement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Update an existing announcement
    @PutMapping("/{announcementId}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long announcementId, 
            @Valid @RequestBody Announcement updatedAnnouncement) {
        System.out.println("Cập nhật thông báo ID: " + announcementId);
        
        try {
            Optional<Announcement> existingAnnouncementOpt = announcementRepository.findById(announcementId);
            
            if (existingAnnouncementOpt.isPresent()) {
                Announcement existingAnnouncement = existingAnnouncementOpt.get();
                  // Update fields
                existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
                existingAnnouncement.setContent(updatedAnnouncement.getContent());
                existingAnnouncement.setPriority(updatedAnnouncement.getPriority());
                existingAnnouncement.setStatus(updatedAnnouncement.getStatus());
                existingAnnouncement.setIsPinned(updatedAnnouncement.getIsPinned());
                existingAnnouncement.setTargetAudience(updatedAnnouncement.getTargetAudience());
                if (updatedAnnouncement.getScheduledDate() != null) {
                    existingAnnouncement.setScheduledDate(updatedAnnouncement.getScheduledDate());
                }
                if (updatedAnnouncement.getExpiryDate() != null) {
                    existingAnnouncement.setExpiryDate(updatedAnnouncement.getExpiryDate());
                }
                
                Announcement savedAnnouncement = announcementRepository.save(existingAnnouncement);
                return ResponseEntity.ok(savedAnnouncement);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating announcement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Delete an announcement
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        System.out.println("Xóa thông báo ID: " + announcementId);
        
        try {
            if (announcementRepository.existsById(announcementId)) {
                announcementRepository.deleteById(announcementId);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error deleting announcement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
