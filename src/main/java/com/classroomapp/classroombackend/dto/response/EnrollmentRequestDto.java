package com.classroomapp.classroombackend.dto.response;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.entity.EnrollmentRequest.EnrollmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestDto {
    private Long id;
    private Long courseTemplateId;
    private String courseTemplateName;
    private String courseTemplateDescription;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private EnrollmentStatus status;
    private String message;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String processedByName;
}