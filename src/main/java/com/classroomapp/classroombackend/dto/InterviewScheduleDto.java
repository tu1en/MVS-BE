package com.classroomapp.classroombackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewScheduleDto {
    private Long id;
    private Long applicationId;
    private String applicantName;
    private String applicantEmail;
    private String jobTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String result;
} 