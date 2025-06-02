package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDto {
    private Long id;
    private Long classroomId;
    private String classroomName;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionType;
    private String status;
    private String title;
    private boolean autoMarkTeacherAttendance;
    private boolean isActive;
    private LocalDateTime createdAt;
} 