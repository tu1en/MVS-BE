package com.classroomapp.classroombackend.dto.assignmentmanagement;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDto {
    
    private Long id;
    
    private Long assignmentId;
    
    private String assignmentTitle;
    
    private Long studentId;
    
    private String studentName;
    
    private String comment;
    
    private String fileSubmissionUrl;
    
    private LocalDateTime submittedAt;
    
    private Integer score;
    
    private String feedback;
    
    private LocalDateTime gradedAt;
    
    private Long gradedById;
    
    private String gradedByName;
    
    // Calculated fields
    private Boolean isLate;
    
    private Boolean isGraded;
}
