package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO for creating a new classroom schedule
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleDto {

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private boolean isRecurring = true;

    /**
     * Validate time range
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    /**
     * Get duration in minutes
     */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Check if schedule is within valid working hours (7:00 - 22:00)
     */
    public boolean isWithinWorkingHours() {
        LocalTime workStart = LocalTime.of(7, 0);
        LocalTime workEnd = LocalTime.of(22, 0);
        return startTime != null && endTime != null &&
               !startTime.isBefore(workStart) && !endTime.isAfter(workEnd);
    }
}