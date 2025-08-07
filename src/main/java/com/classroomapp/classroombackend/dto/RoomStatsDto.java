package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatsDto {
    
    private Long totalRooms;
    
    private Long activeRooms;
    
    private Long inactiveRooms;
    
    private Integer totalCapacity;
    
    private Integer averageCapacity;
    
    private Double utilizationRate; // Percentage of rooms currently booked
    
    private Long currentlyOccupied;
    
    private Long availableNow;
}