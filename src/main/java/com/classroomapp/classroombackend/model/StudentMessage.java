package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
      @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String subject;
    
    @Column(columnDefinition = "NTEXT", nullable = false)
    private String content;
    
    @Column(length = 50)
    private String messageType = "GENERAL"; // GENERAL, COMPLAINT, REQUEST, INQUIRY, URGENT
    
    @Column(length = 50)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT
    
    @Column(length = 50)
    private String status = "SENT"; // SENT, READ, REPLIED, RESOLVED, ARCHIVED
    
    @Column
    private Boolean isRead = false;
    
    @Column
    private LocalDateTime readAt;
      @Column(columnDefinition = "NTEXT")
    private String reply;
    
    @Column
    private LocalDateTime repliedAt;
    
    @ManyToOne
    @JoinColumn(name = "replied_by")
    private User repliedBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.status = "READ";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reply(String replyContent, User replier) {
        this.reply = replyContent;
        this.repliedBy = replier;
        this.repliedAt = LocalDateTime.now();
        this.status = "REPLIED";
        this.updatedAt = LocalDateTime.now();
        if (!this.isRead) {
            markAsRead();
        }
    }
    
    public void resolve() {
        this.status = "RESOLVED";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void archive() {
        this.status = "ARCHIVED";
        this.updatedAt = LocalDateTime.now();
    }
    
    // Explicit getters to resolve compilation issues
    public Long getId() { return id; }
    public User getSender() { return sender; }
    public User getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public Boolean getIsRead() { return isRead; }
    public LocalDateTime getReadAt() { return readAt; }
    public String getReply() { return reply; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public User getRepliedBy() { return repliedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Explicit setters for compilation issues  
    public void setSender(User sender) { this.sender = sender; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setContent(String content) { this.content = content; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
