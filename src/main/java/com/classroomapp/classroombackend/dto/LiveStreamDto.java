package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveStreamDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("lectureId")
    private Long lectureId;
    
    @JsonProperty("streamKey")
    private String streamKey;
    
    @JsonProperty("streamUrl")
    private String streamUrl;
    
    @JsonProperty("viewerUrl")
    private String viewerUrl;
    
    @JsonProperty("status")
    private String status; // OFFLINE, STARTING, LIVE, ENDING, ENDED
    
    @JsonProperty("viewerCount")
    private Integer viewerCount = 0;
    
    @JsonProperty("maxViewers")
    private Integer maxViewers = 0;
    
    @JsonProperty("startedAt")
    private LocalDateTime startedAt;
    
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @JsonProperty("endedAt")
    private LocalDateTime endedAt;
    
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    @JsonProperty("quality")
    private String quality; // AUTO, 1080p, 720p, 480p, 360p
    
    @JsonProperty("isRecording")
    private Boolean isRecording = false;
    
    @JsonProperty("chatEnabled")
    private Boolean chatEnabled = true;
    
    @JsonProperty("viewers")
    private List<String> viewers; // List of viewer usernames
    
    @JsonProperty("chatMessages")
    private List<String> chatMessages;
}
