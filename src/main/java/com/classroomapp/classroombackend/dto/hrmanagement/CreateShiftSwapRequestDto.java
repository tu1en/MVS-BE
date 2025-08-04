package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho tạo mới Shift Swap Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShiftSwapRequestDto {
    
    @NotNull(message = "Target employee ID không được null")
    private Long targetEmployeeId;
    
    @NotNull(message = "Assignment ID không được null")
    private Long assignmentId;
    
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String reason;
    
    @NotNull(message = "Priority không được null")
    private String priority; // HIGH, MEDIUM, LOW
    
    private Boolean isEmergency = false;
    
    private LocalDateTime requestTime;
    
    private String additionalNotes;
}