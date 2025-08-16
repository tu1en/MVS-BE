package com.classroomapp.classroombackend.dto.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for submitting attendance records for a lecture
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSubmitDto {
    private Long lectureId; // ID of the lecture
    private Long classroomId; // ID of the classroom
    private List<AttendanceRecord> records; // List of attendance records

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRecord {
        private Long studentId;
        private String status;
        private String note; // Optional note for the attendance record
    }
} 