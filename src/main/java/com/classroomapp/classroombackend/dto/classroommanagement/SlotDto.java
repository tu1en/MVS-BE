package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalTime;

import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Slot entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotDto {

    private Long id;

    private String slotName;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String description;

    private Slot.SlotStatus status;

    private Long sessionId;

    /**
     * Get formatted time range for display
     */
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return startTime.toString() + " - " + endTime.toString();
        }
        return null;
    }

    /**
     * Calculate slot duration in minutes
     */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Check if this slot overlaps with another slot
     */
    public boolean overlapsWith(SlotDto other) {
        if (other == null || startTime == null || endTime == null || 
            other.startTime == null || other.endTime == null) {
            return false;
        }
        
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }
}
