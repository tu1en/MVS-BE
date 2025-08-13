package com.classroomapp.classroombackend.model;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing SMS notifications sent to parents about student attendance
 */
@Entity
@Table(name = "sms_notifications")
public class SMSNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student; // Student (User with roleId = 1)
    
    @Column(name = "attendance_session_id")
    private Long attendanceSessionId; // Reference to attendance session
    
    @Column(name = "parent_phone", length = 20, nullable = false)
    private String parentPhone;
    
    @Column(name = "message_content", length = 500, nullable = false)
    private String messageContent;
    
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SMSStatus status = SMSStatus.PENDING;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "gateway_message_id")
    private String gatewayMessageId;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "last_retry_time")
    private LocalDateTime lastRetryTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public SMSNotification() {}

    public SMSNotification(User student, Long attendanceSessionId, String parentPhone, 
                          String messageContent, LocalDateTime sendTime) {
        this.student = student;
        this.attendanceSessionId = attendanceSessionId;
        this.parentPhone = parentPhone;
        this.messageContent = messageContent;
        this.sendTime = sendTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (retryCount == null) retryCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Long getAttendanceSessionId() {
        return attendanceSessionId;
    }

    public void setAttendanceSessionId(Long attendanceSessionId) {
        this.attendanceSessionId = attendanceSessionId;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public SMSStatus getStatus() {
        return status;
    }

    public void setStatus(SMSStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getGatewayMessageId() {
        return gatewayMessageId;
    }

    public void setGatewayMessageId(String gatewayMessageId) {
        this.gatewayMessageId = gatewayMessageId;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getLastRetryTime() {
        return lastRetryTime;
    }

    public void setLastRetryTime(LocalDateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}