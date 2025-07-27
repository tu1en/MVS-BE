package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

/**
 * DTO cho cáº­p nháº­t máº«u ca lÃ m viá»‡c
 */
@Data
public class UpdateShiftTemplateDto {
    @NotBlank(message = "TÃªn template khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 100, message = "TÃªn template khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 100 kÃ½ tá»±")
    private String templateName;
    
    @Size(max = 500, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String description;
    
    @NotNull(message = "Thá»i gian báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime startTime;
    
    @NotNull(message = "Thá»i gian káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime endTime;
    
    @Size(max = 7, message = "MÃ£ mÃ u khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 7 kÃ½ tá»±")
    private String colorCode;
    
    private Boolean isOvertimeEligible;
    
    private Integer maxEmployees;
    
    private Integer minEmployees;
    
    private Boolean isActive;
}
