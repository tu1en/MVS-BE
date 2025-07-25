package com.classroomapp.classroombackend.dto;

import com.classroomapp.classroombackend.entity.enumeration.SyllabusStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String syllabusName;
    private String syllabusCode;
    private String subject;
    private String gradeLevel;
    private String startDate;
    private String endDate;
    private Integer maxStudents;
    private BigDecimal price;
    private String formattedPrice;
    private String creatorName;
    private String status;
    private LocalDateTime createdAt;
}