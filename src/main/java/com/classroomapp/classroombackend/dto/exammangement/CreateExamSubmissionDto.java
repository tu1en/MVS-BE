package com.classroomapp.classroombackend.dto.exammangement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExamSubmissionDto {

    @NotNull
    private Long examId;

    private String content;

    // We don't need studentId here, as we'll get it from the authenticated principal.
    // We also don't need startedAt, as we'll set it on the server when submission starts.
} 