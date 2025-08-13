package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.*;
import com.classroomapp.classroombackend.entity.Room;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.repository.RoomRepository;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.service.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {
    
    private final RoomRepository roomRepository;
    private final ClassRepository classRepository;
    
    @Override
    public List<RoomDto> getAllRooms() {
        log.info("Getting all active rooms");
        List<Room> rooms = roomRepository.findByIsActiveTrueOrderByRoomCodeAsc();
        return rooms.stream()
                   .map(this::convertToDto)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomDto> getAllRooms(RoomSearchRequestDto searchRequest) {
        log.info("Getting rooms with search criteria: {}", searchRequest);
        
        List<Room> rooms;
        
        // Apply status filter
        if (searchRequest.getStatus() != null && searchRequest.getStatus().equals("all")) {
            rooms = roomRepository.findAll();
        } else {
            rooms = roomRepository.findByIsActiveTrueOrderByRoomCodeAsc();
        }
        
        // Apply capacity filter
        if (searchRequest.getMinCapacity() != null) {
            rooms = rooms.stream()
                        .filter(room -> room.getCapacity() >= searchRequest.getMinCapacity())
                        .collect(Collectors.toList());
        }
        
        // Apply location filter
        if (searchRequest.getLocation() != null && !searchRequest.getLocation().isEmpty()) {
            rooms = rooms.stream()
                        .filter(room -> room.getLocation() != null && 
                                      room.getLocation().toLowerCase().contains(searchRequest.getLocation().toLowerCase()))
                        .collect(Collectors.toList());
        }
        
        // Apply building filter (extracted from location or room code)
        if (searchRequest.getBuilding() != null && !searchRequest.getBuilding().isEmpty()) {
            rooms = rooms.stream()
                        .filter(room -> extractBuilding(room).equals(searchRequest.getBuilding()))
                        .collect(Collectors.toList());
        }
        
        return rooms.stream()
                   .map(this::convertToDto)
                   .collect(Collectors.toList());
    }
    
    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting room by ID: {}", roomId);
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isPresent()) {
            return convertToDto(room.get());
        }
        throw new RuntimeException("Không tìm thấy phòng với ID: " + roomId);
    }
    
    @Override
    public RoomDto getRoomByCode(String roomCode) {
        log.info("Getting room by code: {}", roomCode);
        Optional<Room> room = roomRepository.findByRoomCode(roomCode);
        if (room.isPresent()) {
            return convertToDto(room.get());
        }
        throw new RuntimeException("Không tìm thấy phòng với mã: " + roomCode);
    }
    
    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        log.info("Creating new room: {}", roomDto.getRoomCode());
        
        // Check if room code already exists
        if (roomRepository.existsByRoomCode(roomDto.getRoomCode())) {
            throw new RuntimeException("Phòng với mã " + roomDto.getRoomCode() + " đã tồn tại");
        }
        
        Room room = convertToEntity(roomDto);
        room.setIsActive(true);
        Room savedRoom = roomRepository.save(room);
        
        log.info("Successfully created room with ID: {}", savedRoom.getId());
        return convertToDto(savedRoom);
    }
    
    @Override
    public RoomDto updateRoom(Long roomId, RoomDto roomDto) {
        log.info("Updating room with ID: {}", roomId);
        
        Optional<Room> existingRoom = roomRepository.findById(roomId);
        if (!existingRoom.isPresent()) {
            throw new RuntimeException("Không tìm thấy phòng với ID: " + roomId);
        }
        
        Room room = existingRoom.get();
        
        // Check if room code is being changed and if new code already exists
        if (!room.getRoomCode().equals(roomDto.getRoomCode()) && 
            roomRepository.existsByRoomCode(roomDto.getRoomCode())) {
            throw new RuntimeException("Phòng với mã " + roomDto.getRoomCode() + " đã tồn tại");
        }
        
        // Update fields
        room.setRoomCode(roomDto.getRoomCode());
        room.setRoomName(roomDto.getRoomName());
        room.setCapacity(roomDto.getCapacity());
        room.setLocation(roomDto.getLocation());
        room.setFacilities(roomDto.getFacilities());
        if (roomDto.getIsActive() != null) {
            room.setIsActive(roomDto.getIsActive());
        }
        
        Room savedRoom = roomRepository.save(room);
        log.info("Successfully updated room with ID: {}", savedRoom.getId());
        return convertToDto(savedRoom);
    }
    
    @Override
    public void deleteRoom(Long roomId) {
        log.info("Soft deleting room with ID: {}", roomId);
        
        Optional<Room> room = roomRepository.findById(roomId);
        if (!room.isPresent()) {
            throw new RuntimeException("Không tìm thấy phòng với ID: " + roomId);
        }
        
        Room roomEntity = room.get();
        roomEntity.setIsActive(false);
        roomRepository.save(roomEntity);
        
        log.info("Successfully deactivated room with ID: {}", roomId);
    }
    
    @Override
    public RoomAvailabilityResponseDto checkRoomAvailability(RoomAvailabilityRequestDto request) {
        log.info("Checking room availability: Room {}, Date {}, Time {}-{}", 
                 request.getRoomId(), request.getDate(), request.getStartTime(), request.getEndTime());
        
        try {
            LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime startTime = LocalTime.parse(request.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(request.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
            
            // Check if room exists and is active
            Optional<Room> room = roomRepository.findById(request.getRoomId());
            if (!room.isPresent() || !room.get().getIsActive()) {
                return new RoomAvailabilityResponseDto(false, "Không tìm thấy phòng hoặc phòng đang bị vô hiệu hóa");
            }
            
            // Check for conflicting classes
            List<ClassEntity> conflictingClasses = classRepository.findConflictingClasses(
                request.getRoomId(), date, request.getExcludeClassId());
            
            if (conflictingClasses.isEmpty()) {
                return new RoomAvailabilityResponseDto(true, "Phòng trống");
            } else {
                List<String> conflicts = conflictingClasses.stream()
                    .map(cls -> String.format("Lớp '%s' bị trùng với khung thời gian yêu cầu", 
                                           cls.getClassName()))
                    .collect(Collectors.toList());
                
                return new RoomAvailabilityResponseDto(false, conflicts, new ArrayList<>());
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tình trạng phòng: {}", e.getMessage(), e);
            return new RoomAvailabilityResponseDto(false, "Lỗi khi kiểm tra tình trạng phòng: " + e.getMessage());
        }
    }
    
    @Override
    public List<RoomDto> getAvailableRooms(RoomSearchRequestDto searchRequest) {
        log.info("Searching for available rooms with criteria: {}", searchRequest);
        
        try {
            // Get all rooms that match basic criteria
            List<RoomDto> allRooms = getAllRooms(searchRequest);
            
            // If no time criteria specified, return all matching rooms
            if (searchRequest.getDate() == null || searchRequest.getStartTime() == null || searchRequest.getEndTime() == null) {
                return allRooms;
            }
            
            // Filter by availability
            return allRooms.stream()
                          .filter(room -> {
                              RoomAvailabilityRequestDto availabilityRequest = new RoomAvailabilityRequestDto();
                              availabilityRequest.setRoomId(room.getId());
                              availabilityRequest.setDate(searchRequest.getDate());
                              availabilityRequest.setStartTime(searchRequest.getStartTime());
                              availabilityRequest.setEndTime(searchRequest.getEndTime());
                              availabilityRequest.setExcludeClassId(searchRequest.getExcludeClassId());
                              
                              RoomAvailabilityResponseDto availability = checkRoomAvailability(availabilityRequest);
                              return availability.isAvailable();
                          })
                          .collect(Collectors.toList());
                          
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm phòng trống: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tìm kiếm phòng trống: " + e.getMessage());
        }
    }
    
    @Override
    public List<RoomScheduleDto> getRoomSchedule(Long roomId, String startDate, String endDate) {
        log.info("Getting room schedule for room {} from {} to {}", roomId, startDate, endDate);
        
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            
            List<ClassEntity> classes = classRepository.findByRoomIdAndDateRange(roomId, start, end);
            
            return classes.stream()
                         .map(this::convertClassToScheduleDto)
                         .collect(Collectors.toList());
                         
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch phòng: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy lịch phòng: " + e.getMessage());
        }
    }
    
    @Override
    public List<RoomScheduleDto> getMultipleRoomsSchedule(List<Long> roomIds, String startDate, String endDate) {
        log.info("Getting schedule for {} rooms from {} to {}", roomIds.size(), startDate, endDate);
        
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            
            List<ClassEntity> classes = classRepository.findByRoomIdsAndDateRange(roomIds, start, end);
            
            return classes.stream()
                         .map(this::convertClassToScheduleDto)
                         .collect(Collectors.toList());
                         
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch cho nhiều phòng: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy lịch cho nhiều phòng: " + e.getMessage());
        }
    }
    
    @Override
    public List<RoomDto> searchRooms(String keyword) {
        log.info("Searching rooms with keyword: {}", keyword);
        List<Room> rooms = roomRepository.findByRoomNameContainingOrRoomCodeContainingIgnoreCase(keyword);
        return rooms.stream()
                   .map(this::convertToDto)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomDto> getRoomsByCapacity(Integer minCapacity, Integer maxCapacity) {
        log.info("Getting rooms by capacity range: {} - {}", minCapacity, maxCapacity);
        List<Room> rooms = roomRepository.findRoomsByCapacityRange(minCapacity, maxCapacity);
        return rooms.stream()
                   .map(this::convertToDto)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<RoomDto> getRoomsByLocation(String location) {
        log.info("Getting rooms by location: {}", location);
        List<Room> rooms = roomRepository.findByLocationContainingIgnoreCase(location);
        return rooms.stream()
                   .map(this::convertToDto)
                   .collect(Collectors.toList());
    }
    
    @Override
    public RoomStatsDto getRoomStatistics() {
        log.info("Getting room statistics");
        
        List<Room> allRooms = roomRepository.findAll();
        List<Room> activeRooms = roomRepository.findByIsActiveTrueOrderByRoomCodeAsc();
        
        Long totalRooms = (long) allRooms.size();
        Long activeCount = (long) activeRooms.size();
        Long inactiveCount = totalRooms - activeCount;
        
        Integer totalCapacity = activeRooms.stream()
                                          .mapToInt(Room::getCapacity)
                                          .sum();
        
        Integer averageCapacity = activeCount > 0 ? 
                                totalCapacity / activeCount.intValue() : 0;
        
        // TODO: Calculate utilization rate based on actual bookings
        Double utilizationRate = 0.0; // Placeholder
        
        return new RoomStatsDto(totalRooms, activeCount, inactiveCount, 
                               totalCapacity, averageCapacity, utilizationRate,
                               0L, activeCount); // Placeholder values
    }
    
    // Helper methods
    
    private RoomDto convertToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomCode(room.getRoomCode());
        dto.setRoomName(room.getRoomName());
        dto.setCapacity(room.getCapacity());
        dto.setLocation(room.getLocation());
        dto.setFacilities(room.getFacilities());
        dto.setIsActive(room.getIsActive());
        dto.setCreatedAt(room.getCreatedAt());
        
        // Additional fields for frontend compatibility
        dto.setBuilding(extractBuilding(room));
        dto.setNumber(extractRoomNumber(room));
        dto.setType(extractRoomType(room));
        dto.setStatus(room.getIsActive() ? "active" : "inactive");
        dto.setName(room.getRoomName()); // Alias
        dto.setDescription(room.getFacilities()); // Alias
        
        return dto;
    }
    
    private Room convertToEntity(RoomDto dto) {
        Room room = new Room();
        room.setId(dto.getId());
        room.setRoomCode(dto.getRoomCode());
        room.setRoomName(dto.getRoomName());
        room.setCapacity(dto.getCapacity());
        room.setLocation(dto.getLocation());
        room.setFacilities(dto.getFacilities());
        room.setIsActive(dto.getIsActive());
        return room;
    }
    
    private RoomScheduleDto convertClassToScheduleDto(ClassEntity classEntity) {
        RoomScheduleDto dto = new RoomScheduleDto();
        dto.setId(classEntity.getId());
        dto.setRoomId(classEntity.getRoom() != null ? classEntity.getRoom().getId() : null);
        dto.setClassId(classEntity.getId());
        dto.setClassName(classEntity.getClassName());
        dto.setTitle(classEntity.getClassName());
        
        // Note: ClassEntity doesn't have time fields, so we'll use placeholder values
        // In a real implementation, you'd get this from a Schedule entity or ClassLesson entity
        dto.setDate(classEntity.getStartDate() != null ? 
                   classEntity.getStartDate().toString() : null);
        dto.setStartTime("08:00"); // Default/placeholder
        dto.setEndTime("10:00"); // Default/placeholder
        dto.setStatus("scheduled"); // Default status
        dto.setDescription(classEntity.getDescription());
        dto.setCreatedAt(classEntity.getCreatedAt());
        
        // TODO: Add teacher and student information if available
        if (classEntity.getTeacher() != null) {
            dto.setTeacherName(classEntity.getTeacher().getFullName());
        }
        dto.setStudentCount(classEntity.getCurrentStudents());
        
        return dto;
    }
    
    private String extractBuilding(Room room) {
        // Extract building from room code (e.g., "A101" -> "A")
        if (room.getRoomCode() != null && room.getRoomCode().length() > 0) {
            return String.valueOf(room.getRoomCode().charAt(0));
        }
        // Or from location
        if (room.getLocation() != null && room.getLocation().contains("Building")) {
            // Extract building name from location
            String[] parts = room.getLocation().split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i + 1].equals("Building")) {
                    return parts[i];
                }
            }
        }
        return "Unknown";
    }
    
    private String extractRoomNumber(Room room) {
        // Extract room number from room code (e.g., "A101" -> "101")
        if (room.getRoomCode() != null && room.getRoomCode().length() > 1) {
            return room.getRoomCode().substring(1);
        }
        return room.getRoomCode();
    }
    
    private String extractRoomType(Room room) {
        // Derive room type from facilities or default
        if (room.getFacilities() != null) {
            String facilities = room.getFacilities().toLowerCase();
            if (facilities.contains("lab") || facilities.contains("laboratory")) {
                return "lab";
            } else if (facilities.contains("computer")) {
                return "computer";
            } else if (facilities.contains("seminar")) {
                return "seminar";
            } else if (facilities.contains("meeting")) {
                return "meeting";
            }
        }
        return "lecture"; // Default type
    }
}