package com.classroomapp.classroombackend.dto;

import com.classroomapp.classroombackend.model.ParentRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentRequestDto {
    
    private Long id;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Classroom ID is required")
    private Long classroomId;
    
    @NotBlank(message = "Parent name is required")
    private String parentName;
    
    @NotBlank(message = "Parent phone is required")
    private String parentPhone;
    
    @Email(message = "Valid email is required")
    private String parentEmail;
    
    @NotNull(message = "Request type is required")
    private ParentRequest.RequestType requestType;
    
    @NotNull(message = "Request date is required")
    private LocalDate requestDate;
    
    private String startTime;
    private String endTime;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String supportingDocumentUrl;
    private ParentRequest.RequestStatus status;
    private Long approvedByTeacherId;
    private Long approvedByAssistantId;
    private String teacherResponse;
    private String assistantResponse;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Boolean teacherNotified;
    private Boolean assistantNotified;
    
    // Additional fields for display
    private String studentName;
    private String studentCode;
    private String classroomName;
    private String approvedByTeacherName;
    private String approvedByAssistantName;
    
    // Helper methods for display
    public String getRequestTypeDisplayName() {
        if (requestType == null) return "";
        switch (requestType) {
            case LEAVE: return "Nghỉ học";
            case LATE_ARRIVAL: return "Đi muộn";
            case EARLY_DEPARTURE: return "Về sớm";
            default: return requestType.toString();
        }
    }
    
    public String getStatusDisplayName() {
        if (status == null) return "";
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case APPROVED: return "Đã duyệt";
            case REJECTED: return "Từ chối";
            case EXPIRED: return "Hết hạn";
            default: return status.toString();
        }
    }
    
    public boolean isExpired() {
        return requestDate != null && requestDate.isBefore(LocalDate.now()) && 
               status == ParentRequest.RequestStatus.PENDING;
    }
    
    public boolean needsApproval() {
        return status == ParentRequest.RequestStatus.PENDING && !isExpired();
    }
    
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        } else if (startTime != null) {
            return "Từ " + startTime;
        } else if (endTime != null) {
            return "Đến " + endTime;
        }
        return "";
    }
}