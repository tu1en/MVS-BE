package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrUpdateAttendanceDto {
    @NotNull
    private Long lectureId;
    @NotNull
    private Long classroomId;
    @NotEmpty
    private List<AttendanceRecordDto> records;
} 