package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.Attendance.AttendanceStatus;

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
    private Long sessionId;
    private AttendanceStatus status;
    private LocalDateTime markedAt;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
