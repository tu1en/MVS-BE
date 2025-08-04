package com.classroomapp.classroombackend.dto.request;

import com.classroomapp.classroombackend.entity.SystemChart.ChartType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for SystemChart operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemChartRequest {
    
    @NotBlank(message = "Tiêu đề biểu đồ không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotNull(message = "Loại biểu đồ không được để trống")
    private ChartType chartType;
    
    private String chartData;
    
    private String chartConfig;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Boolean isPublic = false;
    
    private String createdBy;
}