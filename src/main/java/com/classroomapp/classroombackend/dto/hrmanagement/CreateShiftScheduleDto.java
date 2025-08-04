package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho táº¡o má»›i lá»‹ch lÃ m viá»‡c theo ca
 */
@Data
public class CreateShiftScheduleDto {
    @NotBlank(message = "TÃªn lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 200, message = "TÃªn lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 200 kÃ½ tá»±")
    private String scheduleName;
    
    @NotNull(message = "NgÃ y báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate startDate;
    
    @NotNull(message = "NgÃ y káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate endDate;
    
    private String type; // WEEKLY, MONTHLY
    
    @Size(max = 1000, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String description;
    
    private List<Long> shiftTemplateIds;
    private List<Long> assignedUserIds;
    
    // Backward compatibility fields
    @NotNull(message = "Employee ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long assignedUserId;
    
    @NotNull(message = "Template ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long templateId;
    
    @NotNull(message = "NgÃ y lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate workingDate;
    
    @Size(max = 500, message = "Ghi chÃº khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String notes;
    
    private Boolean isConfirmed = false;
    
    private String shiftType;
}
