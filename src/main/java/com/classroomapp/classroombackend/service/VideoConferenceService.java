package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.Map;

public interface VideoConferenceService {
    // Basic
    void startConference(Long roomId);
    void endConference(Long roomId);
    boolean isConferenceActive(Long roomId);

    // Extended
    Map<String, Object> createConferenceSession(Long classroomId, Map<String, Object> sessionData);
    Map<String, Object> startConferenceSession(String sessionId);
    Map<String, Object> endConferenceSession(String sessionId);
    Map<String, Object> getConferenceStatus(String sessionId);
    void addParticipant(String sessionId, String participantId, String participantName);
    void removeParticipant(String sessionId, String participantId);
    List<Map<String, Object>> getActiveParticipants(String sessionId);
    List<Map<String, Object>> getActiveSessions();
    List<Map<String, Object>> getSessionsByClassroom(Long classroomId);
    String generateRoomId(Long classroomId);
    boolean validateSessionData(Map<String, Object> sessionData);
    Map<String, Object> getWebRTCConfig();
    void updateParticipantStatus(String sessionId, String participantId, String status);
    void setScreenSharingEnabled(String sessionId, boolean enabled);
    void setRecordingEnabled(String sessionId, boolean enabled);
    Map<String, Object> getSessionStatistics(String sessionId);
}
