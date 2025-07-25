package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionDto {
    
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;
    
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;
    
    private String fileSubmissionUrl;
} 