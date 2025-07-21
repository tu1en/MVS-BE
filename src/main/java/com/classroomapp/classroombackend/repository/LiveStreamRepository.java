package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.LiveStream;

/**
 * Repository interface for LiveStream entity
 */
@Repository
public interface LiveStreamRepository extends JpaRepository<LiveStream, Long> {
    
    /**
     * Find live stream by lecture ID
     * @param lectureId the lecture ID
     * @return Optional containing the live stream if found
     */
    Optional<LiveStream> findByLectureId(Long lectureId);
    
    /**
     * Find active live stream by lecture ID
     * @param lectureId the lecture ID
     * @return Optional containing the active live stream if found
     */
    @Query("SELECT ls FROM LiveStream ls WHERE ls.lectureId = :lectureId AND ls.status = 'LIVE'")
    Optional<LiveStream> findActiveLiveStreamByLectureId(@Param("lectureId") Long lectureId);
    
    /**
     * Find live stream by stream key
     * @param streamKey the unique stream key
     * @return Optional containing the live stream if found
     */
    Optional<LiveStream> findByStreamKey(String streamKey);
    
    /**
     * Find all active live streams
     * @return List of active live streams
     */
    @Query("SELECT ls FROM LiveStream ls WHERE ls.status = 'LIVE' ORDER BY ls.startTime DESC")
    List<LiveStream> findActiveLiveStreams();
    
    /**
     * Find all live streams by status
     * @param status the stream status
     * @return List of live streams with the specified status
     */
    List<LiveStream> findByStatusOrderByStartTimeDesc(LiveStream.StreamStatus status);
    
    /**
     * Find live streams by lecture ID ordered by start time
     * @param lectureId the lecture ID
     * @return List of live streams for the lecture
     */
    List<LiveStream> findByLectureIdOrderByStartTimeDesc(Long lectureId);
    
    /**
     * Find live streams within date range
     * @param startDate the start date
     * @param endDate the end date
     * @return List of live streams within the date range
     */
    @Query("SELECT ls FROM LiveStream ls WHERE ls.startTime >= :startDate AND ls.startTime <= :endDate ORDER BY ls.startTime DESC")
    List<LiveStream> findByStartTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Check if there's an active live stream for a lecture
     * @param lectureId the lecture ID
     * @return true if there's an active live stream
     */
    @Query("SELECT COUNT(ls) > 0 FROM LiveStream ls WHERE ls.lectureId = :lectureId AND ls.status IN ('SCHEDULED', 'LIVE')")
    boolean hasActiveLiveStream(@Param("lectureId") Long lectureId);
    
    /**
     * Find live streams that need to be ended (past end time but still active)
     * @param currentTime the current time
     * @return List of live streams that should be ended
     */
    @Query("SELECT ls FROM LiveStream ls WHERE ls.endTime < :currentTime AND ls.status = 'LIVE'")
    List<LiveStream> findLiveStreamsToEnd(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get total viewer count for all active streams
     * @return Total current viewers across all active streams
     */
    @Query("SELECT COALESCE(SUM(ls.currentViewers), 0) FROM LiveStream ls WHERE ls.status = 'LIVE'")
    Integer getTotalActiveViewers();
    
    /**
     * Find live streams with recording enabled
     * @return List of live streams with recording enabled
     */
    @Query("SELECT ls FROM LiveStream ls WHERE ls.recordingEnabled = true ORDER BY ls.startTime DESC")
    List<LiveStream> findRecordingEnabledStreams();
    
    /**
     * Find live streams by classroom (through lecture relationship)
     * @param classroomId the classroom ID
     * @return List of live streams for the classroom
     */
    @Query("SELECT ls FROM LiveStream ls JOIN ls.lecture l WHERE l.classroom.id = :classroomId ORDER BY ls.startTime DESC")
    List<LiveStream> findByClassroomId(@Param("classroomId") Long classroomId);
    
    /**
     * Update current viewer count
     * @param streamId the stream ID
     * @param viewerCount the new viewer count
     */
    @Query("UPDATE LiveStream ls SET ls.currentViewers = :viewerCount WHERE ls.id = :streamId")
    void updateViewerCount(@Param("streamId") Long streamId, @Param("viewerCount") Integer viewerCount);
    
    /**
     * Find streams that exceeded max viewers
     * @return List of streams that exceeded their max viewer limit
     */
    @Query("SELECT ls FROM LiveStream ls WHERE ls.currentViewers > ls.maxViewers AND ls.status = 'LIVE'")
    List<LiveStream> findStreamsExceedingMaxViewers();
}
