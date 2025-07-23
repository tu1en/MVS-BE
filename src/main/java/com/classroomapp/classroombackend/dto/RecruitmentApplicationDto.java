package com.classroomapp.classroombackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecruitmentApplicationDto {
    private Long id;
    private Long jobPositionId;
    private String jobTitle;
    private String fullName;
    private String email;
    private String cvUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
} 