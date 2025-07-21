package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.model.LiveStream;

/**
 * Service interface for LiveStream management
 */
public interface LiveStreamService {
    
    /**
     * Create a new live stream for a lecture
     * @param lectureId the lecture ID
     * @param streamConfig the stream configuration
     * @return the created live stream DTO
     */
    LiveStreamDto createLiveStream(Long lectureId, LiveStreamDto streamConfig);
    
    /**
     * Start a live stream
     * @param streamId the stream ID
     * @return the updated live stream DTO
     */
    LiveStreamDto startLiveStream(Long streamId);
    
    /**
     * Stop a live stream
     * @param streamId the stream ID
     * @return the updated live stream DTO
     */
    LiveStreamDto stopLiveStream(Long streamId);
    
    /**
     * Get live stream by ID
     * @param streamId the stream ID
     * @return the live stream DTO
     */
    LiveStreamDto getLiveStreamById(Long streamId);
    
    /**
     * Get active live stream for a lecture
     * @param lectureId the lecture ID
     * @return the active live stream DTO or null if none
     */
    LiveStreamDto getActiveLiveStreamByLectureId(Long lectureId);
    
    /**
     * Get all live streams for a lecture
     * @param lectureId the lecture ID
     * @return list of live stream DTOs
     */
    List<LiveStreamDto> getLiveStreamsByLectureId(Long lectureId);
    
    /**
     * Get all active live streams
     * @return list of active live stream DTOs
     */
    List<LiveStreamDto> getActiveLiveStreams();
    
    /**
     * Update viewer count for a live stream
     * @param streamId the stream ID
     * @param viewerCount the new viewer count
     */
    void updateViewerCount(Long streamId, Integer viewerCount);
    
    /**
     * Add viewer to a live stream
     * @param streamId the stream ID
     * @param viewerUsername the viewer username
     */
    void addViewer(Long streamId, String viewerUsername);
    
    /**
     * Remove viewer from a live stream
     * @param streamId the stream ID
     * @param viewerUsername the viewer username
     */
    void removeViewer(Long streamId, String viewerUsername);
    
    /**
     * Get live stream by stream key
     * @param streamKey the unique stream key
     * @return the live stream DTO
     */
    LiveStreamDto getLiveStreamByStreamKey(String streamKey);
    
    /**
     * Update live stream configuration
     * @param streamId the stream ID
     * @param streamConfig the updated configuration
     * @return the updated live stream DTO
     */
    LiveStreamDto updateLiveStream(Long streamId, LiveStreamDto streamConfig);
    
    /**
     * Delete a live stream
     * @param streamId the stream ID
     */
    void deleteLiveStream(Long streamId);
    
    /**
     * Check if lecture has active live stream
     * @param lectureId the lecture ID
     * @return true if has active live stream
     */
    boolean hasActiveLiveStream(Long lectureId);
    
    /**
     * End expired live streams
     * @return number of streams ended
     */
    int endExpiredLiveStreams();
    
    /**
     * Get total active viewers across all streams
     * @return total viewer count
     */
    Integer getTotalActiveViewers();
    
    /**
     * Get live streams for a classroom
     * @param classroomId the classroom ID
     * @return list of live stream DTOs
     */
    List<LiveStreamDto> getLiveStreamsByClassroomId(Long classroomId);
    
    /**
     * Generate unique stream key
     * @param lectureId the lecture ID
     * @return unique stream key
     */
    String generateStreamKey(Long lectureId);
    
    /**
     * Validate stream configuration
     * @param streamConfig the stream configuration
     * @return true if valid
     */
    boolean validateStreamConfig(LiveStreamDto streamConfig);
}
