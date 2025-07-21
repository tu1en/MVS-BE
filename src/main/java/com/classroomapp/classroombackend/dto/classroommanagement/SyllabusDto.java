package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusDto {
    
    private Long id;
    
    @NotBlank(message = "Syllabus title is required")
    private String title;
    
    private String content;
    
    private String learningObjectives;
    
    private String requiredMaterials;
    
    private String gradingCriteria;
    
    private Long classroomId;
    
    private String classroomName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    /**
     * Check if syllabus has learning objectives
     */
    public boolean hasLearningObjectives() {
        return learningObjectives != null && !learningObjectives.trim().isEmpty();
    }

    /**
     * Check if syllabus has required materials
     */
    public boolean hasRequiredMaterials() {
        return requiredMaterials != null && !requiredMaterials.trim().isEmpty();
    }

    /**
     * Check if syllabus has grading criteria
     */
    public boolean hasGradingCriteria() {
        return gradingCriteria != null && !gradingCriteria.trim().isEmpty();
    }

    /**
     * Get content summary (first 100 characters)
     */
    public String getContentSummary() {
        if (content == null || content.trim().isEmpty()) {
            return "Chưa có nội dung";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
