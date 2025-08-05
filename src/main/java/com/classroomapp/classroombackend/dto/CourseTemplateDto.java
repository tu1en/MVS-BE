package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTemplateDto {
    private Long id;
    private String name;
    private String description;
    private String subject;
    private Integer totalWeeks;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private List<LessonTemplateDto> lessonTemplates;
}