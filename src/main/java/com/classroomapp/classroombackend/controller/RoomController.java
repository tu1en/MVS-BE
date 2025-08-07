package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.*;
import com.classroomapp.classroombackend.service.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoomController {
    
    private final RoomService roomService;
    
    /**
     * Get all rooms with optional filtering
     * GET /api/rooms
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRooms(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long excludeClassId
    ) {
        log.info("🔍 RoomController.getAllRooms called with filters - building: {}, type: {}, minCapacity: {}", 
                 building, type, minCapacity);
        
        try {
            RoomSearchRequestDto searchRequest = new RoomSearchRequestDto();
            searchRequest.setBuilding(building);
            searchRequest.setType(type);
            searchRequest.setLocation(location);
            searchRequest.setMinCapacity(minCapacity);
            searchRequest.setStatus(status != null ? status : "active");
            searchRequest.setDate(date);
            searchRequest.setStartTime(startTime);
            searchRequest.setEndTime(endTime);
            searchRequest.setExcludeClassId(excludeClassId);
            
            List<RoomDto> rooms = roomService.getAllRooms(searchRequest);
            log.info("✅ Successfully retrieved {} rooms", rooms.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rooms);
            response.put("total", rooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error retrieving rooms: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get room by ID
     * GET /api/rooms/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoomById(@PathVariable Long id) {
        log.info("🔍 RoomController.getRoomById called with ID: {}", id);
        
        try {
            RoomDto room = roomService.getRoomById(id);
            log.info("✅ Successfully retrieved room: {}", room.getRoomCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", room);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error retrieving room by ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create new room
     * POST /api/rooms
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoom(@Valid @RequestBody RoomDto roomDto) {
        log.info("📝 RoomController.createRoom called for room code: {}", roomDto.getRoomCode());
        
        try {
            RoomDto createdRoom = roomService.createRoom(roomDto);
            log.info("✅ Successfully created room: {}", createdRoom.getRoomCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdRoom);
            response.put("message", "Room created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error creating room: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update existing room
     * PUT /api/rooms/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        log.info("📝 RoomController.updateRoom called for room ID: {}", id);
        
        try {
            RoomDto updatedRoom = roomService.updateRoom(id, roomDto);
            log.info("✅ Successfully updated room: {}", updatedRoom.getRoomCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedRoom);
            response.put("message", "Room updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error updating room ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete room (soft delete)
     * DELETE /api/rooms/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable Long id) {
        log.info("🗑️ RoomController.deleteRoom called for room ID: {}", id);
        
        try {
            roomService.deleteRoom(id);
            log.info("✅ Successfully deleted room ID: {}", id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error deleting room ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check room availability
     * POST /api/rooms/check-availability
     */
    @PostMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkRoomAvailability(@Valid @RequestBody RoomAvailabilityRequestDto request) {
        log.info("🔍 RoomController.checkRoomAvailability called for room {} on {} at {}-{}", 
                 request.getRoomId(), request.getDate(), request.getStartTime(), request.getEndTime());
        
        try {
            RoomAvailabilityResponseDto availability = roomService.checkRoomAvailability(request);
            log.info("✅ Room availability check completed: {}", availability.isAvailable() ? "Available" : "Not Available");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", availability);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error checking room availability: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Search for available rooms
     * POST /api/rooms/search-available
     */
    @PostMapping("/search-available")
    public ResponseEntity<Map<String, Object>> searchAvailableRooms(@Valid @RequestBody RoomSearchRequestDto searchRequest) {
        log.info("🔍 RoomController.searchAvailableRooms called with criteria: {}", searchRequest);
        
        try {
            List<RoomDto> availableRooms = roomService.getAvailableRooms(searchRequest);
            log.info("✅ Found {} available rooms", availableRooms.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", availableRooms);
            response.put("total", availableRooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error searching available rooms: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get room schedule
     * GET /api/rooms/{id}/schedule
     */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<Map<String, Object>> getRoomSchedule(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        log.info("🔍 RoomController.getRoomSchedule called for room {} from {} to {}", id, startDate, endDate);
        
        try {
            List<RoomScheduleDto> schedule = roomService.getRoomSchedule(id, startDate, endDate);
            log.info("✅ Retrieved {} schedule items for room {}", schedule.size(), id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", schedule);
            response.put("total", schedule.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting room schedule for ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Search rooms by keyword
     * GET /api/rooms/search
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRooms(@RequestParam String keyword) {
        log.info("🔍 RoomController.searchRooms called with keyword: {}", keyword);
        
        try {
            List<RoomDto> rooms = roomService.searchRooms(keyword);
            log.info("✅ Found {} rooms matching keyword: {}", rooms.size(), keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rooms);
            response.put("total", rooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error searching rooms with keyword {}: {}", keyword, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get room statistics
     * GET /api/rooms/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRoomStatistics() {
        log.info("🔍 RoomController.getRoomStatistics called");
        
        try {
            RoomStatsDto stats = roomService.getRoomStatistics();
            log.info("✅ Retrieved room statistics");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting room statistics: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get rooms by capacity range
     * GET /api/rooms/capacity
     */
    @GetMapping("/capacity")
    public ResponseEntity<Map<String, Object>> getRoomsByCapacity(
            @RequestParam Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity
    ) {
        log.info("🔍 RoomController.getRoomsByCapacity called with range: {}-{}", minCapacity, maxCapacity);
        
        try {
            maxCapacity = maxCapacity != null ? maxCapacity : Integer.MAX_VALUE;
            List<RoomDto> rooms = roomService.getRoomsByCapacity(minCapacity, maxCapacity);
            log.info("✅ Found {} rooms in capacity range", rooms.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rooms);
            response.put("total", rooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting rooms by capacity: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}