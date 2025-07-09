package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.time.LocalDate;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAttendanceHistoryDto {
    private Long lectureId;
    private String lectureTitle;
    private LocalDate sessionDate;
    private AttendanceStatus status;
} 