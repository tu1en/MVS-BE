package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressAnalyticsDto {
    private Long classroomId;
    private String classroomName;
    private Integer totalStudents;
    private BigDecimal averageProgress;
    private BigDecimal highestProgress;
    private BigDecimal lowestProgress;
    private Integer studentsAbove80Percent;
    private Integer studentsBelow50Percent;
    private List<StudentProgressDto> topPerformers;
    private List<StudentProgressDto> strugglingStudents;
    private Integer totalAssignments;
    private Integer completedAssignments;
    private Long totalTimeSpent;
    private BigDecimal averageTimePerStudent;
}
