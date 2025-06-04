package com.classroomapp.classroombackend.dto;

import lombok.Data;

@Data
public class RequestDTO {
    private String email;
    private String fullName;
    private String phoneNumber;
    private String requestedRole;
    private String formResponses;
} 