package com.doproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho request táº¡o/cáº­p nháº­t workflow
 */
@Data
public class WorkflowRequest {
    
    @NotBlank(message = "TÃªn workflow khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 255, message = "TÃªn workflow khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 255 kÃ½ tá»±")
    private String name;
    
    @Size(max = 1000, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String description;
    
    @NotBlank(message = "Dá»¯ liá»‡u JSON khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String jsonData;
    
    private Boolean isActive;
    
    private String createdBy;
}
