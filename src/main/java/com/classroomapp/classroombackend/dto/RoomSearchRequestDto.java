package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchRequestDto {
    
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;
    
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Start time must be in HH:MM format")
    private String startTime;
    
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "End time must be in HH:MM format")
    private String endTime;
    
    @Min(value = 1, message = "Minimum capacity must be at least 1")
    private Integer minCapacity;
    
    private String building;
    
    private String type;
    
    private String location;
    
    private Long excludeClassId; // To exclude current class when checking for updates
    
    private String status; // active, inactive, all
}