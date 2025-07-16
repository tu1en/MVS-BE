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

/**
 * Entity representing assignment of work shifts to users
 * Links users with their assigned work shifts for specific date ranges
 */
@Entity
@Table(name = "user_shift_assignments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "start_date", "end_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Người dùng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Ca làm việc không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private WorkShift workShift;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "notes", columnDefinition = "NVARCHAR(1000)")
    private String notes;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;

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
     * Check if assignment is currently active
     * @return true if assignment is active
     */
    public boolean isCurrentlyActive() {
        return isActive != null && isActive;
    }

    /**
     * Check if assignment is valid for a specific date
     * @param date the date to check
     * @return true if date falls within assignment range
     */
    public boolean isValidForDate(LocalDate date) {
        return date != null && 
               !date.isBefore(startDate) && 
               !date.isAfter(endDate) && 
               isCurrentlyActive();
    }

    /**
     * Check if this assignment overlaps with another assignment
     * @param other the other assignment to check
     * @return true if assignments overlap
     */
    public boolean overlapsWith(UserShiftAssignment other) {
        if (other == null || !this.user.getId().equals(other.user.getId())) {
            return false;
        }
        
        return !(this.endDate.isBefore(other.startDate) || 
                 this.startDate.isAfter(other.endDate));
    }

    /**
     * Get the duration of this assignment in days
     * @return number of days
     */
    public long getDurationInDays() {
        return startDate.until(endDate).getDays() + 1; // +1 to include both start and end dates
    }

    /**
     * Validate date range
     * @return true if start date is before or equal to end date
     */
    public boolean isValidDateRange() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }
}
