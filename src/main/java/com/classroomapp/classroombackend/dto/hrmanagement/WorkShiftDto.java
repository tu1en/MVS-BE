package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for WorkShift entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftDto {
    
    private Long id;
    
    @NotBlank(message = "Tên ca làm việc không được để trống")
    private String name;
    
    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;
    
    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;
    
    private Double breakHours;
    
    private String description;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    // Computed fields
    private Double workingHours;
    
    private Integer assignmentCount;
    
    /**
     * Calculate working hours (excluding break time)
     */
    public Double getWorkingHours() {
        if (startTime == null || endTime == null) {
            return 0.0;
        }
        
        double totalHours = endTime.toSecondOfDay() - startTime.toSecondOfDay();
        totalHours = totalHours / 3600.0; // Convert seconds to hours
        
        return Math.max(0, totalHours - (breakHours != null ? breakHours : 0.0));
    }
    
    /**
     * Format time range as string
     */
    public String getTimeRange() {
        if (startTime == null || endTime == null) {
            return "";
        }
        return startTime.toString() + " - " + endTime.toString();
    }
}
