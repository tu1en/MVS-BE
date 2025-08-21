package com.classroomapp.classroombackend.model.attendancemanagement;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a makeup attendance request
 * Teachers can request to take makeup attendance for missed lectures
 */
@Entity
@Table(name = "makeup_attendance_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeupAttendanceRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    @Column(name = "reason", columnDefinition = "NVARCHAR(2000)", nullable = false)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "NVARCHAR(1000)")
    private String rejectionReason;
    
    @Column(name = "manager_notes", columnDefinition = "NVARCHAR(1000)")
    private String managerNotes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "makeupRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AttendanceSession> attendanceSessions = new ArrayList<>();
    
    /**
     * Enum for request status
     */
    public enum RequestStatus {
        PENDING,        // Chờ xác nhận
        ACKNOWLEDGED,   // Đã xác nhận
        COMPLETED,      // Hoàn thành
        REJECTED        // Từ chối
    }
    
    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }
    
    // Business logic methods
    public boolean canBeAcknowledged() {
        return status == RequestStatus.PENDING;
    }
    
    public boolean canTakeMakeupAttendance() {
        return status == RequestStatus.ACKNOWLEDGED;
    }
    
    public boolean isCompleted() {
        return status == RequestStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
    
    public boolean isAcknowledged() {
        return status == RequestStatus.ACKNOWLEDGED;
    }
    
    public void acknowledge(User manager) {
        if (!canBeAcknowledged()) {
            throw new IllegalStateException("Request cannot be acknowledged in current status: " + status);
        }
        this.status = RequestStatus.ACKNOWLEDGED;
        this.approvedBy = manager;
        this.approvedAt = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        if (!canTakeMakeupAttendance()) {
            throw new IllegalStateException("Request cannot be completed in current status: " + status);
        }
        this.status = RequestStatus.COMPLETED;
    }
    
    public void reject(User manager, String rejectionReason) {
        if (!canBeAcknowledged()) {
            throw new IllegalStateException("Request cannot be rejected in current status: " + status);
        }
        this.status = RequestStatus.REJECTED;
        this.approvedBy = manager;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }
    
    public void addManagerNotes(User manager, String notes) {
        this.managerNotes = notes;
    }
    
    // Helper methods
    public String getStatusDisplayName() {
        switch (status) {
            case PENDING:
                return "Chờ xác nhận";
            case ACKNOWLEDGED:
                return "Đã xác nhận";
            case COMPLETED:
                return "Hoàn thành";
            case REJECTED:
                return "Từ chối";
            default:
                return status.name();
        }
    }
}
