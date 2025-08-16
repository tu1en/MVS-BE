package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.Notification;
import com.classroomapp.classroombackend.repository.NotificationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class AdminNotificationSchedulerService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    
    /**
     * Scheduled task to send notifications
     * Runs every minute to check for scheduled notifications that are ready to send
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processScheduledNotifications() {
        try {
            // Find notifications that are scheduled and ready to send
            List<Notification> scheduledNotifications = notificationRepository
                .findByStatusAndScheduledAtBefore("SCHEDULED", LocalDateTime.now());
            
            System.out.println("Found " + scheduledNotifications.size() + " scheduled notifications ready to send");
            
            for (Notification notification : scheduledNotifications) {
                try {
                    sendNotificationToTargets(notification);
                    System.out.println("Successfully sent scheduled notification: " + notification.getId());
                } catch (Exception e) {
                    System.err.println("Failed to send scheduled notification " + notification.getId() + ": " + e.getMessage());
                    notification.setStatus("FAILED");
                    notificationRepository.save(notification);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in scheduled notification processing: " + e.getMessage());
        }
    }
    
    /**
     * Send notification to target audience
     */
    private void sendNotificationToTargets(Notification notification) {
        try {
            List<Long> targetUserIds = getTargetUserIds(notification.getTargetAudience(), notification.getTargetDetails());
            
            System.out.println("Sending notification to " + targetUserIds.size() + " users");
            
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
                
                // Send real-time notification via WebSocket if available
                try {
                    if (webSocketNotificationService != null) {
                        webSocketNotificationService.sendNotification("ADMIN_NOTIFICATION", userNotification.getContent(), userNotification);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to send WebSocket notification to user " + userId + ": " + e.getMessage());
                }
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
    
    /**
     * Get target user IDs based on audience type
     */
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
                            // This would need ClassroomEnrollmentRepository - implement as needed
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
    
    /**
     * Manual method to send a specific notification immediately
     */
    public void sendNotificationNow(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
            
            if ("SENT".equals(notification.getStatus())) {
                throw new RuntimeException("Notification has already been sent");
            }
            
            notification.setStatus("PENDING");
            notification.setScheduledAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            sendNotificationToTargets(notification);
            
        } catch (Exception e) {
            System.err.println("Error sending notification immediately: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get statistics about scheduled notifications
     */
    public ScheduledNotificationStats getScheduledNotificationStats() {
        try {
            long totalScheduled = notificationRepository.countByTypeAndStatus("ADMIN_ANNOUNCEMENT", "SCHEDULED");
            long overdue = notificationRepository.findByStatusAndScheduledAtBefore("SCHEDULED", LocalDateTime.now()).size();
            long upcoming = totalScheduled - overdue;
            
            return new ScheduledNotificationStats(totalScheduled, overdue, upcoming);
        } catch (Exception e) {
            System.err.println("Error getting scheduled notification stats: " + e.getMessage());
            return new ScheduledNotificationStats(0, 0, 0);
        }
    }
    
    // Inner class for statistics
    public static class ScheduledNotificationStats {
        private long totalScheduled;
        private long overdue;
        private long upcoming;
        
        public ScheduledNotificationStats(long totalScheduled, long overdue, long upcoming) {
            this.totalScheduled = totalScheduled;
            this.overdue = overdue;
            this.upcoming = upcoming;
        }
        
        // Getters
        public long getTotalScheduled() { return totalScheduled; }
        public long getOverdue() { return overdue; }
        public long getUpcoming() { return upcoming; }
    }
}