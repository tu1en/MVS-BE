package com.classroomapp.classroombackend.dto.assignmentmanagement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeSubmissionDto {
    
    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 1000, message = "Score seems too high, must be less than 1000")
    private Integer score;
    
    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;
}
