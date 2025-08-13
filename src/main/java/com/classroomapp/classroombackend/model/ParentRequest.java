package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Entity representing parent requests for student leave/late/early departure
 */
@Entity
@Table(name = "parent_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;
    
    @Column(name = "parent_name", columnDefinition = "NVARCHAR(255)")
    private String parentName;
    
    @Column(name = "parent_phone", length = 20)
    private String parentPhone;
    
    @Column(name = "parent_email", length = 100)
    private String parentEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;
    
    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;
    
    @Column(name = "start_time")
    private String startTime; // For late arrival or early departure
    
    @Column(name = "end_time")
    private String endTime; // For early departure or multiple days
    
    @Column(name = "reason", columnDefinition = "NVARCHAR(1000)")
    private String reason;
    
    @Column(name = "supporting_document_url")
    private String supportingDocumentUrl; // URL to uploaded document if any
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "approved_by_teacher_id")
    private Long approvedByTeacherId;
    
    @Column(name = "approved_by_assistant_id")
    private Long approvedByAssistantId;
    
    @Column(name = "teacher_response", columnDefinition = "NVARCHAR(500)")
    private String teacherResponse;
    
    @Column(name = "assistant_response", columnDefinition = "NVARCHAR(500)")
    private String assistantResponse;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "teacher_notified", nullable = false)
    private Boolean teacherNotified = false;
    
    @Column(name = "assistant_notified", nullable = false)
    private Boolean assistantNotified = false;
    
    // Enums
    public enum RequestType {
        LEAVE,          // Nghỉ học
        LATE_ARRIVAL,   // Đi muộn
        EARLY_DEPARTURE // Về sớm
    }
    
    public enum RequestStatus {
        PENDING,        // Chờ xử lý
        APPROVED,       // Đã duyệt
        REJECTED,       // Từ chối
        EXPIRED         // Hết hạn
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
        if (teacherNotified == null) {
            teacherNotified = false;
        }
        if (assistantNotified == null) {
            assistantNotified = false;
        }
    }
    
    // Helper methods
    public boolean isExpired() {
        return requestDate.isBefore(LocalDate.now()) && status == RequestStatus.PENDING;
    }
    
    public boolean needsApproval() {
        return status == RequestStatus.PENDING && !isExpired();
    }
    
    public String getRequestTypeDisplayName() {
        switch (requestType) {
            case LEAVE: return "Nghỉ học";
            case LATE_ARRIVAL: return "Đi muộn";
            case EARLY_DEPARTURE: return "Về sớm";
            default: return requestType.toString();
        }
    }
    
    public String getStatusDisplayName() {
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case APPROVED: return "Đã duyệt";
            case REJECTED: return "Từ chối";
            case EXPIRED: return "Hết hạn";
            default: return status.toString();
        }
    }
}