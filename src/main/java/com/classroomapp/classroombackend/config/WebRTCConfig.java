package com.classroomapp.classroombackend.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebRTC Configuration
 */
@Configuration
public class WebRTCConfig {
    
    @Value("${webrtc.stun.servers:stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302}")
    private String stunServers;
    
    @Value("${webrtc.turn.servers:}")
    private String turnServers;
    
    @Value("${webrtc.ice.candidate.pool.size:10}")
    private Integer iceCandidatePoolSize;
    
    @Value("${webrtc.signaling.url:ws://localhost:8088/signaling}")
    private String signalingUrl;
    
    @Value("${livestream.rtmp.server.url:rtmp://localhost:1935/live}")
    private String rtmpServerUrl;
    
    @Value("${livestream.max.viewers.default:100}")
    private Integer defaultMaxViewers;
    
    @Value("${livestream.recording.enabled.default:false}")
    private Boolean defaultRecordingEnabled;
    
    @Value("${livestream.chat.enabled.default:true}")
    private Boolean defaultChatEnabled;
    
    @Value("${videoconference.max.participants.default:50}")
    private Integer defaultMaxParticipants;
    
    @Value("${videoconference.screen.sharing.enabled:true}")
    private Boolean screenSharingEnabled;
    
    @Value("${videoconference.recording.enabled:true}")
    private Boolean videoRecordingEnabled;
    
    @Value("${videoconference.session.timeout.minutes:240}")
    private Integer sessionTimeoutMinutes;
    
    /**
     * Get WebRTC configuration as a Map
     */
    @Bean
    public Map<String, Object> webRTCConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        // ICE servers configuration
        List<Map<String, Object>> iceServers = new ArrayList<>();
        
        // Add STUN servers
        if (stunServers != null && !stunServers.trim().isEmpty()) {
            String[] stunServerArray = stunServers.split(",");
            for (String stunServer : stunServerArray) {
                Map<String, Object> stunConfig = new HashMap<>();
                stunConfig.put("urls", stunServer.trim());
                iceServers.add(stunConfig);
            }
        }
        
        // Add TURN servers if configured
        if (turnServers != null && !turnServers.trim().isEmpty()) {
            String[] turnServerArray = turnServers.split(",");
            for (String turnServer : turnServerArray) {
                Map<String, Object> turnConfig = new HashMap<>();
                turnConfig.put("urls", turnServer.trim());
                // Add credentials if needed
                // turnConfig.put("username", "your-turn-username");
                // turnConfig.put("credential", "your-turn-password");
                iceServers.add(turnConfig);
            }
        }
        
        config.put("iceServers", iceServers);
        config.put("iceCandidatePoolSize", iceCandidatePoolSize);
        config.put("signalingUrl", signalingUrl);
        
        return config;
    }
    
    /**
     * Get LiveStream configuration as a Map
     */
    @Bean
    public Map<String, Object> liveStreamConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("rtmpServerUrl", rtmpServerUrl);
        config.put("defaultMaxViewers", defaultMaxViewers);
        config.put("defaultRecordingEnabled", defaultRecordingEnabled);
        config.put("defaultChatEnabled", defaultChatEnabled);
        
        return config;
    }
    
    /**
     * Get Video Conference configuration as a Map
     */
    @Bean
    public Map<String, Object> videoConferenceConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("defaultMaxParticipants", defaultMaxParticipants);
        config.put("screenSharingEnabled", screenSharingEnabled);
        config.put("recordingEnabled", videoRecordingEnabled);
        config.put("sessionTimeoutMinutes", sessionTimeoutMinutes);
        
        return config;
    }
    
    // Getters for individual properties
    public String getStunServers() {
        return stunServers;
    }
    
    public String getTurnServers() {
        return turnServers;
    }
    
    public Integer getIceCandidatePoolSize() {
        return iceCandidatePoolSize;
    }
    
    public String getSignalingUrl() {
        return signalingUrl;
    }
    
    public String getRtmpServerUrl() {
        return rtmpServerUrl;
    }
    
    public Integer getDefaultMaxViewers() {
        return defaultMaxViewers;
    }
    
    public Boolean getDefaultRecordingEnabled() {
        return defaultRecordingEnabled;
    }
    
    public Boolean getDefaultChatEnabled() {
        return defaultChatEnabled;
    }
    
    public Integer getDefaultMaxParticipants() {
        return defaultMaxParticipants;
    }
    
    public Boolean getScreenSharingEnabled() {
        return screenSharingEnabled;
    }
    
    public Boolean getVideoRecordingEnabled() {
        return videoRecordingEnabled;
    }
    
    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }
    
    /**
     * Get complete WebRTC configuration for frontend
     */
    public Map<String, Object> getCompleteWebRTCConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("webrtc", webRTCConfiguration());
        config.put("livestream", liveStreamConfiguration());
        config.put("videoconference", videoConferenceConfiguration());
        
        return config;
    }
    
    /**
     * Validate WebRTC configuration
     */
    public boolean isConfigurationValid() {
        // Check if at least one STUN server is configured
        if (stunServers == null || stunServers.trim().isEmpty()) {
            return false;
        }
        
        // Check if signaling URL is configured
        if (signalingUrl == null || signalingUrl.trim().isEmpty()) {
            return false;
        }
        
        // Check if RTMP server URL is configured
        if (rtmpServerUrl == null || rtmpServerUrl.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get default peer connection configuration
     */
    public Map<String, Object> getDefaultPeerConnectionConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("iceServers", webRTCConfiguration().get("iceServers"));
        config.put("iceCandidatePoolSize", iceCandidatePoolSize);
        
        // Additional peer connection options
        config.put("bundlePolicy", "max-bundle");
        config.put("rtcpMuxPolicy", "require");
        config.put("iceTransportPolicy", "all");
        
        return config;
    }
    
    /**
     * Get media constraints for getUserMedia
     */
    public Map<String, Object> getDefaultMediaConstraints() {
        Map<String, Object> constraints = new HashMap<>();
        
        // Video constraints
        Map<String, Object> video = new HashMap<>();
        video.put("width", Map.of("min", 640, "ideal", 1280, "max", 1920));
        video.put("height", Map.of("min", 480, "ideal", 720, "max", 1080));
        video.put("frameRate", Map.of("min", 15, "ideal", 30, "max", 60));
        
        // Audio constraints
        Map<String, Object> audio = new HashMap<>();
        audio.put("echoCancellation", true);
        audio.put("noiseSuppression", true);
        audio.put("autoGainControl", true);
        
        constraints.put("video", video);
        constraints.put("audio", audio);
        
        return constraints;
    }
    
    /**
     * Get screen sharing constraints
     */
    public Map<String, Object> getScreenSharingConstraints() {
        Map<String, Object> constraints = new HashMap<>();
        
        Map<String, Object> video = new HashMap<>();
        video.put("mediaSource", "screen");
        video.put("width", Map.of("max", 1920));
        video.put("height", Map.of("max", 1080));
        video.put("frameRate", Map.of("max", 30));
        
        constraints.put("video", video);
        constraints.put("audio", true);
        
        return constraints;
    }
}
