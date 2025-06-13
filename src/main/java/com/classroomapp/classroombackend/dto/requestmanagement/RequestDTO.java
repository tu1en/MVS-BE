package com.classroomapp.classroombackend.dto.requestmanagement;

import lombok.Data;

@Data
public class RequestDTO {
    private String email;
    private String fullName;
    private String phoneNumber;
    private String requestedRole;
    private String formResponses;
}
