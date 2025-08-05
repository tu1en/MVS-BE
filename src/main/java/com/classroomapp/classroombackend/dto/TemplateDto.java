package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDto {
    private String name;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
}