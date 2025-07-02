package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusDto {
    
    private Long id;
    
    @NotBlank(message = "Syllabus title is required")
    private String title;
    
    private String content;
    
    private String learningObjectives;
    
    private String requiredMaterials;
    
    private String gradingCriteria;
    
    private Long classroomId;
}
