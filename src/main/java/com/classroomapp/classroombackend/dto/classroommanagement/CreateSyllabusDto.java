package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new syllabus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSyllabusDto {

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotBlank(message = "Syllabus title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Content cannot exceed 5000 characters")
    private String content;

    @Size(max = 2000, message = "Learning objectives cannot exceed 2000 characters")
    private String learningObjectives;

    @Size(max = 1000, message = "Required materials cannot exceed 1000 characters")
    private String requiredMaterials;

    @Size(max = 1000, message = "Grading criteria cannot exceed 1000 characters")
    private String gradingCriteria;

    /**
     * Validate if title is provided and not empty
     */
    public boolean isValidTitle() {
        return title != null && !title.trim().isEmpty() && title.length() <= 255;
    }

    /**
     * Get trimmed title
     */
    public String getTrimmedTitle() {
        return title != null ? title.trim() : null;
    }

    /**
     * Check if content is provided
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * Check if learning objectives are provided
     */
    public boolean hasLearningObjectives() {
        return learningObjectives != null && !learningObjectives.trim().isEmpty();
    }

    /**
     * Check if required materials are provided
     */
    public boolean hasRequiredMaterials() {
        return requiredMaterials != null && !requiredMaterials.trim().isEmpty();
    }

    /**
     * Check if grading criteria are provided
     */
    public boolean hasGradingCriteria() {
        return gradingCriteria != null && !gradingCriteria.trim().isEmpty();
    }
}