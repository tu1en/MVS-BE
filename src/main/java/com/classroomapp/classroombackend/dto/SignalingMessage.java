package com.classroomapp.classroombackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho WebRTC signaling messages (offer, answer, ice-candidate)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalingMessage {
    
    private String type; // "offer", "answer", "candidate", "join", "leave"
    private String roomId;
    private String senderId;
    private String targetId; // null cho broadcast, hoặc specific user ID
    
    // WebRTC SDP data
    private String sdp;
    
    // ICE Candidate data
    @JsonProperty("candidate")
    private IceCandidate candidate;
    
    // User info for join/leave events
    private UserInfo user;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IceCandidate {
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String name;
        private boolean isTeacher;
    }
}
