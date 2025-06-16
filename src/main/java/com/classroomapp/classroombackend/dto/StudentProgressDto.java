package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long classroomId;
    private String classroomName;
    private Long assignmentId;
    private String assignmentTitle;
    private String progressType;
    private BigDecimal progressPercentage;
    private BigDecimal pointsEarned;
    private BigDecimal maxPoints;
    private LocalDateTime completionDate;
    private LocalDateTime lastAccessed;
    private Integer timeSpentMinutes;
    private String notes;
}
