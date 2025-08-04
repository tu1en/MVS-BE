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
    
    @NotBlank(message = "TÃªn ca lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String name;
    
    @NotNull(message = "Giá» báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime startTime;
    
    @NotNull(message = "Giá» káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
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
