package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDto {
    private Long id;
    private String sessionName;
    private Long classroomId;
    private String classroomName;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionType;
    private String status;
    private String title;
    private String description;
    private Boolean locationRequired;
    private Double locationLatitude;
    private Double locationLongitude;
    private Integer locationRadiusMeters;
    private Boolean autoMarkTeacherAttendance;
    private List<AttendanceDto> attendanceRecords;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

