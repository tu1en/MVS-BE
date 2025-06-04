package com.mvs.dto;

import com.mvs.entity.NotificationType;
import com.mvs.entity.PriorityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private NotificationType type;
    private PriorityLevel priority;
    private Long senderId;
    private String senderName;
    private Set<Long> recipientIds;
    private boolean read;
}
