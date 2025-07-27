package com.classroomapp.classroombackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseTeacherDTO {
    private Long id;
    private CourseDTO course;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherRole;
    private String status;
    private String assignedAt;
    private String acceptedAt;
    private String notes;
}