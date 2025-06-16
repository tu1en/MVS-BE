package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordingSessionDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("lectureId")
    private Long lectureId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("recordingUrl")
    private String recordingUrl;
    
    @JsonProperty("duration")
    private Long duration; // in seconds
    
    @JsonProperty("fileSize")
    private Long fileSize; // in bytes
    
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    @JsonProperty("status")
    private String status; // RECORDING, PROCESSING, READY, FAILED
    
    @JsonProperty("quality")
    private String quality; // HD, SD, AUDIO_ONLY
    
    @JsonProperty("downloadUrl")
    private String downloadUrl;
    
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;
    
    @JsonProperty("transcript")
    private String transcript;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
