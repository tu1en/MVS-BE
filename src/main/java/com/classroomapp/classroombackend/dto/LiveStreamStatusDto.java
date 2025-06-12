package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveStreamStatusDto {
    
    @JsonProperty("streamId")
    private Long streamId;
    
    @JsonProperty("lectureId")
    private Long lectureId;
    
    @JsonProperty("status")
    private String status; // OFFLINE, STARTING, LIVE, ENDING, ENDED
    
    @JsonProperty("isLive")
    private Boolean isLive;
    
    @JsonProperty("viewerCount")
    private Integer viewerCount;
    
    @JsonProperty("duration")
    private Long duration; // in seconds
    
    @JsonProperty("quality")
    private String quality;
    
    @JsonProperty("streamQuality")
    private String streamQuality;
    
    @JsonProperty("bitrate")
    private Integer bitrate;
    
    @JsonProperty("fps")
    private Integer fps;
    
    @JsonProperty("resolution")
    private String resolution;
    
    @JsonProperty("lastUpdate")
    private LocalDateTime lastUpdate;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("isRecording")
    private Boolean isRecording;
    
    @JsonProperty("chatEnabled")
    private Boolean chatEnabled;
}
