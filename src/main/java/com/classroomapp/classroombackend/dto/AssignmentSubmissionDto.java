package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String submissionText;
    private String attachmentUrl;
    private LocalDateTime submissionDate;
    private String status; // SUBMITTED, GRADED, LATE
    private Double grade;
    private String feedback;
}
