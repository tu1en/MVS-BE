package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.CreateTeacherEvaluationDto;
import com.classroomapp.classroombackend.dto.TeacherEvaluationDto;
import com.classroomapp.classroombackend.dto.TeacherEvaluationStatisticsDto;

import java.util.List;

public interface TeacherEvaluationService {
    
    /**
     * Create a new teacher evaluation
     * @param dto the evaluation data
     * @param evaluatorId the ID of the Teaching Assistant making the evaluation
     * @return the created evaluation
     */
    TeacherEvaluationDto createEvaluation(CreateTeacherEvaluationDto dto, Long evaluatorId);
    
    /**
     * Get all evaluations for a specific teacher
     * @param teacherId the teacher's ID
     * @return list of evaluations for the teacher
     */
    List<TeacherEvaluationDto> getEvaluationsByTeacher(Long teacherId);
    
    /**
     * Get all evaluations made by a specific evaluator
     * @param evaluatorId the evaluator's ID
     * @return list of evaluations made by the evaluator
     */
    List<TeacherEvaluationDto> getEvaluationsByEvaluator(Long evaluatorId);
    
    /**
     * Get the average overall score for a teacher
     * @param teacherId the teacher's ID
     * @return the average score, or null if no evaluations exist
     */
    Double getAverageScoreByTeacher(Long teacherId);
    
    /**
     * Update an existing evaluation
     * @param id the evaluation ID
     * @param dto the updated evaluation data
     * @return the updated evaluation
     */
    TeacherEvaluationDto updateEvaluation(Long id, CreateTeacherEvaluationDto dto);
    
    /**
     * Delete an evaluation
     * @param id the evaluation ID
     */
    void deleteEvaluation(Long id);
    
    /**
     * Get evaluation by ID
     * @param id the evaluation ID
     * @return the evaluation
     */
    TeacherEvaluationDto getEvaluationById(Long id);
    
    /**
     * Get all evaluations with pagination support
     * @return list of all evaluations
     */
    List<TeacherEvaluationDto> getAllEvaluations();
    
    /**
     * Get evaluation statistics for a teacher
     * @param teacherId the teacher's ID
     * @return evaluation statistics including count and average scores
     */
    TeacherEvaluationStatisticsDto getEvaluationStatistics(Long teacherId);
}