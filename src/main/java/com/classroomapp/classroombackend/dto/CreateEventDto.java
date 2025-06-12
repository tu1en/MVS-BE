package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDto {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Start date and time is required")
    private LocalDateTime startDatetime;
    
    @NotNull(message = "End date and time is required")
    private LocalDateTime endDatetime;
    
    @NotNull(message = "Event type is required")
    private String eventType;
    
    private Long classroomId;
    
    private String location;
    
    private Boolean isAllDay = false;
    
    private Integer reminderMinutes = 15;
    
    private String color = "#007bff";
    
    private String recurringRule;
    
    private Long parentEventId;
}
