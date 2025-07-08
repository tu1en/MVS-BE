package com.classroomapp.classroombackend.dto.requestmanagement;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRequestDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String requestedRole; // "TEACHER" or "STUDENT"

    private String formResponses; // Dữ liệu JSON từ form
} 