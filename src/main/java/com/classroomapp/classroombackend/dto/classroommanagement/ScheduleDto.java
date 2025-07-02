package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    
    private Long id;
    
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private String notes;
    
    private boolean isRecurring;
    
    private Long classroomId;
    
    // Helper method to format the day and time for display
    public String getFormattedSchedule() {
        return dayOfWeek + " " + startTime + " - " + endTime + " at " + location;
    }
}
