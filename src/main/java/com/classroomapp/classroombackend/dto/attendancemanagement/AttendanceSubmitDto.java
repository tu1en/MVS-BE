package com.classroomapp.classroombackend.dto.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting attendance records for a lecture
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSubmitDto {
    private Long studentId;
    private String status;
    private String note; // Optional note for the attendance record
    private Long sessionId; // ID of the attendance session
    private Long classroomId; // ID of the classroom
} 