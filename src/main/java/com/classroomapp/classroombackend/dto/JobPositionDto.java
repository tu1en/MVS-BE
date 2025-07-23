package com.classroomapp.classroombackend.dto;

import lombok.Data;

@Data
public class JobPositionDto {
    private Long id;
    private String title;
    private String description;
    private String salaryRange;
} 