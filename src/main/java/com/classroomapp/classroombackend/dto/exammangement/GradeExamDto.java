package com.classroomapp.classroombackend.dto.exammangement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeExamDto {

    @NotNull
    @Min(0)
    private Integer score;

    private String feedback;
} 