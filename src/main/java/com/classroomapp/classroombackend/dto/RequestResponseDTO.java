package com.classroomapp.classroombackend.dto;

import lombok.Data;

@Data
public class RequestResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String requestedRole;
    private String status;
    private String rejectReason;
    private String createdAt;
    private String processedAt;
    private String formResponses;
} 