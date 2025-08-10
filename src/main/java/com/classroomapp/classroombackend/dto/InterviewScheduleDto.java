package com.classroomapp.classroombackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewScheduleDto {
    private Long id;
    private Long applicationId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String jobTitle;
    private String salaryRange;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private String status;
    private String result;
    private String offer;
    private String evaluation;
    private String contractType;
    private BigDecimal hourlyRate;
} 