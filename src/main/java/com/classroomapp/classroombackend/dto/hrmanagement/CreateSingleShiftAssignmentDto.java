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
    
    @NotNull(message = "ID nhân viên không được để trống")
    private Long employeeId;
    
    @NotNull(message = "Ngày phân công không được để trống")
    @FutureOrPresent(message = "Ngày phân công phải từ hôm nay trở đi")
    private LocalDate assignmentDate;
    
    @NotNull(message = "Thời gian bắt đầu dự kiến không được để trống")
    private LocalTime plannedStartTime;
    
    @NotNull(message = "Thời gian kết thúc dự kiến không được để trống")
    private LocalTime plannedEndTime;
    
    @NotNull(message = "Số giờ làm việc dự kiến không được để trống")
    private BigDecimal plannedHours;
    
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
    
    private Long shiftTemplateId;
    
    private Long scheduleId;
    
    @Size(max = 100, message = "Tên người tạo không được vượt quá 100 ký tự")
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
