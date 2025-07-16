package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing work shifts for HR management
 * Used to define standard working hours for non-teaching staff
 */
@Entity
@Table(name = "work_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên ca làm việc không được để trống")
    @Column(name = "name", nullable = false, unique = true, columnDefinition = "NVARCHAR(100)")
    private String name;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_hours", columnDefinition = "DECIMAL(3,1) DEFAULT 0.0")
    private Double breakHours = 0.0;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    // One-to-many relationship with UserShiftAssignment
    @OneToMany(mappedBy = "workShift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserShiftAssignment> assignments;

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
     * Get shift name (alias for getName() for compatibility)
     * @return shift name
     */
    public String getShiftName() {
        return name;
    }

    /**
     * Calculate total working hours per day (excluding break time)
     * @return working hours as double
     */
    public double getWorkingHours() {
        if (startTime == null || endTime == null) {
            return 0.0;
        }

        double totalHours = endTime.toSecondOfDay() - startTime.toSecondOfDay();
        totalHours = totalHours / 3600.0; // Convert seconds to hours

        return Math.max(0, totalHours - (breakHours != null ? breakHours : 0.0));
    }

    /**
     * Check if this shift is currently active
     * @return true if shift is active
     */
    public boolean isCurrentlyActive() {
        return isActive != null && isActive;
    }

    /**
     * Validate shift times
     * @return true if start time is before end time
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
