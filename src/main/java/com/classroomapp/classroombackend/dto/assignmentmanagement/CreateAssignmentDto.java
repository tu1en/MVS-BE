package com.classroomapp.classroombackend.dto.assignmentmanagement;

import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.dto.FileUploadResponse;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentDto {
    
    @NotBlank(message = "Assignment title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    
    @Min(value = 0, message = "Points must be non-negative")
    private Integer points;
    
    private List<FileUploadResponse> attachments;
    
    @NotNull(message = "Classroom ID is required")
    private Long classroomId;
}
