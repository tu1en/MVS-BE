package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResultDto {
    private long totalSessions;
    private long attendedSessions;
    private double attendancePercentage;
    private List<StudentAttendanceDto> detailedRecords;
} 