package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {
    private Long id;
    private Long submissionId;
    private String feedbackText;
    private String feedbackType;
    private List<String> attachments;
    private LocalDateTime createdDate;
    private String createdBy;
}
