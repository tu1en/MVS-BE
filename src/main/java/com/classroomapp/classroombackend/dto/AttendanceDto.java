package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.attendancemanagement.Attendance.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userFullName;
    private String userPhotoUrl;
    private Long classroomId;
    private String classroomName;
    private Long sessionId;
    private LocalDateTime sessionDate;
    private boolean isPresent;
    private AttendanceStatus status;
    private String attendanceType;
    private String comment;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String ipAddress;
    private Long markedById;
    private String markedByName;
    private LocalDateTime markedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isTeacherRecord;
    
    // Explicit getter to resolve compilation issues
    public Long getUserId() { return userId; }
}

