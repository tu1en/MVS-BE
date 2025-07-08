package com.classroomapp.classroombackend.dto.exammangement;

import java.time.Instant;

import lombok.Data;

@Data
public class ExamDto {
    private Long id;
    private String title;
    private Long classroomId;
    private String classroomName;
    private Instant startTime;
    private Instant endTime;
    private Integer durationInMinutes;
} 