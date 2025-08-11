package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.Nationalized;

import com.classroomapp.classroombackend.model.usermanagement.User;

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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing staff attendance logs for HR management
 * This is separate from student attendance system
 */
@Entity
@Table(name = "staff_attendance_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "NgÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "NgÃ y cháº¥m cÃ´ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type", nullable = false)
    private AttendanceType attendanceType = AttendanceType.NORMAL;

    @Column(name = "notes", columnDefinition = "NVARCHAR(1000)")
    @Nationalized
    private String notes;

    @Column(name = "location_info", columnDefinition = "NVARCHAR(500)")
    @Nationalized
    private String locationInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info", columnDefinition = "NVARCHAR(500)")
    @Nationalized
    private String deviceInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum for attendance types
     */
    public enum AttendanceType {
        NORMAL,     // Cháº¥m cÃ´ng bÃ¬nh thÆ°á»ng
        OVERTIME,   // LÃ m thÃªm giá»
        HOLIDAY,    // LÃ m ngÃ y lá»…
        WEEKEND,    // LÃ m cuá»‘i tuáº§n
        REMOTE      // LÃ m viá»‡c tá»« xa
    }

    /**
     * Calculate total working hours for the day
     * @return working hours as double, 0 if incomplete data
     */
    public double getWorkingHours() {
        if (checkInTime == null || checkOutTime == null) {
            return 0.0;
        }
        
        long seconds = checkOutTime.toSecondOfDay() - checkInTime.toSecondOfDay();
        return seconds / 3600.0; // Convert to hours
    }

    /**
     * Check if this is a complete attendance record
     * @return true if both check-in and check-out are recorded
     */
    public boolean isComplete() {
        return checkInTime != null && checkOutTime != null;
    }

    /**
     * Check if user checked in late based on shift assignment
     * @param expectedStartTime expected start time from shift
     * @param toleranceMinutes tolerance in minutes
     * @return true if late
     */
    public boolean isLate(LocalTime expectedStartTime, int toleranceMinutes) {
        if (checkInTime == null || expectedStartTime == null) {
            return false;
        }
        
        LocalTime toleranceTime = expectedStartTime.plusMinutes(toleranceMinutes);
        return checkInTime.isAfter(toleranceTime);
    }

    /**
     * Check if user left early based on shift assignment
     * @param expectedEndTime expected end time from shift
     * @param toleranceMinutes tolerance in minutes
     * @return true if left early
     */
    public boolean isEarly(LocalTime expectedEndTime, int toleranceMinutes) {
        if (checkOutTime == null || expectedEndTime == null) {
            return false;
        }

        LocalTime toleranceTime = expectedEndTime.minusMinutes(toleranceMinutes);
        return checkOutTime.isBefore(toleranceTime);
    }

    /**
     * Check if this is a late arrival (alias for isLate with default tolerance)
     * @return true if arrived late (more than 15 minutes after expected start)
     */
    public boolean isLateArrival() {
        // This would typically need shift information, but for now return false
        // In a real implementation, this would check against the user's assigned shift
        return false;
    }

    /**
     * Check if this is an early departure (alias for isEarly with default tolerance)
     * @return true if left early (more than 15 minutes before expected end)
     */
    public boolean isEarlyDeparture() {
        // This would typically need shift information, but for now return false
        // In a real implementation, this would check against the user's assigned shift
        return false;
    }

    /**
     * Check if attendance record is for today
     * @return true if attendance date is today
     */
    public boolean isToday() {
        return attendanceDate != null && attendanceDate.equals(LocalDate.now());
    }

    /**
     * Get formatted attendance summary
     * @return formatted string with check-in/out times
     */
    public String getAttendanceSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (checkInTime != null) {
            summary.append("VÃ o: ").append(checkInTime.toString());
        } else {
            summary.append("VÃ o: ChÆ°a cháº¥m");
        }
        
        summary.append(" | ");
        
        if (checkOutTime != null) {
            summary.append("Ra: ").append(checkOutTime.toString());
        } else {
            summary.append("Ra: ChÆ°a cháº¥m");
        }
        
        if (isComplete()) {
            summary.append(" (").append(String.format("%.1f", getWorkingHours())).append("h)");
        }
        
        return summary.toString();
    }
    
    /**
     * Get shift assignment (placeholder method for compatibility)
     * @return shift assignment if available
     */
    public StaffShiftAssignment getShiftAssignment() {
        // This would need to be properly implemented based on relationship
        return null;
    }
    
    /**
     * Get attendance status based on check-in/out completeness
     * @return attendance status
     */
    public String getAttendanceStatus() {
        if (checkInTime == null && checkOutTime == null) {
            return "ABSENT";
        } else if (checkInTime != null && checkOutTime != null) {
            return "PRESENT";
        } else if (checkInTime != null && checkOutTime == null) {
            return "PARTIAL";
        } else {
            return "INVALID";
        }
    }
    
    /**
     * Get attendance date (alias for getAttendanceDate)
     * @return attendance date
     */
    public LocalDate getDate() {
        return this.attendanceDate;
    }
}
