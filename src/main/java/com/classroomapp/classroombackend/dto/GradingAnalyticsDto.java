package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradingAnalyticsDto {
    private int totalSubmissions;
    private int gradedSubmissions;
    private int pendingGrading;
    private double averageGrade;
    private double highestGrade;
    private double lowestGrade;
    private Map<String, Integer> gradeDistribution;
    private LocalDateTime lastGradedDate;
}
