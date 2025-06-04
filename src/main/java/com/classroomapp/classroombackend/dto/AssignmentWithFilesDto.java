package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating assignments with file attachments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentWithFilesDto {
    
    @NotBlank(message = "Assignment title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    
    @Min(value = 0, message = "Max score must be non-negative")
    private Integer maxScore;
    
    @NotNull(message = "Classroom ID is required")
    private Long classId;
    
    // File attachments will be handled separately in the controller
}
