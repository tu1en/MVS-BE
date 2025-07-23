package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMessageDto {
    private Long id;
    
    @NotNull(message = "Sender ID không được để trống")
    private Long senderId;
    private String senderName;
    
    @NotNull(message = "Recipient ID không được để trống")
    private Long recipientId;
    private String recipientName;
    
    @NotBlank(message = "Chủ đề không được để trống")
    private String subject;
    
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
    
    private String messageType = "GENERAL"; // GENERAL, COMPLAINT, REQUEST, INQUIRY, URGENT
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT
    private String status = "SENT"; // SENT, READ, REPLIED, RESOLVED, ARCHIVED
    
    private Boolean isRead = false;
    private LocalDateTime readAt;
    
    private String reply;
    private LocalDateTime repliedAt;
    private Long repliedById;
    private String repliedByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit getters to resolve compilation issues
    public Long getSenderId() { return senderId; }
    public Long getRecipientId() { return recipientId; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public String getPriority() { return priority; }

    public void setId(Long id) { this.id = id; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
}
