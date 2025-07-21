package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Video Conference management
 */
public interface VideoConferenceService {

    /**
     * Create a video conference session for a classroom
     * @param classroomId the classroom ID
     * @param sessionData the session configuration
     * @return the conference session details
     */
    Map<String, Object> createConferenceSession(Long classroomId, Map<String, Object> sessionData);

    /**
     * Start a video conference session
     * @param sessionId the session ID
     * @return the updated session details
     */
    Map<String, Object> startConferenceSession(String sessionId);

    /**
     * End a video conference session
     * @param sessionId the session ID
     * @return the session end result
     */
    Map<String, Object> endConferenceSession(String sessionId);

    /**
     * Get conference session status
     * @param sessionId the session ID
     * @return the session status
     */
    Map<String, Object> getConferenceStatus(String sessionId);

    /**
     * Add participant to conference
     * @param sessionId the session ID
     * @param participantId the participant ID
     * @param participantName the participant name
     */
    void addParticipant(String sessionId, String participantId, String participantName);

    /**
     * Remove participant from conference
     * @param sessionId the session ID
     * @param participantId the participant ID
     */
    void removeParticipant(String sessionId, String participantId);

    /**
     * Get active participants in conference
     * @param sessionId the session ID
     * @return list of active participants
     */
    List<Map<String, Object>> getActiveParticipants(String sessionId);

    /**
     * Get all active conference sessions
     * @return list of active sessions
     */
    List<Map<String, Object>> getActiveSessions();

    /**
     * Get conference sessions for a classroom
     * @param classroomId the classroom ID
     * @return list of conference sessions
     */
    List<Map<String, Object>> getSessionsByClassroom(Long classroomId);

    /**
     * Generate room ID for classroom
     * @param classroomId the classroom ID
     * @return generated room ID
     */
    String generateRoomId(Long classroomId);

    /**
     * Validate conference session data
     * @param sessionData the session data
     * @return true if valid
     */
    boolean validateSessionData(Map<String, Object> sessionData);

    /**
     * Get WebRTC configuration
     * @return WebRTC configuration
     */
    Map<String, Object> getWebRTCConfig();

    /**
     * Update participant status
     * @param sessionId the session ID
     * @param participantId the participant ID
     * @param status the new status
     */
    void updateParticipantStatus(String sessionId, String participantId, String status);

    /**
     * Enable/disable screen sharing for session
     * @param sessionId the session ID
     * @param enabled true to enable, false to disable
     */
    void setScreenSharingEnabled(String sessionId, boolean enabled);

    /**
     * Enable/disable recording for session
     * @param sessionId the session ID
     * @param enabled true to enable, false to disable
     */
    void setRecordingEnabled(String sessionId, boolean enabled);

    /**
     * Get session statistics
     * @param sessionId the session ID
     * @return session statistics
     */
    Map<String, Object> getSessionStatistics(String sessionId);
}