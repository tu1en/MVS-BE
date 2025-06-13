package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    private Long id;
    private String title;
    private String content;
    private Long classroomId;
    private String classroomName;
    private Long createdBy;
    private String creatorName;
    private String targetAudience;
    private String priority;
    private LocalDateTime scheduledDate;
    private LocalDateTime expiryDate;
    private Boolean isPinned;
    private Integer attachmentsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private List<AnnouncementAttachmentDto> attachments;
    private Boolean isRead;
    private Long readCount;
}
