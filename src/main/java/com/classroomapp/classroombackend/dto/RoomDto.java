package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    
    private Long id;
    
    @NotBlank(message = "Room code is required")
    @Size(min = 2, max = 20, message = "Room code must be between 2 and 20 characters")
    private String roomCode;
    
    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 100, message = "Room name must be between 3 and 100 characters")
    private String roomName;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;
    
    @Size(max = 1000, message = "Facilities description cannot exceed 1000 characters")
    private String facilities;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    // Additional fields for frontend compatibility
    private String building;    // Extracted from location or roomCode
    private String number;      // Extracted from roomCode
    private String type;        // Derived from facilities or default value
    private String status;      // active/inactive based on isActive
    private String name;        // Alias for roomName
    private String description; // Alias for facilities
}