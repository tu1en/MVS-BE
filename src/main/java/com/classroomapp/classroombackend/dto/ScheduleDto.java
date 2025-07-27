package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;
    private String title;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("start_datetime")
    private LocalDateTime startDatetime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("end_datetime")
    private LocalDateTime endDatetime;
    
    private String location;
    private String color;
    
    @JsonProperty("classroom_id")
    private Long classroomId;
    
    @JsonProperty("classroom_name")
    private String classroomName;
    
    private Long teacherId;
    private String teacherName;
    private String subject;
    private String room;
    private String materialsUrl;
    private String meetUrl;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
    
    private DayOfWeek dayOfWeek;
}