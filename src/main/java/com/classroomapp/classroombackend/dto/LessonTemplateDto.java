package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonTemplateDto {
    private Long id;
    private Long courseTemplateId;
    private Integer weekNumber;
    private String topicName;
    private String lessonType;
    private String objectives;
    private String requirements;
    private String preparations;
    private Integer durationMinutes;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private List<MaterialDto> materials;
}