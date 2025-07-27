package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.WebRTCSession;

@Repository
public interface WebRTCSessionRepository extends JpaRepository<WebRTCSession, Long> {
    
    Optional<WebRTCSession> findBySessionId(String sessionId);
    
    List<WebRTCSession> findByRoomIdAndIsActiveTrue(String roomId);
    
    List<WebRTCSession> findByRoomId(String roomId);
    
    List<WebRTCSession> findByUserId(Long userId);
    
    List<WebRTCSession> findByUserIdAndIsActiveTrue(Long userId);
    
    long countByRoomIdAndIsActiveTrue(String roomId);
    
    @Modifying
    @Query("UPDATE WebRTCSession s SET s.isActive = false, s.disconnectedAt = :disconnectedAt WHERE s.roomId = :roomId AND s.isActive = true")
    void disconnectAllSessionsInRoom(@Param("roomId") String roomId, @Param("disconnectedAt") LocalDateTime disconnectedAt);
    
    @Modifying
    @Query("UPDATE WebRTCSession s SET s.isActive = false, s.disconnectedAt = :disconnectedAt WHERE s.sessionId = :sessionId")
    void disconnectSession(@Param("sessionId") String sessionId, @Param("disconnectedAt") LocalDateTime disconnectedAt);
    
    @Query("SELECT s FROM WebRTCSession s WHERE s.isActive = true AND s.connectedAt < :cutoffTime")
    List<WebRTCSession> findStaleActiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Modifying
    @Query("DELETE FROM WebRTCSession s WHERE s.isActive = false AND s.disconnectedAt < :cutoffTime")
    void deleteOldInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}