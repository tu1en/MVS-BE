package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for Attendance Corrections - Records manual adjustments
 * Based on guild.md specifications
 */
@Entity
@Table(name = "attendance_corrections",
       indexes = {
           @Index(name = "idx_corrections_log", columnList = "attendance_log_id"),
           @Index(name = "idx_corrections_user", columnList = "corrected_by, corrected_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_corrections_attendance_log"))
    private AttendanceLog attendanceLog;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_id", 
                foreignKey = @ForeignKey(name = "fk_corrections_explanation"))
    private AttendanceExplanation explanation;
    
    @Column(name = "original_check_in")
    private LocalDateTime originalCheckIn;
    
    @Column(name = "original_check_out")
    private LocalDateTime originalCheckOut;
    
    @Column(name = "corrected_check_in")
    private LocalDateTime correctedCheckIn;
    
    @Column(name = "corrected_check_out")
    private LocalDateTime correctedCheckOut;
    
    @Column(name = "correction_reason", nullable = false, columnDefinition = "TEXT")
    private String correctionReason;
    
    @Column(name = "original_working_hours", precision = 4, scale = 2)
    private BigDecimal originalWorkingHours;
    
    @Column(name = "corrected_working_hours", precision = 4, scale = 2)
    private BigDecimal correctedWorkingHours;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_by", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_corrections_user"))
    private User correctedBy;
    
    @Column(name = "corrected_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime correctedAt;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    public BigDecimal getWorkingHoursDifference() {
        BigDecimal original = originalWorkingHours != null ? originalWorkingHours : BigDecimal.ZERO;
        BigDecimal corrected = correctedWorkingHours != null ? correctedWorkingHours : BigDecimal.ZERO;
        return corrected.subtract(original);
    }
    
    public String getImpactType() {
        BigDecimal difference = getWorkingHoursDifference();
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            return "INCREASE";
        } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            return "DECREASE";
        }
        return "NO_CHANGE";
    }
    
    public boolean isIncrease() {
        return getWorkingHoursDifference().compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isDecrease() {
        return getWorkingHoursDifference().compareTo(BigDecimal.ZERO) < 0;
    }
}