package com.classroomapp.classroombackend.controller;

import java.util.List;
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

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.VideoConferenceService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Video Conference Controller
 * Handles video conference room creation and management
 */
@RestController
@RequestMapping("/api/video-conference")
@CrossOrigin(origins = "*")
public class VideoConferenceController {
    
    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private VideoConferenceService videoConferenceService;
    
    /**
     * Get all available classrooms for video conference
     * Teachers and students can view available rooms
     */
    @GetMapping("/rooms")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ClassroomDto>> getAvailableRooms() {
        try {
            List<ClassroomDto> classrooms = classroomService.getAllClassrooms();
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get classroom details for video conference
     */
    @GetMapping("/rooms/{classroomId}")
    public ResponseEntity<ClassroomDto> getClassroomForConference(@PathVariable Long classroomId) {
        try {
            ClassroomDto classroom = classroomService.getClassroomById(classroomId);
            if (classroom != null) {
                return ResponseEntity.ok(classroom);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create a video conference session for a classroom
     * Only teachers can start video conferences
     */
    @PostMapping("/rooms/{classroomId}/start")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> startConference(@PathVariable Long classroomId, @RequestBody Map<String, Object> sessionData) {
        try {
            Map<String, Object> conferenceSession = videoConferenceService.createConferenceSession(classroomId, sessionData);
            Map<String, Object> startedSession = videoConferenceService.startConferenceSession((String) conferenceSession.get("sessionId"));
            return ResponseEntity.ok(startedSession);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * End a video conference session
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endConference(@PathVariable String sessionId) {
        try {
            Map<String, Object> result = videoConferenceService.endConferenceSession(sessionId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get conference status for a session
     */
    @GetMapping("/sessions/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> getConferenceStatus(@PathVariable String sessionId) {
        try {
            Map<String, Object> status = videoConferenceService.getConferenceStatus(sessionId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get WebRTC configuration
     */
    @GetMapping("/webrtc-config")
    public ResponseEntity<Map<String, Object>> getWebRTCConfig() {
        try {
            Map<String, Object> config = videoConferenceService.getWebRTCConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active conference sessions
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveSessions() {
        try {
            List<Map<String, Object>> sessions = videoConferenceService.getActiveSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
