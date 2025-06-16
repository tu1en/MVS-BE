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
public class UpdateLectureDto {
    
    @NotNull(message = "Lecture ID is required")
    @JsonProperty("id")
    private Long id;
    
    @NotBlank(message = "Title is required")
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("roomLocation")
    private String roomLocation;
    
    @JsonProperty("maxAttendees")
    private Integer maxAttendees;
    
    @JsonProperty("isRecordingEnabled")
    private Boolean isRecordingEnabled;
    
    @JsonProperty("meetingUrl")
    private String meetingUrl;
    
    @JsonProperty("materials")
    private String materials;
    
    @JsonProperty("status")
    private String status; // SCHEDULED, ONGOING, COMPLETED, CANCELLED
}
