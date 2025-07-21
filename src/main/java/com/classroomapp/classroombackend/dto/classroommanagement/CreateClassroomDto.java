package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new Classroom
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassroomDto {

    @NotNull(message = "Classroom name is required")
    @Size(min = 1, max = 255, message = "Classroom name must be between 1 and 255 characters")
    private String classroomName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    /**
     * Validate classroom name
     */
    public boolean isValidName() {
        return classroomName != null && !classroomName.trim().isEmpty() && classroomName.length() <= 255;
    }

    /**
     * Get trimmed classroom name
     */
    public String getTrimmedName() {
        return classroomName != null ? classroomName.trim() : null;
    }

    /**
     * Check if description is provided
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
}
