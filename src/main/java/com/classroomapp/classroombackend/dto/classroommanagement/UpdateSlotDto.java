package com.classroomapp.classroombackend.dto.classroommanagement;

import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for updating existing Slot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSlotDto {

    @Size(max = 255, message = "Tên slot không được vượt quá 255 ký tự")
    private String slotName;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private Slot.SlotStatus status;

    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return slotName != null || description != null || 
               startTime != null || endTime != null || status != null;
    }

    /**
     * Validation method to check if start time is before end time
     */
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // If either is null, we don't validate (partial update)
        }
        return startTime.isBefore(endTime);
    }

    /**
     * Get duration in minutes (if both times are provided)
     */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Check if this slot conflicts with another time range
     */
    public boolean conflictsWith(LocalTime otherStart, LocalTime otherEnd) {
        if (startTime == null || endTime == null || otherStart == null || otherEnd == null) {
            return false;
        }
        
        // Two time ranges conflict if one starts before the other ends
        return startTime.isBefore(otherEnd) && endTime.isAfter(otherStart);
    }

    /**
     * Format time range as string
     */
    public String getTimeRangeString() {
        if (startTime == null || endTime == null) {
            return "Chưa cập nhật";
        }
        return String.format("%s - %s", startTime.toString(), endTime.toString());
    }

    /**
     * Check if time fields are being updated
     */
    public boolean isTimeUpdate() {
        return startTime != null || endTime != null;
    }

    /**
     * Check if status is being updated
     */
    public boolean isStatusUpdate() {
        return status != null;
    }
}
