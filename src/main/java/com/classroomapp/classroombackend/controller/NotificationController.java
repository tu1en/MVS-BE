package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.NotificationDto;
import com.classroomapp.classroombackend.model.Notification;
import com.classroomapp.classroombackend.repository.NotificationRepository;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    
    @Autowired
    private com.classroomapp.classroombackend.repository.usermanagement.UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Task 37: Nhận thông báo cho giảng viên
    @GetMapping("/teacher")
    public ResponseEntity<List<NotificationDto>> getNotificationsForTeacher() {
        System.out.println("Yêu cầu lấy danh sách thông báo cho giảng viên.");
        List<Notification> notifications = new ArrayList<>();
        
        try {
            // Use type-based filtering for teacher notifications
            notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("TEACHER");
        } catch (Exception e) {
            System.err.println("Error fetching teacher notifications: " + e.getMessage());
            // Fallback to all notifications
            notifications = notificationRepository.findAllByOrderByCreatedAtDesc();
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(notifications));
    }
    
    // Get notifications for specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForUser(@PathVariable Long userId) {
        System.out.println("Yêu cầu lấy danh sách thông báo cho user ID: " + userId);
        List<Notification> notifications = new ArrayList<>();
        
        try {
            // Use recipientId to find notifications for the specific user
            notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            System.err.println("Error fetching user notifications: " + e.getMessage());
            // Fallback to simpler method
            notifications = notificationRepository.findByRecipientId(userId);
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(notifications));
    }
    
    // Get announcements (alias for notifications)
    @GetMapping("/announcements/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getAnnouncementsForUser(@PathVariable Long userId) {
        System.out.println("Yêu cầu lấy danh sách thông báo cho user ID: " + userId);
        List<Notification> notifications = new ArrayList<>();
        
        try {
            // Use type and recipientId to find announcement-type notifications for user
            notifications = notificationRepository.findByTypeAndRecipientId("ANNOUNCEMENT", userId);
        } catch (Exception e) {
            System.err.println("Error fetching user announcements: " + e.getMessage());
            // Fallback to all notifications for user
            notifications = notificationRepository.findByRecipientId(userId);
        }
        
        return ResponseEntity.ok(convertToNotificationDtoList(notifications));
    }

    // Đánh dấu thông báo đã đọc
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        System.out.println("Yêu cầu đánh dấu thông báo ID: " + notificationId + " là đã đọc.");
        
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                notification.setIsRead(true);
                notificationRepository.save(notification);
                return ResponseEntity.ok().build();
            } 
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    // Helper method to convert entity to DTO
    private List<NotificationDto> convertToNotificationDtoList(List<Notification> notifications) {
        List<NotificationDto> dtos = new ArrayList<>();
        for (Notification notification : notifications) {
            dtos.add(new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getIsRead(),
                notification.getSender()
            ));
        }
        return dtos;
    }

    // =============== ADMIN NOTIFICATION MANAGEMENT ENDPOINTS ===============
    
    // Create admin notification
    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdminNotification(@RequestBody AdminNotificationRequest request) {
        try {
            Notification notification = new Notification();
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            notification.setTargetAudience(request.getTargetAudience());
            notification.setTargetDetails(request.getTargetDetails());
            notification.setScheduledAt(request.getScheduledAt());
            notification.setPriority(request.getPriority() != null ? request.getPriority() : "NORMAL");
            notification.setCreatedBy(request.getCreatedBy());
            notification.setType("ADMIN_ANNOUNCEMENT");
            
            // Set status based on schedule
            if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now())) {
                notification.setStatus("PENDING");
            } else {
                notification.setStatus("SCHEDULED");
            }
            
            notification = notificationRepository.save(notification);
            
            // If not scheduled, send immediately
            if ("PENDING".equals(notification.getStatus())) {
                sendNotificationToTargets(notification);
            }
            
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating notification: " + e.getMessage());
        }
    }
    
    // Get all admin notifications
    @GetMapping("/admin/all")
    public ResponseEntity<List<Notification>> getAllAdminNotifications() {
        try {
            List<Notification> notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("ADMIN_ANNOUNCEMENT");
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Get scheduled notifications
    @GetMapping("/admin/scheduled")
    public ResponseEntity<List<Notification>> getScheduledNotifications() {
        try {
            List<Notification> notifications = notificationRepository.findByStatusOrderByScheduledAtAsc("SCHEDULED");
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Update notification
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateNotification(@PathVariable Long id, @RequestBody AdminNotificationRequest request) {
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(id);
            if (!notificationOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Notification notification = notificationOpt.get();
            
            // Only allow updating if not sent yet
            if ("SENT".equals(notification.getStatus())) {
                return ResponseEntity.badRequest().body("Cannot update notification that has already been sent");
            }
            
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            notification.setTargetAudience(request.getTargetAudience());
            notification.setTargetDetails(request.getTargetDetails());
            notification.setScheduledAt(request.getScheduledAt());
            notification.setPriority(request.getPriority());
            
            // Update status based on schedule
            if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now())) {
                notification.setStatus("PENDING");
            } else {
                notification.setStatus("SCHEDULED");
            }
            
            notification = notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating notification: " + e.getMessage());
        }
    }
    
    // Delete notification
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(id);
            if (!notificationOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Notification notification = notificationOpt.get();
            
            // Only allow deleting if not sent yet
            if ("SENT".equals(notification.getStatus())) {
                return ResponseEntity.badRequest().body("Cannot delete notification that has already been sent");
            }
            
            notificationRepository.delete(notification);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting notification: " + e.getMessage());
        }
    }
    
    // Send notification immediately
    @PostMapping("/admin/{id}/send-now")
    public ResponseEntity<?> sendNotificationNow(@PathVariable Long id) {
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(id);
            if (!notificationOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Notification notification = notificationOpt.get();
            
            if ("SENT".equals(notification.getStatus())) {
                return ResponseEntity.badRequest().body("Notification has already been sent");
            }
            
            notification.setStatus("PENDING");
            notification.setScheduledAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            sendNotificationToTargets(notification);
            
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending notification: " + e.getMessage());
        }
    }
    
    // Get notification statistics
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getNotificationStats() {
        try {
            long totalNotifications = notificationRepository.countByType("ADMIN_ANNOUNCEMENT");
            long sentNotifications = notificationRepository.countByTypeAndStatus("ADMIN_ANNOUNCEMENT", "SENT");
            long scheduledNotifications = notificationRepository.countByTypeAndStatus("ADMIN_ANNOUNCEMENT", "SCHEDULED");
            long failedNotifications = notificationRepository.countByTypeAndStatus("ADMIN_ANNOUNCEMENT", "FAILED");
            
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", totalNotifications);
            stats.put("sent", sentNotifications);
            stats.put("scheduled", scheduledNotifications);
            stats.put("failed", failedNotifications);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting stats: " + e.getMessage());
        }
    }
    
    // Helper method to send notification to targets
    private void sendNotificationToTargets(Notification notification) {
        try {
            List<Long> targetUserIds = getTargetUserIds(notification.getTargetAudience(), notification.getTargetDetails());
            
            for (Long userId : targetUserIds) {
                // Create individual notification for each user
                Notification userNotification = new Notification();
                userNotification.setTitle(notification.getTitle());
                userNotification.setContent(notification.getContent());
                userNotification.setType(notification.getType());
                userNotification.setSender("System");
                userNotification.setRecipientId(userId);
                userNotification.setPriority(notification.getPriority());
                userNotification.setStatus("SENT");
                userNotification.setCreatedBy(notification.getCreatedBy());
                
                notificationRepository.save(userNotification);
            }
            
            // Update original notification status
            notification.setStatus("SENT");
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }
    
    // Helper method to get target user IDs
    private List<Long> getTargetUserIds(String targetAudience, String targetDetails) {
        List<Long> userIds = new ArrayList<>();
        
        try {
            switch (targetAudience.toUpperCase()) {
                case "ALL":
                    // Get all active users
                    userRepository.findActiveUsers().forEach(user -> userIds.add(user.getId()));
                    break;
                case "STUDENTS":
                    // Get all students (roleId = 1)
                    userRepository.findActiveStudents().forEach(user -> userIds.add(user.getId()));
                    break;
                case "PARENTS":
                    // Get all parents (roleId = 4) - assuming parent role ID is 4
                    userRepository.findByRoleIdAndStatus(4, "active").forEach(user -> userIds.add(user.getId()));
                    break;
                case "TEACHERS":
                    // Get all teachers (roleId = 2)
                    userRepository.findActiveTeachers().forEach(user -> userIds.add(user.getId()));
                    break;
                case "ACCOUNTANTS":
                    // Get all accountants (roleId = 5)
                    userRepository.findActiveAccountants().forEach(user -> userIds.add(user.getId()));
                    break;
                case "MANAGERS":
                    // Get all managers (roleId = 3)
                    userRepository.findAllManagers().forEach(user -> userIds.add(user.getId()));
                    break;
                case "SPECIFIC_USER":
                    if (targetDetails != null) {
                        try {
                            Long userId = Long.parseLong(targetDetails);
                            userIds.add(userId);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid user ID: " + targetDetails);
                        }
                    }
                    break;
                case "SPECIFIC_CLASS":
                    if (targetDetails != null) {
                        try {
                            Long classId = Long.parseLong(targetDetails);
                            // Get students by class ID using ClassroomEnrollment
                            // This would need ClassroomEnrollmentRepository
                            // For now, we'll leave this as a placeholder
                            System.out.println("Getting students for class ID: " + classId);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid class ID: " + targetDetails);
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown target audience: " + targetAudience);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error getting target users: " + e.getMessage());
        }
        
        return userIds;
    }
    
    // Request DTO for admin notifications
    public static class AdminNotificationRequest {
        private String title;
        private String content;
        private String targetAudience;
        private String targetDetails;
        private LocalDateTime scheduledAt;
        private String priority;
        private String createdBy;
        
        // Constructors
        public AdminNotificationRequest() {}
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTargetAudience() { return targetAudience; }
        public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
        
        public String getTargetDetails() { return targetDetails; }
        public void setTargetDetails(String targetDetails) { this.targetDetails = targetDetails; }
        
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }
}
