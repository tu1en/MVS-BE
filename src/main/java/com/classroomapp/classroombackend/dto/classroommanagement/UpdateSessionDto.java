package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing Session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return sessionDate != null || description != null;
    }

    /**
     * Validate that session date is not in the past (if provided)
     */
    public boolean isValidDate() {
        return sessionDate == null || !sessionDate.isBefore(LocalDate.now());
    }
}
