package com.classroomapp.classroombackend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseScheduleDto {
    
    @JsonProperty("courseId")
    private Long courseId;
    
    @JsonProperty("courseName")
    private String courseName;
    
    @JsonProperty("scheduleItems")
    private List<ScheduleItemDto> scheduleItems;
}
