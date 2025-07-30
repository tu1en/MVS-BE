package com.classroomapp.classroombackend.dto.absencemanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class AbsenceDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Integer numberOfDays;
    private String description;
    private String status;
    private String resultStatus;
    private String rejectReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    private Long processedBy;
    private Boolean isOverLimit;
} 
