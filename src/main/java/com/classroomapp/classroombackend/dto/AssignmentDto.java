package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignmentDto {
    
    private Long id;
    
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
    
    private String fileAttachmentUrl;
    
    private Long classroomId;
    
    private String classroomName;
    
    // Added for frontend integration
    private String submissionStatus;
    
    // Added for frontend integration
    private Double score;
    
    // Constructor with all fields
    public AssignmentDto(Long id, String title, String description, LocalDateTime dueDate,
                         Integer points, String fileAttachmentUrl, Long classroomId, 
                         String classroomName, String submissionStatus, Double score) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.points = points;
        this.fileAttachmentUrl = fileAttachmentUrl;
        this.classroomId = classroomId;
        this.classroomName = classroomName;
        this.submissionStatus = submissionStatus;
        this.score = score;
    }
    
    // Constructor without submission status and score for backward compatibility
    public AssignmentDto(Long id, String title, String description, LocalDateTime dueDate,
                         Integer points, String fileAttachmentUrl, Long classroomId, String classroomName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.points = points;
        this.fileAttachmentUrl = fileAttachmentUrl;
        this.classroomId = classroomId;
        this.classroomName = classroomName;
        this.submissionStatus = null;
        this.score = null;
    }
} 