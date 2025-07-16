package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Entity representing attendance violations detected by the system
 */
@Entity
@Table(name = "attendance_violations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Người dùng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_assignment_id")
    private UserShiftAssignment shiftAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id")
    private StaffAttendanceLog attendanceLog;

    @NotNull(message = "Ngày vi phạm không được để trống")
    @Column(name = "violation_date", nullable = false)
    private LocalDate violationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    private ViolationType violationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private ViolationSeverity severity = ViolationSeverity.MINOR;

    @Column(name = "expected_time")
    private LocalTime expectedTime;

    @Column(name = "actual_time")
    private LocalTime actualTime;

    @Column(name = "deviation_minutes")
    private Integer deviationMinutes;

    @Column(name = "system_description", columnDefinition = "NVARCHAR(1000)")
    private String systemDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ViolationStatus status = ViolationStatus.PENDING_EXPLANATION;

    @Column(name = "auto_detected", columnDefinition = "BIT DEFAULT 1")
    private Boolean autoDetected = true;

    @Column(name = "detection_time")
    private LocalDateTime detectionTime;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "NVARCHAR(1000)")
    private String resolutionNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-many relationship with explanations
    @OneToMany(mappedBy = "violation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ViolationExplanation> explanations;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (detectionTime == null) {
            detectionTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum for violation types
     */
    public enum ViolationType {
        LATE_ARRIVAL("Đi trễ"),
        EARLY_DEPARTURE("Về sớm"),
        MISSING_CHECK_IN("Thiếu chấm công vào"),
        MISSING_CHECK_OUT("Thiếu chấm công ra"),
        ABSENT_WITHOUT_LEAVE("Vắng không phép"),
        OVERTIME_WITHOUT_APPROVAL("Làm thêm giờ không được duyệt"),
        WRONG_LOCATION("Chấm công sai địa điểm");

        private final String description;

        ViolationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enum for violation severity
     */
    public enum ViolationSeverity {
        MINOR("Nhẹ"),
        MODERATE("Trung bình"),
        MAJOR("Nghiêm trọng"),
        CRITICAL("Rất nghiêm trọng");

        private final String description;

        ViolationSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enum for violation status
     */
    public enum ViolationStatus {
        PENDING_EXPLANATION("Cần giải trình"),
        EXPLANATION_SUBMITTED("Đã gửi giải trình"),
        UNDER_REVIEW("Đang xem xét"),
        APPROVED("Đã duyệt"),
        REJECTED("Bị từ chối"),
        RESOLVED("Đã giải quyết"),
        ESCALATED("Đã chuyển lên cấp trên");

        private final String description;

        ViolationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Check if violation needs explanation
     * @return true if status requires explanation
     */
    public boolean needsExplanation() {
        return status == ViolationStatus.PENDING_EXPLANATION;
    }

    /**
     * Check if violation is resolved
     * @return true if violation is resolved
     */
    public boolean isResolved() {
        return status == ViolationStatus.RESOLVED || status == ViolationStatus.APPROVED;
    }

    /**
     * Check if violation can be explained
     * @return true if violation can still be explained
     */
    public boolean canBeExplained() {
        return status == ViolationStatus.PENDING_EXPLANATION || 
               status == ViolationStatus.REJECTED;
    }

    /**
     * Get violation description with details
     * @return formatted violation description
     */
    public String getDetailedDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(violationType.getDescription());
        
        if (deviationMinutes != null && deviationMinutes > 0) {
            desc.append(" (").append(deviationMinutes).append(" phút)");
        }
        
        if (expectedTime != null && actualTime != null) {
            desc.append(" - Dự kiến: ").append(expectedTime)
                .append(", Thực tế: ").append(actualTime);
        }
        
        return desc.toString();
    }

    /**
     * Calculate days since violation occurred
     * @return number of days
     */
    public long getDaysSinceViolation() {
        return violationDate.until(LocalDate.now()).getDays();
    }

    /**
     * Check if violation is overdue for explanation
     * @param maxDays maximum days allowed for explanation
     * @return true if overdue
     */
    public boolean isOverdueForExplanation(int maxDays) {
        return needsExplanation() && getDaysSinceViolation() > maxDays;
    }
}
