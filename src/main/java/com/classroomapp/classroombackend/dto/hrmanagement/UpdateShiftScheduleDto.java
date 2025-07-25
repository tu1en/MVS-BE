package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho cáº­p nháº­t lá»‹ch lÃ m viá»‡c theo ca
 */
@Data
public class UpdateShiftScheduleDto {
    @NotNull(message = "ID lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long id;

    @NotBlank(message = "TÃªn lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 200, message = "TÃªn lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 200 kÃ½ tá»±")
    private String scheduleName;

    private LocalDate startDate;
    private LocalDate endDate;
    
    private String status; // DRAFT, PUBLISHED, ARCHIVED
    
    @Size(max = 1000, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String description;
    
    private List<Long> shiftTemplateIds;
    private List<Long> assignedUserIds;
    
    private Boolean isActive;
}
