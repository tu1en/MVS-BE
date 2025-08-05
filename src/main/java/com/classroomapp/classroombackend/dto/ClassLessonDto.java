package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassLessonDto {
    private Long id;
    private Long classId;
    private Long lessonTemplateId;
    private String lessonTopic;
    private String lessonType;
    private LocalDate actualDate;
    private String actualStartTime;
    private String actualEndTime;
    private String status;
    private String notes;
    private Integer attendanceCount;
    private List<MaterialDto> materials;
}