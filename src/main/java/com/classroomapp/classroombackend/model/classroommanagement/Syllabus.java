package com.classroomapp.classroombackend.model.classroommanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "syllabuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String title;
    
    @Column(length = 5000)
    private String content;
    
    @Column(name = "learning_objectives", length = 2000)
    private String learningObjectives;
    
    @Column(name = "required_materials", length = 1000)
    private String requiredMaterials;
    
    @Column(name = "grading_criteria", length = 1000)
    private String gradingCriteria;
    
    // One-to-One relationship with Classroom
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", unique = true)
    private Classroom classroom;
}
