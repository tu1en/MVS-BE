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

    @NotBlank(message = "TÃªn slot khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 255, message = "TÃªn slot khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 255 kÃ½ tá»±")
    private String slotName;

    @Size(max = 1000, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String description;

    @NotNull(message = "Thá»i gian báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull(message = "Thá»i gian káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @NotNull(message = "Session ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
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
            return "ChÆ°a xÃ¡c Ä‘á»‹nh";
        }
        return String.format("%s - %s", startTime.toString(), endTime.toString());
    }
}
