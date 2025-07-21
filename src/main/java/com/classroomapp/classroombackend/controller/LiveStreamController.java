package com.classroomapp.classroombackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.service.LiveStreamService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Controller for LiveStream management
 */
@RestController
@RequestMapping("/api/livestream")
@CrossOrigin(origins = "*")
public class LiveStreamController {
    
    @Autowired
    private LiveStreamService liveStreamService;
    
    /**
     * Create a new live stream for a lecture
     * Only teachers can create live streams
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveStreamDto> createLiveStream(@RequestBody Map<String, Object> request) {
        try {
            Long lectureId = Long.valueOf(request.get("lectureId").toString());
            
            LiveStreamDto streamConfig = new LiveStreamDto();
            if (request.containsKey("title")) {
                // Note: LiveStreamDto doesn't have title field, but we can extend it if needed
            }
            if (request.containsKey("chatEnabled")) {
                streamConfig.setChatEnabled((Boolean) request.get("chatEnabled"));
            }
            if (request.containsKey("maxViewers")) {
                streamConfig.setMaxViewers((Integer) request.get("maxViewers"));
            }
            if (request.containsKey("isRecording")) {
                streamConfig.setIsRecording((Boolean) request.get("isRecording"));
            }
            
            LiveStreamDto createdStream = liveStreamService.createLiveStream(lectureId, streamConfig);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStream);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Start a live stream
     * Only teachers can start live streams
     */
    @PostMapping("/{streamId}/start")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveStreamDto> startLiveStream(@PathVariable Long streamId) {
        try {
            LiveStreamDto stream = liveStreamService.startLiveStream(streamId);
            return ResponseEntity.ok(stream);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Stop a live stream
     * Only teachers can stop live streams
     */
    @PostMapping("/{streamId}/stop")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveStreamDto> stopLiveStream(@PathVariable Long streamId) {
        try {
            LiveStreamDto stream = liveStreamService.stopLiveStream(streamId);
            return ResponseEntity.ok(stream);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get live stream by ID
     * Teachers and students can view live stream details
     */
    @GetMapping("/{streamId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<LiveStreamDto> getLiveStream(@PathVariable Long streamId) {
        try {
            LiveStreamDto stream = liveStreamService.getLiveStreamById(streamId);
            return ResponseEntity.ok(stream);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get active live stream for a lecture
     */
    @GetMapping("/lecture/{lectureId}/active")
    public ResponseEntity<LiveStreamDto> getActiveLiveStreamForLecture(@PathVariable Long lectureId) {
        try {
            LiveStreamDto stream = liveStreamService.getActiveLiveStreamByLectureId(lectureId);
            if (stream != null) {
                return ResponseEntity.ok(stream);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all live streams for a lecture
     */
    @GetMapping("/lecture/{lectureId}")
    public ResponseEntity<List<LiveStreamDto>> getLiveStreamsByLecture(@PathVariable Long lectureId) {
        try {
            List<LiveStreamDto> streams = liveStreamService.getLiveStreamsByLectureId(lectureId);
            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all active live streams
     */
    @GetMapping("/active")
    public ResponseEntity<List<LiveStreamDto>> getActiveLiveStreams() {
        try {
            List<LiveStreamDto> streams = liveStreamService.getActiveLiveStreams();
            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update live stream configuration
     */
    @PutMapping("/{streamId}")
    public ResponseEntity<LiveStreamDto> updateLiveStream(@PathVariable Long streamId, @RequestBody LiveStreamDto streamConfig) {
        try {
            LiveStreamDto updatedStream = liveStreamService.updateLiveStream(streamId, streamConfig);
            return ResponseEntity.ok(updatedStream);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete a live stream
     */
    @DeleteMapping("/{streamId}")
    public ResponseEntity<Void> deleteLiveStream(@PathVariable Long streamId) {
        try {
            liveStreamService.deleteLiveStream(streamId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update viewer count for a live stream
     */
    @PostMapping("/{streamId}/viewers")
    public ResponseEntity<Void> updateViewerCount(@PathVariable Long streamId, @RequestParam Integer count) {
        try {
            liveStreamService.updateViewerCount(streamId, count);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add viewer to live stream
     */
    @PostMapping("/{streamId}/viewers/add")
    public ResponseEntity<Void> addViewer(@PathVariable Long streamId, @RequestParam String username) {
        try {
            liveStreamService.addViewer(streamId, username);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Remove viewer from live stream
     */
    @PostMapping("/{streamId}/viewers/remove")
    public ResponseEntity<Void> removeViewer(@PathVariable Long streamId, @RequestParam String username) {
        try {
            liveStreamService.removeViewer(streamId, username);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get live stream by stream key
     */
    @GetMapping("/key/{streamKey}")
    public ResponseEntity<LiveStreamDto> getLiveStreamByKey(@PathVariable String streamKey) {
        try {
            LiveStreamDto stream = liveStreamService.getLiveStreamByStreamKey(streamKey);
            return ResponseEntity.ok(stream);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get live streams for a classroom
     */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<LiveStreamDto>> getLiveStreamsByClassroom(@PathVariable Long classroomId) {
        try {
            List<LiveStreamDto> streams = liveStreamService.getLiveStreamsByClassroomId(classroomId);
            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get total active viewers
     */
    @GetMapping("/stats/viewers")
    public ResponseEntity<Map<String, Integer>> getTotalActiveViewers() {
        try {
            Integer totalViewers = liveStreamService.getTotalActiveViewers();
            return ResponseEntity.ok(Map.of("totalActiveViewers", totalViewers));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * End expired live streams (maintenance endpoint)
     */
    @PostMapping("/maintenance/end-expired")
    public ResponseEntity<Map<String, Integer>> endExpiredStreams() {
        try {
            int endedCount = liveStreamService.endExpiredLiveStreams();
            return ResponseEntity.ok(Map.of("endedStreamsCount", endedCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
