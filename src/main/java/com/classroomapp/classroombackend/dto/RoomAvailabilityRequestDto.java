package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityRequestDto {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Start time must be in HH:MM format")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "End time must be in HH:MM format")
    private String endTime;
    
    private Long excludeClassId; // To exclude current class when checking for updates
}