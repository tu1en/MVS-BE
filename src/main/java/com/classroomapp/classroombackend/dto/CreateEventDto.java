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
    
    // Explicit setters to resolve compilation issues
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartDatetime(LocalDateTime startDatetime) { this.startDatetime = startDatetime; }
    public void setEndDatetime(LocalDateTime endDatetime) { this.endDatetime = endDatetime; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public void setClassroomId(long classroomId) { this.classroomId = classroomId; }
    public void setLocation(String location) { this.location = location; }
    public void setIsAllDay(Boolean isAllDay) { this.isAllDay = isAllDay; }
    public void setReminderMinutes(Integer reminderMinutes) { this.reminderMinutes = reminderMinutes; }
    public void setColor(String color) { this.color = color; }
    public void setRecurringRule(String recurringRule) { this.recurringRule = recurringRule; }
    public void setParentEventId(Long parentEventId) { this.parentEventId = parentEventId; }
}
