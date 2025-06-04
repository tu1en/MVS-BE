package com.mvs.controller;

import com.mvs.dto.NotificationDTO;
import com.mvs.dto.request.CreateAnnouncementRequest;
import com.mvs.dto.request.CreateHomeworkNotificationRequest;
import com.mvs.entity.User;
import com.mvs.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/announcements")
    public ResponseEntity<NotificationDTO> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request,
            Authentication authentication) {
        Long senderId = getUserIdFromAuth(authentication);
        NotificationDTO notification = notificationService.createAnnouncement(
                request.getTitle(),
                request.getContent(),
                request.getPriority(),
                senderId,
                request.getRecipientIds()
        );
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/homework")
    public ResponseEntity<NotificationDTO> createHomeworkNotification(
            @Valid @RequestBody CreateHomeworkNotificationRequest request,
            Authentication authentication) {
        Long teacherId = getUserIdFromAuth(authentication);
        NotificationDTO notification = notificationService.createHomeworkNotification(
                request.getTitle(),
                request.getContent(),
                teacherId,
                request.getStudentIds()
        );
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new RuntimeException("User not authenticated");
    }
}
