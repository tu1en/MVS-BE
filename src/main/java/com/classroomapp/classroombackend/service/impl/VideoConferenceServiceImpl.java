package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.VideoConferenceService;

/**
 * Implementation of VideoConferenceService
 */
@Service
public class VideoConferenceServiceImpl implements VideoConferenceService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoConferenceServiceImpl.class);
    
    @Autowired
    private ClassroomService classroomService;
    
    // In-memory storage for conference sessions (in production, use Redis or database)
    private final Map<String, Map<String, Object>> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> sessionParticipants = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, Object> createConferenceSession(Long classroomId, Map<String, Object> sessionData) {
        logger.info("Creating conference session for classroom ID: {}", classroomId);
        
        // Validate classroom exists
        ClassroomDto classroom = classroomService.getClassroomById(classroomId);
        if (classroom == null) {
            throw new IllegalArgumentException("Classroom not found with id: " + classroomId);
        }
        
        // Generate session ID
        String sessionId = generateRoomId(classroomId);
        
        // Create session details
        Map<String, Object> session = new HashMap<>();
        session.put("sessionId", sessionId);
        session.put("roomId", "classroom_" + classroomId);
        session.put("classroomId", classroomId);
        session.put("classroomName", classroom.getName());
        session.put("status", "created");
        session.put("createdAt", LocalDateTime.now());
        session.put("participantCount", 0);
        session.put("maxParticipants", sessionData.getOrDefault("maxParticipants", 50));
        session.put("screenSharingEnabled", sessionData.getOrDefault("screenSharingEnabled", true));
        session.put("recordingEnabled", sessionData.getOrDefault("recordingEnabled", false));
        session.put("chatEnabled", sessionData.getOrDefault("chatEnabled", true));
        session.put("signalingUrl", "ws://localhost:8088/signaling");
        
        // Store session
        activeSessions.put(sessionId, session);
        sessionParticipants.put(sessionId, new ArrayList<>());
        
        logger.info("Created conference session: {}", sessionId);
        return session;
    }
    
    @Override
    public Map<String, Object> startConferenceSession(String sessionId) {
        logger.info("Starting conference session: {}", sessionId);
        
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Conference session not found: " + sessionId);
        }
        
        session.put("status", "active");
        session.put("startedAt", LocalDateTime.now());
        
        logger.info("Started conference session: {}", sessionId);
        return session;
    }
    
    @Override
    public Map<String, Object> endConferenceSession(String sessionId) {
        logger.info("Ending conference session: {}", sessionId);
        
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Conference session not found: " + sessionId);
        }
        
        session.put("status", "ended");
        session.put("endedAt", LocalDateTime.now());
        session.put("participantCount", 0);
        
        // Clear participants
        sessionParticipants.put(sessionId, new ArrayList<>());
        
        logger.info("Ended conference session: {}", sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ended");
        result.put("message", "Conference ended successfully");
        result.put("sessionId", sessionId);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getConferenceStatus(String sessionId) {
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session == null) {
            Map<String, Object> status = new HashMap<>();
            status.put("sessionId", sessionId);
            status.put("status", "not_found");
            status.put("participantCount", 0);
            return status;
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("sessionId", sessionId);
        status.put("status", session.get("status"));
        status.put("participantCount", session.get("participantCount"));
        status.put("classroomId", session.get("classroomId"));
        status.put("isActive", "active".equals(session.get("status")));
        
        return status;
    }
    
    @Override
    public void addParticipant(String sessionId, String participantId, String participantName) {
        logger.debug("Adding participant {} to session {}", participantId, sessionId);
        
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Conference session not found: " + sessionId);
        }
        
        List<Map<String, Object>> participants = sessionParticipants.get(sessionId);
        
        // Check if participant already exists
        boolean exists = participants.stream()
                .anyMatch(p -> participantId.equals(p.get("participantId")));
        
        if (!exists) {
            Map<String, Object> participant = new HashMap<>();
            participant.put("participantId", participantId);
            participant.put("participantName", participantName);
            participant.put("joinedAt", LocalDateTime.now());
            participant.put("status", "connected");
            
            participants.add(participant);
            session.put("participantCount", participants.size());
        }
    }
    
    @Override
    public void removeParticipant(String sessionId, String participantId) {
        logger.debug("Removing participant {} from session {}", participantId, sessionId);
        
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session == null) {
            return; // Session doesn't exist, nothing to remove
        }
        
        List<Map<String, Object>> participants = sessionParticipants.get(sessionId);
        participants.removeIf(p -> participantId.equals(p.get("participantId")));
        session.put("participantCount", participants.size());
    }
    
    @Override
    public List<Map<String, Object>> getActiveParticipants(String sessionId) {
        return sessionParticipants.getOrDefault(sessionId, new ArrayList<>());
    }
    
    @Override
    public List<Map<String, Object>> getActiveSessions() {
        return activeSessions.values().stream()
                .filter(session -> "active".equals(session.get("status")))
                .map(session -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("sessionId", session.get("sessionId"));
                    summary.put("classroomId", session.get("classroomId"));
                    summary.put("classroomName", session.get("classroomName"));
                    summary.put("participantCount", session.get("participantCount"));
                    summary.put("startedAt", session.get("startedAt"));
                    return summary;
                })
                .toList();
    }
    
    @Override
    public List<Map<String, Object>> getSessionsByClassroom(Long classroomId) {
        return activeSessions.values().stream()
                .filter(session -> classroomId.equals(session.get("classroomId")))
                .toList();
    }
    
    @Override
    public String generateRoomId(Long classroomId) {
        return "conference_" + classroomId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Override
    public boolean validateSessionData(Map<String, Object> sessionData) {
        if (sessionData == null) {
            return false;
        }
        
        // Validate max participants
        Object maxParticipants = sessionData.get("maxParticipants");
        if (maxParticipants instanceof Integer && (Integer) maxParticipants < 1) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public Map<String, Object> getWebRTCConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // ICE servers configuration
        List<Map<String, Object>> iceServers = new ArrayList<>();
        
        Map<String, Object> stunServer = new HashMap<>();
        stunServer.put("urls", "stun:stun.l.google.com:19302");
        iceServers.add(stunServer);
        
        // Add more STUN/TURN servers as needed
        Map<String, Object> stunServer2 = new HashMap<>();
        stunServer2.put("urls", "stun:stun1.l.google.com:19302");
        iceServers.add(stunServer2);
        
        config.put("iceServers", iceServers);
        config.put("iceCandidatePoolSize", 10);
        
        return config;
    }
    
    @Override
    public void updateParticipantStatus(String sessionId, String participantId, String status) {
        List<Map<String, Object>> participants = sessionParticipants.get(sessionId);
        if (participants != null) {
            participants.stream()
                    .filter(p -> participantId.equals(p.get("participantId")))
                    .findFirst()
                    .ifPresent(p -> p.put("status", status));
        }
    }
    
    @Override
    public void setScreenSharingEnabled(String sessionId, boolean enabled) {
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session != null) {
            session.put("screenSharingEnabled", enabled);
            logger.info("Screen sharing {} for session {}", enabled ? "enabled" : "disabled", sessionId);
        }
    }
    
    @Override
    public void setRecordingEnabled(String sessionId, boolean enabled) {
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session != null) {
            session.put("recordingEnabled", enabled);
            logger.info("Recording {} for session {}", enabled ? "enabled" : "disabled", sessionId);
        }
    }
    
    @Override
    public Map<String, Object> getSessionStatistics(String sessionId) {
        Map<String, Object> session = activeSessions.get(sessionId);
        if (session == null) {
            return new HashMap<>();
        }
        
        List<Map<String, Object>> participants = sessionParticipants.get(sessionId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("sessionId", sessionId);
        stats.put("totalParticipants", participants.size());
        stats.put("activeParticipants", participants.stream()
                .mapToInt(p -> "connected".equals(p.get("status")) ? 1 : 0)
                .sum());
        stats.put("sessionDuration", calculateSessionDuration(session));
        stats.put("screenSharingEnabled", session.get("screenSharingEnabled"));
        stats.put("recordingEnabled", session.get("recordingEnabled"));
        
        return stats;
    }
    
    /**
     * Calculate session duration in minutes
     */
    private long calculateSessionDuration(Map<String, Object> session) {
        LocalDateTime startedAt = (LocalDateTime) session.get("startedAt");
        if (startedAt == null) {
            return 0;
        }
        
        LocalDateTime endTime = (LocalDateTime) session.getOrDefault("endedAt", LocalDateTime.now());
        return java.time.Duration.between(startedAt, endTime).toMinutes();
    }
}
