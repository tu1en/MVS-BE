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
    
    @NotEmpty(message = "Danh sách người dùng không được để trống")
    private List<Long> userIds;
    
    @NotNull(message = "ID ca làm việc không được để trống")
    private Long shiftId;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
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
