package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.config.WebRTCConfig;
import com.classroomapp.classroombackend.service.VideoConferenceService;

/**
 * Test Controller for WebRTC functionality
 */
@RestController
@RequestMapping("/api/public/webrtc-test")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "false")
public class WebRTCTestController {
    
    @Autowired
    private WebRTCConfig webRTCConfig;
    
    @Autowired
    private VideoConferenceService videoConferenceService;
    
    /**
     * Test basic WebRTC configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getWebRTCConfig() {
        try {
            Map<String, Object> config = webRTCConfig.getCompleteWebRTCConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get WebRTC config");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Test video conference service
     */
    @PostMapping("/conference/test/{classroomId}")
    public ResponseEntity<Map<String, Object>> testVideoConference(@PathVariable Long classroomId, @RequestBody(required = false) Map<String, Object> sessionData) {
        try {
            if (sessionData == null) {
                sessionData = new HashMap<>();
            }
            
            Map<String, Object> session = videoConferenceService.createConferenceSession(classroomId, sessionData);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create conference session");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Test WebSocket signaling URL
     */
    @GetMapping("/signaling-info")
    public ResponseEntity<Map<String, Object>> getSignalingInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("signalingUrl", webRTCConfig.getSignalingUrl());
            info.put("stunServers", webRTCConfig.getStunServers());
            info.put("iceCandidatePoolSize", webRTCConfig.getIceCandidatePoolSize());
            info.put("status", "WebRTC configuration loaded successfully");
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get signaling info");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Test active sessions
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("activeSessions", videoConferenceService.getActiveSessions());
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get active sessions");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Simple health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("service", "WebRTC Test Controller");
        health.put("timestamp", System.currentTimeMillis());
        health.put("configValid", webRTCConfig.isConfigurationValid());
        return ResponseEntity.ok(health);
    }
    
    /**
     * Test peer connection config
     */
    @GetMapping("/peer-config")
    public ResponseEntity<Map<String, Object>> getPeerConnectionConfig() {
        try {
            Map<String, Object> config = webRTCConfig.getDefaultPeerConnectionConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get peer connection config");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Test media constraints
     */
    @GetMapping("/media-constraints")
    public ResponseEntity<Map<String, Object>> getMediaConstraints() {
        try {
            Map<String, Object> constraints = new HashMap<>();
            constraints.put("default", webRTCConfig.getDefaultMediaConstraints());
            constraints.put("screenSharing", webRTCConfig.getScreenSharingConstraints());
            return ResponseEntity.ok(constraints);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get media constraints");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
