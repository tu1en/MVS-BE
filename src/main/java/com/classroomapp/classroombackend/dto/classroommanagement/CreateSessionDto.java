package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new Session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionDto {

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Session date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * Validate that session date is not in the past
     */
    public boolean isValidDate() {
        return sessionDate != null && !sessionDate.isBefore(LocalDate.now());
    }

    /**
     * Get formatted date for display
     */
    public String getFormattedDate() {
        return sessionDate != null ? sessionDate.toString() : null;
    }
}
