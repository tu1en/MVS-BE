package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest.RequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for MakeupAttendanceRequest entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MakeupAttendanceRequestDto {
    
    private Long id;
    
    // Teacher information
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    
    // Lecture information
    private Long lectureId;
    private String lectureTitle;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String lectureDate;
    private String lectureTime;
    
    // Classroom information
    private Long classroomId;
    private String classroomName;
    
    // Request details
    private String reason;
    private RequestStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    
    // Approval information
    private Long approvedById;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;
    
    private String rejectionReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Helper methods for UI
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isAcknowledged() {
        return status == RequestStatus.ACKNOWLEDGED;
    }

    public boolean isCompleted() {
        return status == RequestStatus.COMPLETED;
    }

    public boolean needsAttention() {
        return status == RequestStatus.PENDING || status == RequestStatus.ACKNOWLEDGED;
    }
    
    public String getStatusDisplayName() {
        switch (status) {
            case PENDING:
                return "Chờ xác nhận";
            case ACKNOWLEDGED:
                return "Đã xác nhận";
            case COMPLETED:
                return "Hoàn thành";
            default:
                return status.name();
        }
    }

    public String getStatusColor() {
        switch (status) {
            case PENDING:
                return "orange";
            case ACKNOWLEDGED:
                return "blue";
            case COMPLETED:
                return "green";
            default:
                return "default";
        }
    }
}
