package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassDto {
    private Long id;
    private Long courseTemplateId;
    private String courseTemplateName;
    private String className;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Long roomId;
    private String roomCode;
    private String roomName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String schedule;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private List<ClassLessonDto> classLessons;
}






