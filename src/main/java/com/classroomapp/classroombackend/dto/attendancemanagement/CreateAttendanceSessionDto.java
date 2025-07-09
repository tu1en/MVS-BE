package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAttendanceSessionDto {
    @NotNull
    private Long classroomId;

    @NotNull
    @Future
    private Instant endTime;
} 