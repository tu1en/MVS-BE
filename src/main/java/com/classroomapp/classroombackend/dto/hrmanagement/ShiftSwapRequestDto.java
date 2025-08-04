package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO cho Shift Swap Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSwapRequestDto {
    
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long targetEmployeeId;
    private String targetEmployeeName;
    private Long assignmentId;
    private String assignmentDetails;
    private String reason;
    private String priority;
    private Boolean isEmergency;
    private LocalDateTime requestTime;
    private String status;
    private String targetResponse;
    private String managerResponse;
    private String targetResponseReason;
    private String managerResponseReason;
    private LocalDateTime targetResponseTime;
    private LocalDateTime managerResponseTime;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String additionalNotes;
}