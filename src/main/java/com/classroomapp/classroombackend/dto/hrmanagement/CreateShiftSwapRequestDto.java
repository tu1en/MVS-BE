package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho tạo mới yêu cầu đổi ca
 */
@Data
public class CreateShiftSwapRequestDto {
    @NotNull(message = "Target employee ID không được để trống")
    private Long targetEmployeeId;
    
    @NotBlank(message = "Lý do không được để trống")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String reason;
    
    @NotNull(message = "Priority không được để trống")
    private String priority;
    
    private Boolean isEmergency = false;
    
    private LocalDateTime requestTime = LocalDateTime.now();
}