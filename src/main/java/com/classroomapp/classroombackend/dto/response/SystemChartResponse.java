package com.classroomapp.classroombackend.dto.response;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.entity.SystemChart.ChartType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SystemChart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemChartResponse {
    
    private Long id;
    private String title;
    private String description;
    private ChartType chartType;
    private String chartData;
    private String chartConfig;
    private Boolean isActive;
    private Boolean isPublic;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}