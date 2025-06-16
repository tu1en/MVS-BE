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
    
    @Column(nullable = false, length = 255)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
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
    
    @Column(columnDefinition = "TEXT")
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
}
