package com.classroomapp.classroombackend.dto.assignmentmanagement;

import java.util.List;

import com.classroomapp.classroombackend.dto.FileUploadResponse;

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
    
    private List<FileUploadResponse> attachments;
}
