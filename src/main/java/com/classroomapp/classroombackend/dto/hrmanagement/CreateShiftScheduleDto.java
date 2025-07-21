package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho tạo mới lịch làm việc theo ca
 */
@Data
public class CreateShiftScheduleDto {
    @NotBlank(message = "Tên lịch làm việc không được để trống")
    @Size(max = 200, message = "Tên lịch làm việc không được vượt quá 200 ký tự")
    private String scheduleName;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    private String type; // WEEKLY, MONTHLY
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    private List<Long> shiftTemplateIds;
    private List<Long> assignedUserIds;
    
    // Backward compatibility fields
    @NotNull(message = "Employee ID không được để trống")
    private Long employeeId;
    
    @NotNull(message = "Template ID không được để trống")
    private Long templateId;
    
    @NotNull(message = "Ngày làm việc không được để trống")
    private LocalDate workingDate;
    
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;
    
    private Boolean isConfirmed = false;
    
    private String shiftType;
}
