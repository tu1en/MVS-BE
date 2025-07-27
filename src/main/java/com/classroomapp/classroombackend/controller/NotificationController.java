package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.NotificationDto;
import com.classroomapp.classroombackend.model.Notification;
import com.classroomapp.classroombackend.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * ✅ CREATE NOTIFICATION - POST /api/notifications
     * This is the missing endpoint that was causing 404 errors
     */
    @PostMapping
    @PreAuthorize("permitAll()") // Allow all users to create notifications for now
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody CreateNotificationRequest request) {
        try {
            log.info("Creating notification: title={}, content={}, targetAudience={}", 
                    request.getTitle(), request.getContent(), request.getTargetAudience());
            
            // Validate request
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Title is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Create notification entity
            Notification notification = new Notification();
            notification.setMessage(request.getContent() != null ? request.getContent() : request.getTitle());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            notification.setSender("System"); // TODO: Get from SecurityContext in production
            
            // Set type based on targetAudience
            if ("students".equals(request.getTargetAudience())) {
                notification.setType("STUDENT");
            } else if ("teachers".equals(request.getTargetAudience())) {
                notification.setType("TEACHER");
            } else {
                notification.setType("ANNOUNCEMENT");
            }
            
            // Set recipientId if provided
            if (request.getRecipientId() != null) {
                notification.setRecipientId(request.getRecipientId());
            }
            
            // Save notification to database
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification created successfully with ID: {}", savedNotification.getId());
            
            // Build success response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification created successfully");
            response.put("data", Map.of(
                "id", savedNotification.getId(),
                "title", request.getTitle(),
                "content", request.getContent(),
                "targetAudience", request.getTargetAudience(),
                "type", savedNotification.getType(),
                "createdAt", savedNotification.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to create notification: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to create notification: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get notifications for teachers
     * Task 37: Nhận thông báo cho giảng viên
     */
    @GetMapping("/teacher")
    @PreAuthorize("permitAll()") // Allow for testing
    public ResponseEntity<List<NotificationDto>> getNotificationsForTeacher() {
        log.info("Getting notifications for teachers");
        List<Notification> notifications = new ArrayList<>();
        
        try {
            // Use type-based filtering for teacher notifications
            notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("TEACHER");
            
            // If no teacher-specific notifications, get announcements
            if (notifications.isEmpty()) {
                notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("ANNOUNCEMENT");
            }
            
            log.info("Found {} notifications for teachers", notifications.size());
            
        } catch (Exception e) {
            log.error("Error fetching teacher notifications: {}", e.getMessage());
            // Fallback to all notifications
            try {
                notifications = notificationRepository.findAllByOrderByCreatedAtDesc();
                log.info("Fallback: Retrieved {} total notifications", notifications.size());
            } catch (Exception fallbackError) {
                log.error("Fallback also failed: {}", fallbackError.getMessage());
                notifications = new ArrayList<>();
            }
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(notifications));
    }
    
    /**
     * Get notifications for specific user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("permitAll()") // Allow for testing
    public ResponseEntity<List<NotificationDto>> getNotificationsForUser(@PathVariable Long userId) {
        log.info("Getting notifications for user ID: {}", userId);
        List<Notification> notifications = new ArrayList<>();
        
        try {
            // Use recipientId to find notifications for the specific user
            notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
            
            // If no user-specific notifications, get general announcements
            if (notifications.isEmpty()) {
                notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("ANNOUNCEMENT");
            }
            
            log.info("Found {} notifications for user {}", notifications.size(), userId);
            
        } catch (Exception e) {
            log.error("Error fetching user notifications: {}", e.getMessage());
            // Fallback to simpler method
            try {
                notifications = notificationRepository.findByRecipientId(userId);
                log.info("Fallback: Found {} notifications for user {}", notifications.size(), userId);
            } catch (Exception fallbackError) {
                log.error("Fallback also failed: {}", fallbackError.getMessage());
                notifications = new ArrayList<>();
            }
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(notifications));
    }
    
    /**
     * Get announcements for specific user (alias for notifications)
     */
    @GetMapping("/announcements/user/{userId}")
    @PreAuthorize("permitAll()") // Allow for testing
    public ResponseEntity<List<NotificationDto>> getAnnouncementsForUser(@PathVariable Long userId) {
        log.info("Getting announcements for user ID: {}", userId);
        List<Notification> notifications = new ArrayList<>();
        
        try {
            // Use type and recipientId to find announcement-type notifications for user
            notifications = notificationRepository.findByTypeAndRecipientId("ANNOUNCEMENT", userId);
            
            // If no user-specific announcements, get all announcements
            if (notifications.isEmpty()) {
                notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("ANNOUNCEMENT");
            }
            
            log.info("Found {} announcements for user {}", notifications.size(), userId);
            
        } catch (Exception e) {
            log.error("Error fetching user announcements: {}", e.getMessage());
            // Fallback to all notifications for user
            try {
                notifications = notificationRepository.findByRecipientId(userId);
                log.info("Fallback: Found {} notifications for user {}", notifications.size(), userId);
            } catch (Exception fallbackError) {
                log.error("Fallback also failed: {}", fallbackError.getMessage());
                notifications = new ArrayList<>();
            }
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(notifications));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("permitAll()") // Allow for testing
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable Long notificationId) {
        log.info("Marking notification ID: {} as read", notificationId);
        
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                notification.setIsRead(true);
                notificationRepository.save(notification);
                
                log.info("Notification {} marked as read successfully", notificationId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Notification marked as read");
                response.put("notificationId", notificationId);
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("Notification {} not found", notificationId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Notification not found");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to mark notification as read: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all notifications (for debugging)
     */
    @GetMapping("/all")
    @PreAuthorize("permitAll()") // Allow for testing
    public ResponseEntity<Map<String, Object>> getAllNotifications() {
        try {
            log.info("Getting all notifications for debugging");
            List<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("total", notifications.size());
            response.put("data", convertToNotificationDtoList(notifications));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all notifications: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get notifications: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Helper method to convert entities to DTOs
     */
    private List<NotificationDto> convertToNotificationDtoList(List<Notification> notifications) {
        List<NotificationDto> dtos = new ArrayList<>();
        for (Notification notification : notifications) {
            try {
                NotificationDto dto = new NotificationDto(
                    notification.getId(),
                    notification.getMessage(),
                    notification.getCreatedAt(),
                    notification.getIsRead(),
                    notification.getSender()
                );
                dtos.add(dto);
            } catch (Exception e) {
                log.warn("Failed to convert notification {} to DTO: {}", notification.getId(), e.getMessage());
                // Skip this notification and continue with others
            }
        }
        return dtos;
    }

    /**
     * DTO class for creating notifications
     */
    public static class CreateNotificationRequest {
        private String title;
        private String content;
        private String targetAudience;
        private Long recipientId;
        private String type;
        
        // Default constructor
        public CreateNotificationRequest() {}
        
        // Constructor with main fields
        public CreateNotificationRequest(String title, String content, String targetAudience) {
            this.title = title;
            this.content = content;
            this.targetAudience = targetAudience;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTargetAudience() { return targetAudience; }
        public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
        
        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        @Override
        public String toString() {
            return "CreateNotificationRequest{" +
                    "title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", targetAudience='" + targetAudience + '\'' +
                    ", recipientId=" + recipientId +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}