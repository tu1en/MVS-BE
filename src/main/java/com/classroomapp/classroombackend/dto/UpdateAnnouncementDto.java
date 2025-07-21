package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.validation.EnumValidator;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnnouncementDto {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String content;

    @EnumValidator(enumClass = Announcement.TargetAudience.class, message = "Invalid target audience", allowNull = true)
    private String targetAudience; // e.g. ALL, STUDENTS, TEACHERS

    @EnumValidator(enumClass = Announcement.Priority.class, message = "Invalid priority", allowNull = true)
    private String priority; // e.g. LOW, NORMAL, HIGH

    private LocalDateTime scheduledDate;

    private LocalDateTime expiryDate;

    private Boolean isPinned;

    @EnumValidator(enumClass = Announcement.AnnouncementStatus.class, message = "Invalid status", allowNull = true)
    private String status; // e.g. ACTIVE, ARCHIVED
}
