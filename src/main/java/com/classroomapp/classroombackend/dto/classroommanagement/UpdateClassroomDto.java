package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateClassroomDto {
    @NotBlank(message = "Classroom name cannot be blank")
    @Size(min = 3, max = 100, message = "Classroom name must be between 3 and 100 characters")
    private String name;

    private String description;

    // Alias method for backward compatibility
    public String getClassroomName() {
        return name;
    }

    public boolean hasUpdates() {
        return (name != null && !name.isEmpty()) || (description != null && !description.isEmpty());
    }
}
