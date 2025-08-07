package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResponseDto {
    
    private boolean isAvailable;
    
    private List<String> conflicts;
    
    private List<String> warnings;
    
    private List<RoomDto> suggestions; // Alternative rooms if current one is not available
    
    private String message;
    
    // Constructor for simple availability check
    public RoomAvailabilityResponseDto(boolean isAvailable, String message) {
        this.isAvailable = isAvailable;
        this.message = message;
    }
    
    // Constructor with conflicts
    public RoomAvailabilityResponseDto(boolean isAvailable, List<String> conflicts, List<String> warnings) {
        this.isAvailable = isAvailable;
        this.conflicts = conflicts;
        this.warnings = warnings;
    }
}