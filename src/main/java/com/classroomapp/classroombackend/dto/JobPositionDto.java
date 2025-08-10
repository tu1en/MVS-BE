package com.classroomapp.classroombackend.dto;

import lombok.Data;

@Data
public class JobPositionDto {
    private Long id;
    private String title;
    private String description;
    private String salaryRange;
    private Integer quantity;
    private String contractType;
    private Long recruitmentPlanId;
    private String recruitmentPlanStatus; // Thêm field này để frontend biết trạng thái
} 