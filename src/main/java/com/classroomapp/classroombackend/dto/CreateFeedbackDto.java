package com.classroomapp.classroombackend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedbackDto {
    private Long submissionId;
    private String feedbackText;
    private String feedbackType; // WRITTEN, AUDIO, VIDEO
    private List<String> attachments;
}
