package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableEventDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String eventType;
    private Long classroomId;
    private String classroomName;
    private Long createdBy;
    private String location;
    private Boolean isAllDay;
    private Integer reminderMinutes;
    private String color;
    private String recurringRule;
    private Long parentEventId;
    private Boolean isCancelled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
