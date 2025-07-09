package com.classroomapp.classroombackend.dto.attendancemanagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentAttendanceDto {
    @NotNull
    private Long sessionId;
} 