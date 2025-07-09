package com.classroomapp.classroombackend.dto.exammangement;

import java.time.Instant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExamDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationInMinutes;
} 