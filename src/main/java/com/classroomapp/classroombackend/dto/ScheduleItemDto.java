package com.classroomapp.classroombackend.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("courseId")
    private Long courseId;
    
    @JsonProperty("dayOfWeek")
    private int dayOfWeek; // 1 = Monday, 7 = Sunday
    
    @JsonProperty("startTime")
    private LocalTime startTime;
    
    @JsonProperty("endTime")
    private LocalTime endTime;
    
    @JsonProperty("room")
    private String room;
    
    @JsonProperty("teacher")
    private String teacher;
    
    @JsonProperty("notes")
    private String notes;
}
