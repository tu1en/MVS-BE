package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.WebRTCRoom;
import com.classroomapp.classroombackend.model.WebRTCSession;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.WebRTCRoomRepository;
import com.classroomapp.classroombackend.repository.WebRTCSessionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebRTCSessionService {
    
    private final WebRTCSessionRepository sessionRepository;
    private final WebRTCRoomRepository roomRepository;
    private final UserRepository userRepository;
    
    public WebRTCSession createSession(String sessionId, String roomId, String userEmail, String userAgent, String ipAddress) {
        try {
            log.info("Creating WebRTC session: {} for room: {} user: {}", sessionId, roomId, userEmail);
            
            // Find or create user
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(userEmail);
            }
            
            // Create session
            WebRTCSession session = new WebRTCSession();
            session.setSessionId(sessionId);
            session.setRoomId(roomId);
            session.setUser(userOpt.orElse(null));
            session.setUserAgent(userAgent);
            session.setIpAddress(ipAddress);
            session.setIsActive(true);
            session.setConnectedAt(LocalDateTime.now());
            
            WebRTCSession savedSession = sessionRepository.save(session);
            
            // Update room participant count
            updateRoomParticipantCount(roomId);
            
            log.info("Successfully created WebRTC session: {}", sessionId);
            return savedSession;
            
        } catch (Exception e) {
            log.error("Error creating WebRTC session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    public WebRTCRoom createOrGetRoom(String roomId, String roomName, String createdBy) {
        try {
            log.info("Creating or getting WebRTC room: {} by: {}", roomId, createdBy);
            
            Optional<WebRTCRoom> existingRoom = roomRepository.findByRoomId(roomId);
            if (existingRoom.isPresent() && existingRoom.get().getIsActive()) {
                return existingRoom.get();
            }
            
            // Create new room
            WebRTCRoom room = new WebRTCRoom();
            room.setRoomId(roomId);
            room.setRoomName(roomName);
            room.setCreatedBy(createdBy);
            room.setIsActive(true);
            room.setCurrentParticipants(0);
            
            WebRTCRoom savedRoom = roomRepository.save(room);
            log.info("Successfully created WebRTC room: {}", roomId);
            return savedRoom;
            
        } catch (Exception e) {
            log.error("Error creating WebRTC room {}: {}", roomId, e.getMessage(), e);
            throw e;
        }
    }
    
    public void disconnectSession(String sessionId) {
        try {
            log.info("Disconnecting WebRTC session: {}", sessionId);
            
            Optional<WebRTCSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isPresent()) {
                WebRTCSession session = sessionOpt.get();
                session.disconnect();
                sessionRepository.save(session);
                
                // Update room participant count
                updateRoomParticipantCount(session.getRoomId());
                
                // Close room if empty
                checkAndCloseEmptyRoom(session.getRoomId());
                
                log.info("Successfully disconnected WebRTC session: {}", sessionId);
            } else {
                log.warn("WebRTC session not found for disconnect: {}", sessionId);
            }
            
        } catch (Exception e) {
            log.error("Error disconnecting WebRTC session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    public List<WebRTCSession> getActiveSessionsInRoom(String roomId) {
        return sessionRepository.findByRoomIdAndIsActiveTrue(roomId);
    }
    
    public List<WebRTCSession> getAllSessionsInRoom(String roomId) {
        return sessionRepository.findByRoomId(roomId);
    }
    
    public Optional<WebRTCSession> getSessionById(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }
    
    public Optional<WebRTCRoom> getRoomById(String roomId) {
        return roomRepository.findByRoomId(roomId);
    }
    
    public long getActiveConnectionCount() {
        return sessionRepository.countByRoomIdAndIsActiveTrue("");
    }
    
    public long getActiveRoomCount() {
        return roomRepository.countByIsActiveTrue();
    }
    
    public int getParticipantCount(String roomId) {
        return (int) sessionRepository.countByRoomIdAndIsActiveTrue(roomId);
    }
    
    private void updateRoomParticipantCount(String roomId) {
        try {
            int activeParticipants = (int) sessionRepository.countByRoomIdAndIsActiveTrue(roomId);
            roomRepository.updateParticipantCount(roomId, activeParticipants);
            log.debug("Updated participant count for room {}: {}", roomId, activeParticipants);
        } catch (Exception e) {
            log.error("Error updating participant count for room {}: {}", roomId, e.getMessage());
        }
    }
    
    private void checkAndCloseEmptyRoom(String roomId) {
        try {
            int activeParticipants = (int) sessionRepository.countByRoomIdAndIsActiveTrue(roomId);
            if (activeParticipants == 0) {
                roomRepository.closeRoom(roomId, LocalDateTime.now());
                log.info("Closed empty WebRTC room: {}", roomId);
            }
        } catch (Exception e) {
            log.error("Error closing empty room {}: {}", roomId, e.getMessage());
        }
    }
    
    @Transactional
    public void cleanupStaleConnections() {
        try {
            log.info("Starting cleanup of stale WebRTC connections");
            
            // Find sessions that have been active for more than 24 hours (possible stale connections)
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            List<WebRTCSession> staleSessions = sessionRepository.findStaleActiveSessions(cutoffTime);
            
            for (WebRTCSession session : staleSessions) {
                log.warn("Cleaning up stale session: {} in room: {}", session.getSessionId(), session.getRoomId());
                disconnectSession(session.getSessionId());
            }
            
            // Clean up old inactive sessions (older than 7 days)
            LocalDateTime oldSessionCutoff = LocalDateTime.now().minusDays(7);
            sessionRepository.deleteOldInactiveSessions(oldSessionCutoff);
            
            // Clean up old closed rooms (older than 7 days)
            roomRepository.deleteOldClosedRooms(oldSessionCutoff);
            
            log.info("Completed cleanup of stale WebRTC connections");
            
        } catch (Exception e) {
            log.error("Error during WebRTC cleanup: {}", e.getMessage(), e);
        }
    }
    
    @Transactional
    public void disconnectAllSessionsInRoom(String roomId) {
        try {
            log.info("Disconnecting all sessions in room: {}", roomId);
            sessionRepository.disconnectAllSessionsInRoom(roomId, LocalDateTime.now());
            roomRepository.closeRoom(roomId, LocalDateTime.now());
            log.info("Successfully disconnected all sessions in room: {}", roomId);
        } catch (Exception e) {
            log.error("Error disconnecting all sessions in room {}: {}", roomId, e.getMessage(), e);
        }
    }
}