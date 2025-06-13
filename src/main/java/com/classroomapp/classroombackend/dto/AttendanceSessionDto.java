package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
<<<<<<< HEAD

import lombok.AllArgsConstructor;
=======
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
>>>>>>> master
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
<<<<<<< HEAD
=======
@Builder
>>>>>>> master
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDto {
    private Long id;
<<<<<<< HEAD
=======
    private String sessionName;
>>>>>>> master
    private Long classroomId;
    private String classroomName;
    private Long teacherId;
    private String teacherName;
<<<<<<< HEAD
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionType;
    private String status;
    private String title;
    private boolean autoMarkTeacherAttendance;
    private boolean isActive;
    private LocalDateTime createdAt;
} 
=======
    private LocalDateTime sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Boolean locationRequired;
    private Double locationLatitude;
    private Double locationLongitude;
    private Integer locationRadiusMeters;
    private Boolean autoMarkTeacherAttendance;
    private String description;
    private List<AttendanceDto> attendanceRecords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
>>>>>>> master
