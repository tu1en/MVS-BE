package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnouncementDto {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private Long classroomId;
    
    private String targetAudience = "ALL";
    
    private String priority = "NORMAL";
    
    private LocalDateTime scheduledDate;
    
    private LocalDateTime expiryDate;
    
    private Boolean isPinned = false;
}
