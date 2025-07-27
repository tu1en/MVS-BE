package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for creating new work shifts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkShiftDto {
    
    @NotBlank(message = "TÃªn ca lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(min = 3, max = 100, message = "TÃªn ca lÃ m viá»‡c pháº£i tá»« 3-100 kÃ½ tá»±")
    private String name;
    
    @NotNull(message = "Giá» báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime startTime;
    
    @NotNull(message = "Giá» káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime endTime;
    
    @DecimalMin(value = "0.0", message = "Thá»i gian nghá»‰ khÃ´ng Ä‘Æ°á»£c Ã¢m")
    private Double breakHours = 0.0;
    
    @Size(max = 500, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String description;
    
    /**
     * Custom validation method to check if start time is before end time
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
    
    /**
     * Calculate total working hours
     */
    public double getWorkingHours() {
        if (!isValidTimeRange()) {
            return 0.0;
        }
        
        double totalHours = endTime.toSecondOfDay() - startTime.toSecondOfDay();
        totalHours = totalHours / 3600.0; // Convert seconds to hours
        
        return Math.max(0, totalHours - (breakHours != null ? breakHours : 0.0));
    }
}
