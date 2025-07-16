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
    
    @NotBlank(message = "Tên ca làm việc không được để trống")
    @Size(min = 3, max = 100, message = "Tên ca làm việc phải từ 3-100 ký tự")
    private String name;
    
    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;
    
    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;
    
    @DecimalMin(value = "0.0", message = "Thời gian nghỉ không được âm")
    private Double breakHours = 0.0;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
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
