package com.classroomapp.classroombackend.dto.classroommanagement;

import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for creating new Slot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSlotDto {

    @NotBlank(message = "Tên slot không được để trống")
    @Size(max = 255, message = "Tên slot không được vượt quá 255 ký tự")
    private String slotName;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @NotNull(message = "Session ID không được để trống")
    private Long sessionId;

    private Slot.SlotStatus status = Slot.SlotStatus.PENDING;

    /**
     * Validation method to check if start time is before end time
     */
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return false;
        }
        return startTime.isBefore(endTime);
    }

    /**
     * Get duration in minutes
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
            return "Chưa xác định";
        }
        return String.format("%s - %s", startTime.toString(), endTime.toString());
    }
}
