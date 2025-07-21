package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Entity cho Shift Assignment - Phân công ca làm việc
 * Quản lý việc phân công ca làm việc cho nhân viên
 */
@Entity
@Table(name = "shift_assignments", 
       indexes = {
           @Index(name = "IX_shift_assignments_employee_date", columnList = "employee_id, assignment_date"),
           @Index(name = "IX_shift_assignments_date", columnList = "assignment_date"),
           @Index(name = "IX_shift_assignments_status", columnList = "status"),
           @Index(name = "IX_shift_assignments_attendance", columnList = "attendance_status"),
           @Index(name = "IX_shift_assignments_schedule", columnList = "schedule_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "IX_shift_assignments_unique", 
                           columnNames = {"employee_id", "assignment_date", "planned_start_time"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_date", nullable = false)
    @NotNull(message = "Ngày phân công không được để trống")
    private LocalDate assignmentDate;

    @Column(name = "planned_start_time", nullable = false)
    @NotNull(message = "Thời gian bắt đầu dự kiến không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime plannedStartTime;

    @Column(name = "planned_end_time", nullable = false)
    @NotNull(message = "Thời gian kết thúc dự kiến không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime plannedEndTime;

    @Column(name = "actual_start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime actualStartTime;

    @Column(name = "actual_end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime actualEndTime;

    @Column(name = "break_start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private AssignmentStatus status = AssignmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", length = 50, nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.PENDING;

    @Column(name = "planned_hours", nullable = false, precision = 4, scale = 2)
    @NotNull(message = "Số giờ dự kiến không được để trống")
    @DecimalMin(value = "0.25", message = "Số giờ dự kiến phải ít nhất 15 phút")
    private BigDecimal plannedHours;

    @Column(name = "actual_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.00", message = "Số giờ thực tế không được âm")
    private BigDecimal actualHours;

    @Column(name = "overtime_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.00", message = "Số giờ tăng ca không được âm")
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "location_check_in", columnDefinition = "NVARCHAR(MAX)")
    private String locationCheckIn; // JSON for GPS coordinates

    @Column(name = "location_check_out", columnDefinition = "NVARCHAR(MAX)")
    private String locationCheckOut; // JSON for GPS coordinates

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "FK_shift_assignments_employee"))
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false, foreignKey = @ForeignKey(name = "FK_shift_assignments_template"))
    private ShiftTemplate shiftTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", foreignKey = @ForeignKey(name = "FK_shift_assignments_schedule"))
    private ShiftSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false, foreignKey = @ForeignKey(name = "FK_shift_assignments_assigned_by"))
    private User assignedBy;

    @OneToMany(mappedBy = "requesterAssignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShiftSwapRequest> swapRequestsAsRequester;

    @OneToMany(mappedBy = "targetAssignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShiftSwapRequest> swapRequestsAsTarget;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum AssignmentStatus {
        SCHEDULED("Đã lên lịch"),
        IN_PROGRESS("Đang thực hiện"),
        COMPLETED("Hoàn thành"),
        CANCELLED("Đã hủy"),
        NO_SHOW("Không có mặt");

        private final String displayName;

        AssignmentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum AttendanceStatus {
        PENDING("Chờ xác nhận"),
        PRESENT("Có mặt"),
        ABSENT("Vắng mặt"),
        LATE("Đi muộn"),
        EARLY_LEAVE("Về sớm");

        private final String displayName;

        AttendanceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Business methods
    /**
     * Kiểm tra xem phân công có hợp lệ không
     */
    public boolean isValidAssignment() {
        return plannedStartTime != null && 
               plannedEndTime != null && 
               plannedStartTime.isBefore(plannedEndTime) &&
               assignmentDate != null;
    }

    /**
     * Tính toán số giờ thực tế làm việc
     */
    public BigDecimal calculateActualHours() {
        if (actualStartTime == null || actualEndTime == null) {
            return BigDecimal.ZERO;
        }
        
        long minutes = java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
        
        // Trừ thời gian nghỉ nếu có
        if (breakStartTime != null && breakEndTime != null) {
            long breakMinutes = java.time.Duration.between(breakStartTime, breakEndTime).toMinutes();
            minutes -= breakMinutes;
        }
        
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Tính toán số giờ tăng ca
     */
    public BigDecimal calculateOvertimeHours() {
        BigDecimal actual = calculateActualHours();
        if (actual.compareTo(plannedHours) > 0) {
            return actual.subtract(plannedHours);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Check-in cho ca làm việc
     */
    public void checkIn(String location) {
        if (status != AssignmentStatus.SCHEDULED) {
            throw new IllegalStateException("Chỉ có thể check-in cho ca đã lên lịch");
        }
        
        this.checkInTime = LocalDateTime.now();
        this.actualStartTime = checkInTime.toLocalTime();
        this.locationCheckIn = location;
        this.status = AssignmentStatus.IN_PROGRESS;
        
        // Determine attendance status based on planned time
        LocalDateTime plannedDateTime = assignmentDate.atTime(plannedStartTime);
        if (checkInTime.isAfter(plannedDateTime.plusMinutes(15))) {
            this.attendanceStatus = AttendanceStatus.LATE;
        } else {
            this.attendanceStatus = AttendanceStatus.PRESENT;
        }
    }

    /**
     * Check-out cho ca làm việc
     */
    public void checkOut(String location) {
        if (status != AssignmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Chỉ có thể check-out cho ca đang thực hiện");
        }
        
        this.checkOutTime = LocalDateTime.now();
        this.actualEndTime = checkOutTime.toLocalTime();
        this.locationCheckOut = location;
        this.status = AssignmentStatus.COMPLETED;
        
        // Calculate actual hours and overtime
        this.actualHours = calculateActualHours();
        this.overtimeHours = calculateOvertimeHours();
        
        // Check for early leave
        LocalDateTime plannedEndDateTime = assignmentDate.atTime(plannedEndTime);
        if (checkOutTime.isBefore(plannedEndDateTime.minusMinutes(15))) {
            this.attendanceStatus = AttendanceStatus.EARLY_LEAVE;
        }
    }

    /**
     * Hủy ca làm việc
     */
    public void cancel(String reason) {
        if (status == AssignmentStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy ca đã hoàn thành");
        }
        
        this.status = AssignmentStatus.CANCELLED;
        this.attendanceStatus = AttendanceStatus.ABSENT;
        this.notes = (notes != null ? notes + "\n" : "") + "Hủy: " + reason;
    }

    /**
     * Kiểm tra xem có xung đột thời gian với assignment khác không
     */
    public boolean hasTimeConflict(ShiftAssignment other) {
        if (other == null || !assignmentDate.equals(other.assignmentDate)) {
            return false;
        }
        
        return plannedStartTime.isBefore(other.plannedEndTime) && 
               plannedEndTime.isAfter(other.plannedStartTime);
    }

    /**
     * Lấy thông tin hiển thị thời gian
     */
    public String getTimeRangeDisplay() {
        return String.format("%s - %s", plannedStartTime.toString(), plannedEndTime.toString());
    }

    /**
     * Lấy màu hiển thị theo trạng thái
     */
    public String getStatusColor() {
        switch (status) {
            case SCHEDULED: return "#1890ff";
            case IN_PROGRESS: return "#52c41a";
            case COMPLETED: return "#722ed1";
            case CANCELLED: return "#ff4d4f";
            case NO_SHOW: return "#fa8c16";
            default: return "#d9d9d9";
        }
    }

    @PrePersist
    @PreUpdate
    private void validateEntity() {
        if (!isValidAssignment()) {
            throw new IllegalStateException("Thông tin phân công ca không hợp lệ");
        }
        
        // Auto-calculate planned hours if not set
        if (plannedHours == null && plannedStartTime != null && plannedEndTime != null) {
            long minutes = java.time.Duration.between(plannedStartTime, plannedEndTime).toMinutes();
            plannedHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
