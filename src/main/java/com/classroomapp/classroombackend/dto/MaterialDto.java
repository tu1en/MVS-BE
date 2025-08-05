package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {
    private Long id;
    private Long lessonTemplateId;
    private Long classLessonId;
    private String materialType;
    private String title;
    private String description;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Long uploadedBy;
    private Boolean isRequired;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}