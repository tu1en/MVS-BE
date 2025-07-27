package com.classroomapp.classroombackend.controller;

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
import com.classroomapp.classroombackend.service.WebRTCService;

/**
 * WebRTC Configuration Controller
 * Provides WebRTC configuration and peer connection management
 */
@RestController
@RequestMapping("/api/webrtc")
@CrossOrigin(origins = "*")
public class WebRTCController {

    @Autowired
    private WebRTCConfig webRTCConfig;

    @Autowired
    private WebRTCService webRTCService;

    /**
     * Get WebRTC configuration (STUN/TURN servers, etc.)
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getWebRTCConfig() {
        try {
            Map<String, Object> config = webRTCConfig.webRTCConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get LiveStream configuration
     */
    @GetMapping("/livestream-config")
    public ResponseEntity<Map<String, Object>> getLiveStreamConfig() {
        try {
            Map<String, Object> config = webRTCConfig.liveStreamConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create peer connection for user
     */
    @PostMapping("/peer-connection/{userId}")
    public ResponseEntity<Map<String, Object>> createPeerConnection(
            @PathVariable String userId,
            @RequestBody(required = false) Map<String, Object> config) {
        try {
            Map<String, Object> rtcConfig = webRTCService.createRTCConfiguration();
            Map<String, Object> peerConnection = webRTCService.createPeerConnection(userId, rtcConfig);
            return ResponseEntity.ok(peerConnection);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get peer connection info
     */
    @GetMapping("/peer-connection/{userId}")
    public ResponseEntity<Map<String, Object>> getPeerConnectionInfo(@PathVariable String userId) {
        try {
            Map<String, Object> info = webRTCService.getPeerConnectionInfo(userId);
            if (info != null) {
                return ResponseEntity.ok(info);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Handle WebRTC offer
     */
    @PostMapping("/offer/{userId}")
    public ResponseEntity<Map<String, Object>> handleOffer(
            @PathVariable String userId,
            @RequestBody Map<String, Object> offer) {
        try {
            Map<String, Object> answer = webRTCService.handleOffer(userId, offer);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Handle WebRTC answer
     */
    @PostMapping("/answer/{userId}")
    public ResponseEntity<Void> handleAnswer(
            @PathVariable String userId,
            @RequestBody Map<String, Object> answer) {
        try {
            webRTCService.handleAnswer(userId, answer);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Handle ICE candidate
     */
    @PostMapping("/ice-candidate/{userId}")
    public ResponseEntity<Void> handleIceCandidate(
            @PathVariable String userId,
            @RequestBody Map<String, Object> candidate) {
        try {
            webRTCService.handleIceCandidate(userId, candidate);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get WebRTC statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getWebRTCStats() {
        try {
            Map<String, Object> stats = Map.of(
                "activeConnections", webRTCService.getActiveConnections(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
