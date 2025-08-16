package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing messages between parents and teachers
 * Based on PARENT_ROLE_SPEC.md requirements for 1-1 communication
 */
@Entity
@Table(name = "parent_messages")
@Data
@NoArgsConstructor
@ToString(exclude = {"parent", "teacher", "student", "replyTo"})
public class ParentMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "student_id", nullable = false)
    private Long studentId; // Context student

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Nationalized
    @Column(name = "subject", columnDefinition = "NVARCHAR(255)")
    private String subject;

    @Column(name = "message_content", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String messageContent;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "reply_to_id")
    private Long replyToId; // For threading messages

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonBackReference
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    @JsonBackReference
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonBackReference
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id", insertable = false, updatable = false)
    @JsonBackReference
    private ParentMessage replyTo;

    // Constructors

    public ParentMessage(Long parentId, Long teacherId, Long studentId, 
                        SenderType senderType, String subject, String messageContent) {
        this.parentId = parentId;
        this.teacherId = teacherId;
        this.studentId = studentId;
        this.senderType = senderType;
        this.subject = subject;
        this.messageContent = messageContent;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for reply messages
    public ParentMessage(Long parentId, Long teacherId, Long studentId, 
                        SenderType senderType, String subject, String messageContent, Long replyToId) {
        this(parentId, teacherId, studentId, senderType, subject, messageContent);
        this.replyToId = replyToId;
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Mark message as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Check if message is read
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(this.isRead);
    }

    /**
     * Check if this is a reply message
     */
    public boolean isReply() {
        return this.replyToId != null;
    }

    /**
     * Check if sender is parent
     */
    public boolean isSentByParent() {
        return SenderType.PARENT.equals(this.senderType);
    }

    /**
     * Check if sender is teacher
     */
    public boolean isSentByTeacher() {
        return SenderType.TEACHER.equals(this.senderType);
    }

    /**
     * Check if sender is student
     */
    public boolean isSentByStudent() {
        return SenderType.STUDENT.equals(this.senderType);
    }

    /**
     * Get sender display name
     */
    public String getSenderDisplayName() {
        return switch (this.senderType) {
            case PARENT -> "Phụ huynh";
            case TEACHER -> "Giáo viên";
            case STUDENT -> "Học sinh";
        };
    }

    /**
     * Get message preview (first 100 characters)
     */
    public String getMessagePreview() {
        if (messageContent == null) return "";
        return messageContent.length() > 100 ? 
            messageContent.substring(0, 100) + "..." : 
            messageContent;
    }

    /**
     * Check if message is recent (within last 24 hours)
     */
    public boolean isRecent() {
        return createdAt != null && 
               createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    public enum SenderType {
        PARENT,  // Phụ huynh
        TEACHER, // Giáo viên
        STUDENT  // Học sinh
    }
}