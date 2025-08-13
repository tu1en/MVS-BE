package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for Shift Management - Work Shift Configuration
 * Enhanced version based on guild.md specifications
 */
@Entity
@Table(name = "shifts",
       indexes = {
           @Index(name = "idx_shifts_active", columnList = "is_active"),
           @Index(name = "idx_shifts_name", columnList = "name"),
           @Index(name = "idx_shifts_time_range", columnList = "start_time, end_time")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   
    @Column(name = "name", nullable = false, length = 100)
    @Basic(optional = false)
    private String name;
   
    @Column(name = "start_time", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;
   
    @Column(name = "end_time", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
   
    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek; // JSON array like ["MON","TUE","WED"]
   
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
   
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
   
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_shifts_created_by"))
    private User createdBy;
   
    // Relationships
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StaffShiftAssignment> staffAssignments;
   
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttendanceLog> attendanceLogs;
   
    // Business logic methods
    public boolean isValidShift() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
   
    public String getDurationFormatted() {
        if (startTime != null && endTime != null) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return String.format("%dh %dm", hours, remainingMinutes);
        }
        return "0h 0m";
    }
   
    public double getDurationHours() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        }
        return 0.0;
    }
   
    // Explicit setters for compatibility
    public void setActive(Boolean active) {
        this.isActive = active;
    }
   
    public void setActive(boolean active) {
        this.isActive = active;
    }
   
    @PrePersist
    @PreUpdate
    private void validateShift() {
        if (!isValidShift()) {
            throw new IllegalStateException("Giờ kết thúc ca phải sau giờ bắt đầu");
        }
    }
}