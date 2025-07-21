package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating syllabus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSyllabusDto {

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
}