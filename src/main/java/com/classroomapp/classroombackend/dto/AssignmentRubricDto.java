package com.classroomapp.classroombackend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRubricDto {
    private Long id;
    private Long assignmentId;
    private String title;
    private String description;
    private List<RubricCriteriaDto> criteria;
    private Double totalPoints;
}
