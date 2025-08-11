package com.classroomapp.classroombackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // New fields for public catalog and pricing
    private Boolean isPublic;
    private BigDecimal enrollmentFee;
    private Integer maxStudentsPerTemplate;

    private List<LessonTemplateDto> lessonTemplates;
}