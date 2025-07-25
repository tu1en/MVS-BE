package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.model.WhiteboardData;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Repository cho WhiteboardData
 */
@Repository
public interface WhiteboardDataRepository extends JpaRepository<WhiteboardData, Long> {
    
    /**
     * TÃ¬m táº¥t cáº£ whiteboard data cho live stream
     */
    List<WhiteboardData> findByLiveStreamAndIsActiveTrueOrderBySequenceNumberAsc(LiveStream liveStream);
    
    /**
     * TÃ¬m whiteboard data theo element ID
     */
    Optional<WhiteboardData> findByLiveStreamAndElementIdAndIsActiveTrue(LiveStream liveStream, String elementId);
    
    /**
     * TÃ¬m whiteboard data theo user
     */
    List<WhiteboardData> findByLiveStreamAndUserAndIsActiveTrueOrderByCreatedAtAsc(LiveStream liveStream, User user);
    
    /**
     * TÃ¬m whiteboard data Ä‘Æ°á»£c táº¡o sau thá»i Ä‘iá»ƒm cá»¥ thá»ƒ
     */
    List<WhiteboardData> findByLiveStreamAndCreatedAtAfterAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, LocalDateTime after);
    
    /**
     * TÃ¬m whiteboard data theo sequence number
     */
    List<WhiteboardData> findByLiveStreamAndSequenceNumberGreaterThanAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, Long sequenceNumber);
    
    /**
     * Láº¥y sequence number lá»›n nháº¥t cho live stream
     */
    @Query("SELECT COALESCE(MAX(w.sequenceNumber), 0) FROM WhiteboardData w WHERE w.liveStream = :liveStream")
    Long getMaxSequenceNumber(@Param("liveStream") LiveStream liveStream);
    
    /**
     * Äáº¿m sá»‘ elements Ä‘ang active trong whiteboard
     */
    @Query("SELECT COUNT(w) FROM WhiteboardData w WHERE w.liveStream = :liveStream AND w.isActive = true")
    long countActiveElements(@Param("liveStream") LiveStream liveStream);
    
    /**
     * TÃ¬m elements theo layer
     */
    List<WhiteboardData> findByLiveStreamAndLayerIndexAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, Integer layerIndex);
    
    /**
     * XÃ³a táº¥t cáº£ whiteboard data cho live stream (soft delete)
     */
    @Modifying
    @Query("UPDATE WhiteboardData w SET w.isActive = false, w.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE w.liveStream = :liveStream")
    int clearWhiteboardData(@Param("liveStream") LiveStream liveStream);
    
    /**
     * XÃ³a element cá»¥ thá»ƒ (soft delete)
     */
    @Modifying
    @Query("UPDATE WhiteboardData w SET w.isActive = false, w.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE w.liveStream = :liveStream AND w.elementId = :elementId")
    int deleteElement(@Param("liveStream") LiveStream liveStream, @Param("elementId") String elementId);
    
    /**
     * TÃ¬m elements bá»Ÿi action type
     */
    List<WhiteboardData> findByLiveStreamAndActionTypeAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, WhiteboardData.DrawingAction actionType);
    
    /**
     * TÃ¬m elements bá»Ÿi tool type
     */
    List<WhiteboardData> findByLiveStreamAndToolTypeAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, WhiteboardData.DrawingTool toolType);
    
    /**
     * Láº¥y whiteboard state snapshot cho live stream
     */
    @Query("SELECT w FROM WhiteboardData w WHERE w.liveStream = :liveStream AND w.isActive = true " +
           "AND w.actionType IN ('ADD_SHAPE', 'ADD_TEXT', 'DRAW') ORDER BY w.sequenceNumber ASC")
    List<WhiteboardData> getWhiteboardSnapshot(@Param("liveStream") LiveStream liveStream);
    
    /**
     * TÃ¬m recent drawing operations
     */
    @Query("SELECT w FROM WhiteboardData w WHERE w.liveStream = :liveStream " +
           "AND w.createdAt >= :since AND w.isActive = true ORDER BY w.sequenceNumber ASC")
    List<WhiteboardData> getRecentOperations(@Param("liveStream") LiveStream liveStream, 
                                           @Param("since") LocalDateTime since);
    
    /**
     * Cáº­p nháº­t element position/properties
     */
    @Modifying
    @Query("UPDATE WhiteboardData w SET w.xCoordinate = :x, w.yCoordinate = :y, " +
           "w.width = :width, w.height = :height, w.rotation = :rotation, " +
           "w.updatedAt = CURRENT_TIMESTAMP WHERE w.liveStream = :liveStream AND w.elementId = :elementId")
    int updateElementTransform(@Param("liveStream") LiveStream liveStream, 
                              @Param("elementId") String elementId,
                              @Param("x") Double x, @Param("y") Double y,
                              @Param("width") Double width, @Param("height") Double height,
                              @Param("rotation") Double rotation);
    
    /**
     * Cleanup old whiteboard data (hard delete)
     */
    @Modifying
    @Query("DELETE FROM WhiteboardData w WHERE w.liveStream = :liveStream AND w.isActive = false " +
           "AND w.updatedAt < :before")
    int cleanupOldData(@Param("liveStream") LiveStream liveStream, @Param("before") LocalDateTime before);
    
    /**
     * Get statistics cho whiteboard usage
     */
    @Query("SELECT w.user.id as userId, w.user.fullName as userName, COUNT(w) as elementCount " +
           "FROM WhiteboardData w WHERE w.liveStream = :liveStream AND w.isActive = true " +
           "GROUP BY w.user.id, w.user.fullName ORDER BY elementCount DESC")
    List<Object[]> getWhiteboardUsageStatistics(@Param("liveStream") LiveStream liveStream);
}
