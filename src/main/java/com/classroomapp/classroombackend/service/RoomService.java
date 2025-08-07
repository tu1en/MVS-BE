package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.*;

import java.util.List;

public interface RoomService {
    
    /**
     * Get all active rooms
     */
    List<RoomDto> getAllRooms();
    
    /**
     * Get all rooms with optional filtering
     */
    List<RoomDto> getAllRooms(RoomSearchRequestDto searchRequest);
    
    /**
     * Get room by ID
     */
    RoomDto getRoomById(Long roomId);
    
    /**
     * Get room by room code
     */
    RoomDto getRoomByCode(String roomCode);
    
    /**
     * Create new room
     */
    RoomDto createRoom(RoomDto roomDto);
    
    /**
     * Update existing room
     */
    RoomDto updateRoom(Long roomId, RoomDto roomDto);
    
    /**
     * Delete room (soft delete - set inactive)
     */
    void deleteRoom(Long roomId);
    
    /**
     * Check if room is available at specified time
     */
    RoomAvailabilityResponseDto checkRoomAvailability(RoomAvailabilityRequestDto request);
    
    /**
     * Search for available rooms at specified time with optional filters
     */
    List<RoomDto> getAvailableRooms(RoomSearchRequestDto searchRequest);
    
    /**
     * Get room schedule for a specific date range
     */
    List<RoomScheduleDto> getRoomSchedule(Long roomId, String startDate, String endDate);
    
    /**
     * Get all schedules for multiple rooms
     */
    List<RoomScheduleDto> getMultipleRoomsSchedule(List<Long> roomIds, String startDate, String endDate);
    
    /**
     * Search rooms by name or code
     */
    List<RoomDto> searchRooms(String keyword);
    
    /**
     * Get rooms by capacity range
     */
    List<RoomDto> getRoomsByCapacity(Integer minCapacity, Integer maxCapacity);
    
    /**
     * Get rooms by location
     */
    List<RoomDto> getRoomsByLocation(String location);
    
    /**
     * Get room statistics
     */
    RoomStatsDto getRoomStatistics();
}