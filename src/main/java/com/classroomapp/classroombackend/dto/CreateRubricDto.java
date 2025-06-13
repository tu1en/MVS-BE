package com.classroomapp.classroombackend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRubricDto {
    private String title;
    private String description;
    private List<CreateRubricCriteriaDto> criteria;
}
