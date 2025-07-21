package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.validation.EnumValidator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnouncementDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private Long classroomId; // optional - nếu null thì là announcement cho toàn hệ thống
    
    @EnumValidator(enumClass = Announcement.TargetAudience.class, message = "Invalid target audience")
    private String targetAudience = "ALL"; // ALL, STUDENTS, TEACHERS
    
    @EnumValidator(enumClass = Announcement.Priority.class, message = "Invalid priority")
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH
    
    private LocalDateTime scheduledDate; // optional - nếu muốn schedule announcement
    
    private LocalDateTime expiryDate; // optional - ngày hết hạn
    
    private Boolean isPinned = false; // có pin lên đầu không
}
