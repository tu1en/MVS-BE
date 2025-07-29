package com.classroomapp.classroombackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import lombok.extern.slf4j.Slf4j;

/**
 * Video Conference Controller
 * Handles video conference room creation and management
 */
@RestController
@RequestMapping("/api/video-conference")
@CrossOrigin(origins = "*")
@Slf4j
public class VideoConferenceController {
    
    @Autowired
    private ClassroomService classroomService;
    
    /**
     * Get all available classrooms for video conference
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ClassroomDto>> getAvailableRooms() {
        try {
            // Fix: Add Pageable parameter with default pagination
            Pageable pageable = PageRequest.of(0, 100); // Get first 100 classrooms
            Page<ClassroomDto> classroomPage = classroomService.getAllClassrooms(pageable);
            List<ClassroomDto> classrooms = classroomPage.getContent();
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error getting available rooms for video conference", e);
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
            log.error("Error getting classroom {} for conference", classroomId, e);
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
                "signalingUrl", "ws://localhost:8088/signaling",
                "status", "active",
                "startedAt", System.currentTimeMillis()
            );
            
            log.info("Started video conference for classroom {}", classroomId);
            return ResponseEntity.ok(conferenceSession);
        } catch (Exception e) {
            log.error("Error starting conference for classroom {}", classroomId, e);
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
            log.info("Ended video conference for classroom {}", classroomId);
            return ResponseEntity.ok(Map.of(
                "status", "ended",
                "message", "Conference ended successfully"
            ));
        } catch (Exception e) {
            log.error("Error ending conference for classroom {}", classroomId, e);
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
            log.error("Error getting conference status for classroom {}", classroomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}