package com.classroomapp.classroombackend.dto.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin yêu cầu đổi ca
 */
@Data
public class ShiftSwapRequestDto {
    private Long id;
    private UserDetailsDto requester;
    private UserDetailsDto targetEmployee;
    private String reason;
    private ShiftSwapRequest.SwapStatus status;
    private ShiftSwapRequest.Priority priority;
    private Boolean isEmergency;
    private LocalDateTime requestTime;
    private LocalDateTime targetResponseTime;
    private LocalDateTime managerApprovalTime;
    private ShiftSwapRequest.TargetResponse targetResponse;
    private ShiftSwapRequest.ManagerResponse managerResponse;
    private String targetResponseReason;
    private String managerResponseReason;
}