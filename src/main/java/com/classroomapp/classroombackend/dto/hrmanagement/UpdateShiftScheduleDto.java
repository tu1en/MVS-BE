package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho cập nhật lịch làm việc theo ca
 */
@Data
public class UpdateShiftScheduleDto {
    @NotNull(message = "ID lịch làm việc không được để trống")
    private Long id;

    @NotBlank(message = "Tên lịch làm việc không được để trống")
    @Size(max = 200, message = "Tên lịch làm việc không được vượt quá 200 ký tự")
    private String scheduleName;

    private LocalDate startDate;
    private LocalDate endDate;
    
    private String status; // DRAFT, PUBLISHED, ARCHIVED
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    private List<Long> shiftTemplateIds;
    private List<Long> assignedUserIds;
    
    private Boolean isActive;
}
