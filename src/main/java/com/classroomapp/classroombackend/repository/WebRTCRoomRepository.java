package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.WebRTCRoom;

@Repository
public interface WebRTCRoomRepository extends JpaRepository<WebRTCRoom, Long> {
    
    Optional<WebRTCRoom> findByRoomId(String roomId);
    
    List<WebRTCRoom> findByIsActiveTrue();
    
    List<WebRTCRoom> findByCreatedBy(String createdBy);
    
    List<WebRTCRoom> findByCreatedByAndIsActiveTrue(String createdBy);
    
    long countByIsActiveTrue();
    
    @Modifying
    @Query("UPDATE WebRTCRoom r SET r.isActive = false, r.closedAt = :closedAt WHERE r.roomId = :roomId")
    void closeRoom(@Param("roomId") String roomId, @Param("closedAt") LocalDateTime closedAt);
    
    @Query("SELECT r FROM WebRTCRoom r WHERE r.isActive = true AND r.currentParticipants = 0 AND r.createdAt < :cutoffTime")
    List<WebRTCRoom> findEmptyRoomsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Modifying
    @Query("UPDATE WebRTCRoom r SET r.currentParticipants = :count WHERE r.roomId = :roomId")
    void updateParticipantCount(@Param("roomId") String roomId, @Param("count") int count);
    
    @Modifying
    @Query("DELETE FROM WebRTCRoom r WHERE r.isActive = false AND r.closedAt < :cutoffTime")
    void deleteOldClosedRooms(@Param("cutoffTime") LocalDateTime cutoffTime);
}