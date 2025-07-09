package com.classroomapp.classroombackend.dto.requestmanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class RequestDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String requestedRole;
    private String formResponses;
    private String status;
    private String rejectReason;
    private String resultStatus; // APPROVED, REJECTED, null
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime processedAt;
}
