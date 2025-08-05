package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByRoomCode(String roomCode);
    
    boolean existsByRoomCode(String roomCode);
    
    List<Room> findByIsActiveTrueOrderByRoomCodeAsc();
    
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
    
    List<Room> findByIsActiveTrueAndCapacityGreaterThanEqual(Integer capacity);
    
    @Query("SELECT r FROM Room r WHERE r.isActive = true ORDER BY r.roomCode ASC")
    List<Room> findAllActiveRooms();
    
    @Query("SELECT r FROM Room r WHERE r.roomName LIKE CONCAT('%', :keyword, '%') OR r.roomCode LIKE CONCAT('%', :keyword, '%')")
    List<Room> findByRoomNameContainingOrRoomCodeContainingIgnoreCase(@Param("keyword") String keyword);
    
    List<Room> findByLocationContainingIgnoreCase(String location);
    
    long countByIsActiveTrue();
    
    @Query("SELECT r FROM Room r WHERE r.capacity >= :minCapacity AND r.capacity <= :maxCapacity")
    List<Room> findRoomsByCapacityRange(@Param("minCapacity") int minCapacity, @Param("maxCapacity") int maxCapacity);
    
    List<Room> findTop10ByOrderByCreatedAtDesc();
    
    @Query(value = "SELECT * FROM rooms WHERE is_active = 1 ", nativeQuery = true)
    List<Room> findAvailableRooms();
}