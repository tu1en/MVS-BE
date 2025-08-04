package com.classroomapp.classroombackend.dto.classroommanagement;

import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho Slot
 * Dùng trong Firebase sync để đồng bộ dữ liệu slot
 */
@Data
public class SlotDto {
    private Long id;
    private String slotName;
    private Long sessionId;
    private String startTime;
    private String endTime;
    private String description;
    private Slot.SlotStatus status;
}

/**
 * Các enum cho status của Slot
 */
enum SlotStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}