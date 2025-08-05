package com.classroomapp.classroombackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentRequestDto {
    
    @NotNull(message = "Course template ID is required")
    private Long courseTemplateId;
    
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message; // Student's message when requesting enrollment
}