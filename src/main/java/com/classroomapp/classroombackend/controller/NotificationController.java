package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.NotificationDto;
import com.classroomapp.classroombackend.model.Notification;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.NotificationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Task 37: Nhận thông báo cho giảng viên
    @GetMapping("/teacher")
    public ResponseEntity<List<NotificationDto>> getNotificationsForTeacher() {
        System.out.println("Yêu cầu lấy danh sách thông báo cho giảng viên.");
        List<Notification> notifications = new ArrayList<>();

        try {
            // Get all announcements and general notifications that teachers should see
            // Teachers should see ANNOUNCEMENT type notifications (created by AnnouncementSeeder)
            notifications = notificationRepository.findByTypeOrderByCreatedAtDesc("ANNOUNCEMENT");

            // If no announcements found, also include general notifications
            if (notifications.isEmpty()) {
                List<Notification> generalNotifications = notificationRepository.findByTypeOrderByCreatedAtDesc("GENERAL");
                notifications.addAll(generalNotifications);
            }
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

    // Get notifications based on user role
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByRole(@PathVariable Integer roleId) {
        System.out.println("Yêu cầu lấy danh sách thông báo cho role ID: " + roleId);
        List<Notification> notifications = new ArrayList<>();

        try {
            // Get users with this role
            List<User> usersWithRole = userRepository.findByRoleId(roleId);

            if (!usersWithRole.isEmpty()) {
                // Get all user IDs with this role
                List<Long> userIds = usersWithRole.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

                // Get all notifications for these users
                for (Long userId : userIds) {
                    List<Notification> userNotifications = notificationRepository.findByRecipientId(userId);
                    notifications.addAll(userNotifications);
                }

                // Sort by creation date (newest first)
                notifications.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
            }
        } catch (Exception e) {
            System.err.println("Error fetching role notifications: " + e.getMessage());
            e.printStackTrace();
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
}
