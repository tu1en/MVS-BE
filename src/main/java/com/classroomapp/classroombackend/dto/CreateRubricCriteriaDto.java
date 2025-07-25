package com.classroomapp.classroombackend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRubricCriteriaDto {
    private String name;
    private String description;
    private Double maxPoints;
    private List<String> performanceLevels;
}
