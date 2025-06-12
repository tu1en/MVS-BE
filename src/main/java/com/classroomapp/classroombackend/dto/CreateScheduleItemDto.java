package com.classroomapp.classroombackend.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleItemDto {
    
    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    @JsonProperty("dayOfWeek")
    private int dayOfWeek;
    
    @NotNull(message = "Start time is required")
    @JsonProperty("startTime")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonProperty("endTime")
    private LocalTime endTime;
    
    @JsonProperty("room")
    private String room;
    
    @JsonProperty("teacher")
    private String teacher;
    
    @JsonProperty("notes")
    private String notes;
}
