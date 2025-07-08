package com.classroomapp.classroombackend.dto.attendancemanagement;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDto {
    @NotNull
    private Long studentId;
    private String studentName;
    private String studentEmail;
    @NotNull
    private AttendanceStatus status;
} 