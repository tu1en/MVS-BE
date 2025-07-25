package com.classroomapp.classroombackend.dto.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating single shift assignment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSingleShiftAssignmentDto {
    
    @NotNull(message = "ID nhÃ¢n viÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long employeeId;
    
    @NotNull(message = "NgÃ y phÃ¢n cÃ´ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @FutureOrPresent(message = "NgÃ y phÃ¢n cÃ´ng pháº£i tá»« hÃ´m nay trá»Ÿ Ä‘i")
    private LocalDate assignmentDate;
    
    @NotNull(message = "Thá»i gian báº¯t Ä‘áº§u dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime plannedStartTime;
    
    @NotNull(message = "Thá»i gian káº¿t thÃºc dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime plannedEndTime;
    
    @NotNull(message = "Sá»‘ giá» lÃ m viá»‡c dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private BigDecimal plannedHours;
    
    @Size(max = 1000, message = "Ghi chÃº khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String notes;
    
    private Long shiftTemplateId;
    
    private Long scheduleId;
    
    @Size(max = 100, message = "TÃªn ngÆ°á»i táº¡o khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 100 kÃ½ tá»±")
    private String createdBy;
    
    /**
     * Validate time range
     */
    public boolean isValidTimeRange() {
        return plannedStartTime != null && plannedEndTime != null && 
               plannedStartTime.isBefore(plannedEndTime);
    }
    
    /**
     * Check if planned hours match time range
     */
    public boolean isValidPlannedHours() {
        if (plannedStartTime == null || plannedEndTime == null || plannedHours == null) {
            return false;
        }
        
        long minutes = java.time.Duration.between(plannedStartTime, plannedEndTime).toMinutes();
        BigDecimal calculatedHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        return plannedHours.compareTo(calculatedHours) == 0;
    }
    
    /**
     * Check if assignment is for future dates
     */
    public boolean isFutureAssignment() {
        return assignmentDate != null && assignmentDate.isAfter(LocalDate.now());
    }
    
    /**
     * Calculate hours from time range
     */
    public BigDecimal calculateHoursFromTimeRange() {
        if (plannedStartTime == null || plannedEndTime == null) {
            return BigDecimal.ZERO;
        }
        
        long minutes = java.time.Duration.between(plannedStartTime, plannedEndTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
