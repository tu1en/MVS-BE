package com.mvs.service;

import com.mvs.dto.NotificationDTO;
import com.mvs.dto.request.CreateAnnouncementRequest;
import com.mvs.dto.request.CreateHomeworkNotificationRequest;
import com.mvs.entity.Notification;
import com.mvs.entity.NotificationType;
import com.mvs.entity.PriorityLevel;
import com.mvs.entity.User;
import com.mvs.repository.NotificationRepository;
import com.mvs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public NotificationDTO createAnnouncement(String title, String content, PriorityLevel priority, Long senderId, Set<Long> recipientIds) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        Set<User> recipients = recipientIds.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Recipient not found: " + id)))
                .collect(Collectors.toSet());

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(NotificationType.ANNOUNCEMENT)
                .priority(priority)
                .sender(sender)
                .recipients(recipients)
                .read(false)
                .build();

        notification = notificationRepository.save(notification);
        return convertToDTO(notification);
    }

    @Transactional
    public NotificationDTO createHomeworkNotification(String title, String content, Long teacherId, Set<Long> studentIds) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        Set<User> students = studentIds.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Student not found: " + id)))
                .collect(Collectors.toSet());

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(NotificationType.HOMEWORK_ASSIGNED)
                .priority(PriorityLevel.HIGH)
                .sender(teacher)
                .recipients(students)
                .read(false)
                .build();

        notification = notificationRepository.save(notification);
        return convertToDTO(notification);
    }

    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.findAllByRecipient(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (notification.getRecipients().stream().anyMatch(r -> r.getId().equals(userId))) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countUnreadByUser(user);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = modelMapper.map(notification, NotificationDTO.class);
        dto.setSenderId(notification.getSender().getId());
        dto.setSenderName(notification.getSender().getUsername());
        dto.setRecipientIds(notification.getRecipients().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}
