package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLectureDto {
    
    @NotBlank(message = "Title is required")
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "Course ID is required")
    @JsonProperty("courseId")
    private Long courseId;
    
    @NotNull(message = "Start time is required")
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    @JsonProperty("type")
    private String type = "LIVE"; // LIVE, RECORDED, HYBRID
    
    @JsonProperty("roomLocation")
    private String roomLocation;
    
    @JsonProperty("maxAttendees")
    private Integer maxAttendees;
    
    @JsonProperty("isRecordingEnabled")
    private Boolean isRecordingEnabled = false;
    
    @JsonProperty("meetingUrl")
    private String meetingUrl;
    
    @JsonProperty("materials")
    private String materials;
}
