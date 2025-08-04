package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * DTO cho thÃ´ng tin chi tiáº¿t cá»§a lá»‹ch lÃ m viá»‡c theo ca
 */
@Data
public class ShiftScheduleDto {
    private Long id;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // DRAFT, PUBLISHED, ARCHIVED
    private String type;   // WEEKLY, MONTHLY
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> shiftTemplateIds;
    private List<Long> assignedUserIds;
    private List<ShiftAssignmentDto> shiftAssignments;
    private Boolean isActive;
    private Integer totalShifts;
    private Integer assignedShifts;
}
