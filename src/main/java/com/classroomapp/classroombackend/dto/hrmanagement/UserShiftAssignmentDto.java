package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for UserShiftAssignment entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShiftAssignmentDto {
    
    private Long id;
    
    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;
    
    private String userFullName;
    
    private String userEmail;
    
    private String userDepartment;
    
    @NotNull(message = "ID ca làm việc không được để trống")
    private Long shiftId;
    
    private String shiftName;
    
    private String shiftTimeRange;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    private String notes;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private String createdByName;
    
    // Computed fields
    private Long durationInDays;
    
    private Boolean isCurrentlyValid;
    
    /**
     * Calculate duration in days
     */
    public Long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0L;
        }
        return (long) startDate.until(endDate).getDays() + 1;
    }
    
    /**
     * Check if assignment is currently valid
     */
    public Boolean getIsCurrentlyValid() {
        LocalDate today = LocalDate.now();
        return isActive != null && isActive && 
               startDate != null && endDate != null &&
               !today.isBefore(startDate) && !today.isAfter(endDate);
    }
    
    /**
     * Format date range as string
     */
    public String getDateRange() {
        if (startDate == null || endDate == null) {
            return "";
        }
        return startDate.toString() + " đến " + endDate.toString();
    }
}
