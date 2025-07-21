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
     * Tìm tất cả whiteboard data cho live stream
     */
    List<WhiteboardData> findByLiveStreamAndIsActiveTrueOrderBySequenceNumberAsc(LiveStream liveStream);
    
    /**
     * Tìm whiteboard data theo element ID
     */
    Optional<WhiteboardData> findByLiveStreamAndElementIdAndIsActiveTrue(LiveStream liveStream, String elementId);
    
    /**
     * Tìm whiteboard data theo user
     */
    List<WhiteboardData> findByLiveStreamAndUserAndIsActiveTrueOrderByCreatedAtAsc(LiveStream liveStream, User user);
    
    /**
     * Tìm whiteboard data được tạo sau thời điểm cụ thể
     */
    List<WhiteboardData> findByLiveStreamAndCreatedAtAfterAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, LocalDateTime after);
    
    /**
     * Tìm whiteboard data theo sequence number
     */
    List<WhiteboardData> findByLiveStreamAndSequenceNumberGreaterThanAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, Long sequenceNumber);
    
    /**
     * Lấy sequence number lớn nhất cho live stream
     */
    @Query("SELECT COALESCE(MAX(w.sequenceNumber), 0) FROM WhiteboardData w WHERE w.liveStream = :liveStream")
    Long getMaxSequenceNumber(@Param("liveStream") LiveStream liveStream);
    
    /**
     * Đếm số elements đang active trong whiteboard
     */
    @Query("SELECT COUNT(w) FROM WhiteboardData w WHERE w.liveStream = :liveStream AND w.isActive = true")
    long countActiveElements(@Param("liveStream") LiveStream liveStream);
    
    /**
     * Tìm elements theo layer
     */
    List<WhiteboardData> findByLiveStreamAndLayerIndexAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, Integer layerIndex);
    
    /**
     * Xóa tất cả whiteboard data cho live stream (soft delete)
     */
    @Modifying
    @Query("UPDATE WhiteboardData w SET w.isActive = false, w.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE w.liveStream = :liveStream")
    int clearWhiteboardData(@Param("liveStream") LiveStream liveStream);
    
    /**
     * Xóa element cụ thể (soft delete)
     */
    @Modifying
    @Query("UPDATE WhiteboardData w SET w.isActive = false, w.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE w.liveStream = :liveStream AND w.elementId = :elementId")
    int deleteElement(@Param("liveStream") LiveStream liveStream, @Param("elementId") String elementId);
    
    /**
     * Tìm elements bởi action type
     */
    List<WhiteboardData> findByLiveStreamAndActionTypeAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, WhiteboardData.DrawingAction actionType);
    
    /**
     * Tìm elements bởi tool type
     */
    List<WhiteboardData> findByLiveStreamAndToolTypeAndIsActiveTrueOrderBySequenceNumberAsc(
            LiveStream liveStream, WhiteboardData.DrawingTool toolType);
    
    /**
     * Lấy whiteboard state snapshot cho live stream
     */
    @Query("SELECT w FROM WhiteboardData w WHERE w.liveStream = :liveStream AND w.isActive = true " +
           "AND w.actionType IN ('ADD_SHAPE', 'ADD_TEXT', 'DRAW') ORDER BY w.sequenceNumber ASC")
    List<WhiteboardData> getWhiteboardSnapshot(@Param("liveStream") LiveStream liveStream);
    
    /**
     * Tìm recent drawing operations
     */
    @Query("SELECT w FROM WhiteboardData w WHERE w.liveStream = :liveStream " +
           "AND w.createdAt >= :since AND w.isActive = true ORDER BY w.sequenceNumber ASC")
    List<WhiteboardData> getRecentOperations(@Param("liveStream") LiveStream liveStream, 
                                           @Param("since") LocalDateTime since);
    
    /**
     * Cập nhật element position/properties
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