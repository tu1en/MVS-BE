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

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.service.ClassroomService;

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
    
    /**
     * Get all available classrooms for video conference
     */
    @GetMapping("/rooms")
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
     */
    @PostMapping("/rooms/{classroomId}/start")
    public ResponseEntity<Map<String, Object>> startConference(@PathVariable Long classroomId, @RequestBody Map<String, Object> sessionData) {
        try {
            ClassroomDto classroom = classroomService.getClassroomById(classroomId);
            if (classroom == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Create conference session data
            Map<String, Object> conferenceSession = Map.of(
                "roomId", "classroom_" + classroomId,
                "classroomId", classroomId,
                "classroomName", classroom.getName(),
                "signalingUrl", "ws://localhost:8080/signaling",
                "status", "active",
                "startedAt", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(conferenceSession);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * End a video conference session
     */
    @PostMapping("/rooms/{classroomId}/end")
    public ResponseEntity<Map<String, String>> endConference(@PathVariable Long classroomId) {
        try {
            // Log conference end
            return ResponseEntity.ok(Map.of(
                "status", "ended",
                "message", "Conference ended successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get conference status for a classroom
     */
    @GetMapping("/rooms/{classroomId}/status")
    public ResponseEntity<Map<String, Object>> getConferenceStatus(@PathVariable Long classroomId) {
        try {
            // In a real implementation, you would check actual conference status
            Map<String, Object> status = Map.of(
                "classroomId", classroomId,
                "isActive", false, // This would be checked against actual sessions
                "participantCount", 0
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
