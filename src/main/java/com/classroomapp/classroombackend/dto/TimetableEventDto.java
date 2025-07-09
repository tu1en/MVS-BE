package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDatetime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
