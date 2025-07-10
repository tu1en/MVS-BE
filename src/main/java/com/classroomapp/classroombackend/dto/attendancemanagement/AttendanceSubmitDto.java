package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.util.List;

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
    private Long lectureId;
    private Long classroomId;
    private List<AttendanceRecordUpdateDto> records;
    
    /**
     * Inner class representing an attendance record update
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRecordUpdateDto {
        private Long studentId;
        private String status;
        private String note; // Optional note for the attendance record
    }
} 