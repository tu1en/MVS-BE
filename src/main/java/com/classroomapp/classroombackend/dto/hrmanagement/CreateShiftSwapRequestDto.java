package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho táº¡o má»›i yÃªu cáº§u Ä‘á»•i ca
 */
@Data
public class CreateShiftSwapRequestDto {
    @NotNull(message = "Target employee ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long targetEmployeeId;
    
    @NotBlank(message = "LÃ½ do khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 500, message = "LÃ½ do khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String reason;
    
    @NotNull(message = "Priority khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String priority;
    
    private Boolean isEmergency = false;
    
    private LocalDateTime requestTime = LocalDateTime.now();
}
