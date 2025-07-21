package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

/**
 * DTO cho cập nhật mẫu ca làm việc
 */
@Data
public class UpdateShiftTemplateDto {
    @NotBlank(message = "Tên template không được để trống")
    @Size(max = 100, message = "Tên template không được vượt quá 100 ký tự")
    private String templateName;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
    
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalTime startTime;
    
    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalTime endTime;
    
    @Size(max = 7, message = "Mã màu không được vượt quá 7 ký tự")
    private String colorCode;
    
    private Boolean isOvertimeEligible;
    
    private Integer maxEmployees;
    
    private Integer minEmployees;
    
    private Boolean isActive;
}
