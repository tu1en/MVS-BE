package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.Data;

import java.time.LocalTime;

/**
 * DTO cho máº«u ca lÃ m viá»‡c
 */
@Data
public class ShiftTemplateDto {
    private Long id;
    private String templateName;
    private String templateCode;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double durationHours;
    private Boolean isActive;
    private String colorCode;
    private Boolean isOvertimeEligible;
    private Integer maxEmployees;
    private Integer minEmployees;
    private Integer sortOrder;
}
