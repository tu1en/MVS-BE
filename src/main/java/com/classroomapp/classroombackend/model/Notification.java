package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private Boolean isRead = false;
    
    @Column(columnDefinition = "NVARCHAR(255)")
    private String sender;

    @Column
    private Long recipientId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String type = "GENERAL"; // GENERAL, URGENT, SYSTEM, ANNOUNCEMENT, ADMIN_ANNOUNCEMENT

    // New fields for admin notification management
    @Column(columnDefinition = "NVARCHAR(255)")
    private String title; // Notification title
    
    @Column
    private LocalDateTime scheduledAt; // For scheduled notifications
    
    @Column(columnDefinition = "NVARCHAR(255)")
    private String targetAudience; // ALL, STUDENTS, PARENTS, TEACHERS, ACCOUNTANTS, MANAGERS, SPECIFIC_USER, SPECIFIC_CLASS

    @Column(columnDefinition = "NVARCHAR(255)")
    private String targetDetails; // Additional targeting info (class ID, user ID, etc.)

    @Column(columnDefinition = "NVARCHAR(255)")
    private String status = "PENDING"; // PENDING, SENT, SCHEDULED, FAILED

    @Column(columnDefinition = "NVARCHAR(255)")
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(columnDefinition = "NVARCHAR(255)")
    private String createdBy; // Admin who created the notification
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }
    
    public boolean isReadyToSend() {
        return "SCHEDULED".equals(status) && scheduledAt != null && scheduledAt.isBefore(LocalDateTime.now());
    }
    
    // Explicit getters and setters to resolve compilation issues
    public Long getId() { return id; }
    public String getMessage() { return content; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsRead() { return isRead; }
    public String getSender() { return sender; }
    public Long getRecipientId() { return recipientId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getTargetAudience() { return targetAudience; }
    public String getTargetDetails() { return targetDetails; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getCreatedBy() { return createdBy; }
    
    public void setId(Long id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public void setSender(String sender) { this.sender = sender; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public void setTargetDetails(String targetDetails) { this.targetDetails = targetDetails; }
    public void setStatus(String status) { this.status = status; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
