package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating shift assignments (batch assignment)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShiftAssignmentDto {
    
    @NotEmpty(message = "Danh sÃ¡ch ngÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private List<Long> userIds;
    
    @NotNull(message = "ID ca lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long shiftId;
    
    @NotNull(message = "NgÃ y báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate startDate;
    
    @NotNull(message = "NgÃ y káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate endDate;
    
    @Size(max = 1000, message = "Ghi chÃº khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String notes;
    
    /**
     * Validate date range
     */
    public boolean isValidDateRange() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }
    
    /**
     * Check if assignment is for future dates
     */
    public boolean isFutureAssignment() {
        return startDate != null && startDate.isAfter(LocalDate.now());
    }
    
    /**
     * Get duration in days
     */
    public long getDurationInDays() {
        if (!isValidDateRange()) {
            return 0;
        }
        return startDate.until(endDate).getDays() + 1;
    }
}
