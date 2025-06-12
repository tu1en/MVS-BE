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

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationRepository notificationRepository;

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
}
