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
 * Entity cho Shift Assignment - PhÃ¢n cÃ´ng ca lÃ m viá»‡c
 * Quáº£n lÃ½ viá»‡c phÃ¢n cÃ´ng ca lÃ m viá»‡c cho nhÃ¢n viÃªn
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
    @NotNull(message = "NgÃ y phÃ¢n cÃ´ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate assignmentDate;

    @Column(name = "planned_start_time", nullable = false)
    @NotNull(message = "Thá»i gian báº¯t Ä‘áº§u dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime plannedStartTime;

    @Column(name = "planned_end_time", nullable = false)
    @NotNull(message = "Thá»i gian káº¿t thÃºc dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
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
    @NotNull(message = "Sá»‘ giá» dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @DecimalMin(value = "0.25", message = "Sá»‘ giá» dá»± kiáº¿n pháº£i Ã­t nháº¥t 15 phÃºt")
    private BigDecimal plannedHours;

    @Column(name = "actual_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.00", message = "Sá»‘ giá» thá»±c táº¿ khÃ´ng Ä‘Æ°á»£c Ã¢m")
    private BigDecimal actualHours;

    @Column(name = "overtime_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.00", message = "Sá»‘ giá» tÄƒng ca khÃ´ng Ä‘Æ°á»£c Ã¢m")
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "Ghi chÃº khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
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
        SCHEDULED("ÄÃ£ lÃªn lá»‹ch"),
        IN_PROGRESS("Äang thá»±c hiá»‡n"),
        COMPLETED("HoÃ n thÃ nh"),
        CANCELLED("ÄÃ£ há»§y"),
        NO_SHOW("KhÃ´ng cÃ³ máº·t");

        private final String displayName;

        AssignmentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum AttendanceStatus {
        PENDING("Chá» xÃ¡c nháº­n"),
        PRESENT("CÃ³ máº·t"),
        ABSENT("Váº¯ng máº·t"),
        LATE("Äi muá»™n"),
        EARLY_LEAVE("Vá» sá»›m");

        private final String displayName;

        AttendanceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Business methods
    /**
     * Kiá»ƒm tra xem phÃ¢n cÃ´ng cÃ³ há»£p lá»‡ khÃ´ng
     */
    public boolean isValidAssignment() {
        return plannedStartTime != null && 
               plannedEndTime != null && 
               plannedStartTime.isBefore(plannedEndTime) &&
               assignmentDate != null;
    }

    /**
     * TÃ­nh toÃ¡n sá»‘ giá» thá»±c táº¿ lÃ m viá»‡c
     */
    public BigDecimal calculateActualHours() {
        if (actualStartTime == null || actualEndTime == null) {
            return BigDecimal.ZERO;
        }
        
        long minutes = java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
        
        // Trá»« thá»i gian nghá»‰ náº¿u cÃ³
        if (breakStartTime != null && breakEndTime != null) {
            long breakMinutes = java.time.Duration.between(breakStartTime, breakEndTime).toMinutes();
            minutes -= breakMinutes;
        }
        
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * TÃ­nh toÃ¡n sá»‘ giá» tÄƒng ca
     */
    public BigDecimal calculateOvertimeHours() {
        BigDecimal actual = calculateActualHours();
        if (actual.compareTo(plannedHours) > 0) {
            return actual.subtract(plannedHours);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Check-in cho ca lÃ m viá»‡c
     */
    public void checkIn(String location) {
        if (status != AssignmentStatus.SCHEDULED) {
            throw new IllegalStateException("Chá»‰ cÃ³ thá»ƒ check-in cho ca Ä‘Ã£ lÃªn lá»‹ch");
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
     * Check-out cho ca lÃ m viá»‡c
     */
    public void checkOut(String location) {
        if (status != AssignmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Chá»‰ cÃ³ thá»ƒ check-out cho ca Ä‘ang thá»±c hiá»‡n");
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
     * Há»§y ca lÃ m viá»‡c
     */
    public void cancel(String reason) {
        if (status == AssignmentStatus.COMPLETED) {
            throw new IllegalStateException("KhÃ´ng thá»ƒ há»§y ca Ä‘Ã£ hoÃ n thÃ nh");
        }
        
        this.status = AssignmentStatus.CANCELLED;
        this.attendanceStatus = AttendanceStatus.ABSENT;
        this.notes = (notes != null ? notes + "\n" : "") + "Há»§y: " + reason;
    }

    /**
     * Kiá»ƒm tra xem cÃ³ xung Ä‘á»™t thá»i gian vá»›i assignment khÃ¡c khÃ´ng
     */
    public boolean hasTimeConflict(ShiftAssignment other) {
        if (other == null || !assignmentDate.equals(other.assignmentDate)) {
            return false;
        }
        
        return plannedStartTime.isBefore(other.plannedEndTime) && 
               plannedEndTime.isAfter(other.plannedStartTime);
    }

    /**
     * Láº¥y thÃ´ng tin hiá»ƒn thá»‹ thá»i gian
     */
    public String getTimeRangeDisplay() {
        return String.format("%s - %s", plannedStartTime.toString(), plannedEndTime.toString());
    }

    /**
     * Láº¥y mÃ u hiá»ƒn thá»‹ theo tráº¡ng thÃ¡i
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
            throw new IllegalStateException("ThÃ´ng tin phÃ¢n cÃ´ng ca khÃ´ng há»£p lá»‡");
        }
        
        // Auto-calculate planned hours if not set
        if (plannedHours == null && plannedStartTime != null && plannedEndTime != null) {
            long minutes = java.time.Duration.between(plannedStartTime, plannedEndTime).toMinutes();
            plannedHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
