package com.classroomapp.classroombackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing parent leave notices for students
 * Core feature from PARENT_ROLE_SPEC.md - handles FULL_DAY/LATE/EARLY notifications
 */
@Entity
@Table(name = "parent_leave_notice")
@Data
@NoArgsConstructor
@ToString(exclude = {"parent", "student", "acknowledgedByUser"})
public class ParentLeaveNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NoticeType type;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "arrive_at")
    private LocalTime arriveAt; // For LATE type

    @Column(name = "leave_at")
    private LocalTime leaveAt; // For EARLY type

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    private ReasonCode reasonCode;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "attachments", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> attachments; // File paths/URLs as JSON array

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NoticeStatus status = NoticeStatus.SENT;

    @Column(name = "ack_at")
    private LocalDateTime ackAt;

    @Column(name = "ack_by_user_id")
    private Long ackByUserId; // Teacher/staff who acknowledged

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonBackReference
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonBackReference
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ack_by_user_id", insertable = false, updatable = false)
    @JsonBackReference
    private User acknowledgedByUser;

    // Constructors

    public ParentLeaveNotice(Long parentId, Long studentId, NoticeType type, 
                           LocalDate date, ReasonCode reasonCode, String note) {
        this.parentId = parentId;
        this.studentId = studentId;
        this.type = type;
        this.date = date;
        this.reasonCode = reasonCode;
        this.note = note;
        this.status = NoticeStatus.SENT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor for LATE type
    public ParentLeaveNotice(Long parentId, Long studentId, LocalDate date, 
                           LocalTime arriveTime, ReasonCode reasonCode, String note) {
        this(parentId, studentId, NoticeType.LATE, date, reasonCode, note);
        this.arriveAt = arriveTime;
    }

    // Constructor for EARLY type
    public ParentLeaveNotice(Long parentId, Long studentId, LocalDate date, 
                           ReasonCode reasonCode, String note, LocalTime leaveTime) {
        this(parentId, studentId, NoticeType.EARLY, date, reasonCode, note);
        this.leaveAt = leaveTime;
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateNoticeData();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Validate notice data based on type
     */
    private void validateNoticeData() {
        switch (type) {
            case LATE:
                if (arriveAt == null) {
                    throw new IllegalStateException("LATE notice must have arriveAt time");
                }
                if (leaveAt != null) {
                    throw new IllegalStateException("LATE notice cannot have leaveAt time");
                }
                break;
            case EARLY:
                if (leaveAt == null) {
                    throw new IllegalStateException("EARLY notice must have leaveAt time");
                }
                if (arriveAt != null) {
                    throw new IllegalStateException("EARLY notice cannot have arriveAt time");
                }
                break;
            case FULL_DAY:
                if (arriveAt != null || leaveAt != null) {
                    throw new IllegalStateException("FULL_DAY notice cannot have arriveAt or leaveAt times");
                }
                break;
        }
    }

    /**
     * Acknowledge this notice by a teacher/staff
     */
    public void acknowledge(Long acknowledgedByUserId) {
        this.ackByUserId = acknowledgedByUserId;
        this.ackAt = LocalDateTime.now();
        this.status = NoticeStatus.ACKNOWLEDGED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if notice is acknowledged
     */
    public boolean isAcknowledged() {
        return NoticeStatus.ACKNOWLEDGED.equals(this.status);
    }

    /**
     * Check if notice is for today
     */
    public boolean isForToday() {
        return LocalDate.now().equals(this.date);
    }

    /**
     * Check if notice is for future date
     */
    public boolean isFutureNotice() {
        return this.date.isAfter(LocalDate.now());
    }

    /**
     * Get display text for notice type in Vietnamese
     */
    public String getTypeDisplayName() {
        return switch (this.type) {
            case FULL_DAY -> "Nghỉ cả ngày";
            case LATE -> "Đến muộn";
            case EARLY -> "Về sớm";
        };
    }

    /**
     * Get display text for reason in Vietnamese
     */
    public String getReasonDisplayName() {
        return switch (this.reasonCode) {
            case SICK -> "Ốm đau";
            case FAMILY -> "Việc gia đình";
            case APPOINTMENT -> "Có hẹn";
            case EMERGENCY -> "Khẩn cấp";
            case OTHER -> "Khác";
        };
    }

    /**
     * Get time range string for display
     */
    public String getTimeRangeDisplay() {
        return switch (this.type) {
            case FULL_DAY -> "Cả ngày";
            case LATE -> "Đến lúc " + (arriveAt != null ? arriveAt.toString() : "");
            case EARLY -> "Về từ " + (leaveAt != null ? leaveAt.toString() : "");
        };
    }

    // Enums

    public enum NoticeType {
        FULL_DAY, // Nghỉ cả ngày
        LATE,     // Đến muộn
        EARLY     // Về sớm
    }

    public enum ReasonCode {
        SICK,       // Ốm đau
        FAMILY,     // Việc gia đình
        APPOINTMENT,// Có hẹn
        EMERGENCY,  // Khẩn cấp
        OTHER       // Khác
    }

    public enum NoticeStatus {
        SENT,         // Đã gửi
        DELIVERED,    // Đã chuyển đến
        ACKNOWLEDGED  // Đã xác nhận
    }
}