package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.CreateTeacherEvaluationDto;
import com.classroomapp.classroombackend.dto.TeacherEvaluationDto;
import com.classroomapp.classroombackend.dto.TeacherEvaluationStatisticsDto;
import com.classroomapp.classroombackend.model.TeacherEvaluation;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.TeacherEvaluationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.TeacherEvaluationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherEvaluationServiceImpl implements TeacherEvaluationService {
    
    private static final Logger log = LoggerFactory.getLogger(TeacherEvaluationServiceImpl.class);
    
    @Autowired
    private TeacherEvaluationRepository evaluationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public TeacherEvaluationDto createEvaluation(CreateTeacherEvaluationDto dto, Long evaluatorId) {
        log.info("Creating teacher evaluation for teacher {} by evaluator {}", dto.getTeacherId(), evaluatorId);
        
        // Check if evaluation already exists for this teacher-session-evaluator combination
        if (evaluationRepository.existsByTeacherIdAndClassSessionIdAndEvaluatorId(
                dto.getTeacherId(), dto.getClassSessionId(), evaluatorId)) {
            throw new IllegalStateException("Evaluation already exists for this teacher in this session by this evaluator");
        }
        
        // Fetch teacher and evaluator
        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + dto.getTeacherId()));
        User evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluator not found with ID: " + evaluatorId));
        
        // Create evaluation entity
        TeacherEvaluation evaluation = new TeacherEvaluation();
        evaluation.setTeacher(teacher);
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluationDate(LocalDateTime.now());
        evaluation.setTeachingQualityScore(dto.getTeachingQualityScore());
        evaluation.setStudentInteractionScore(dto.getStudentInteractionScore());
        evaluation.setPunctualityScore(dto.getPunctualityScore());
        evaluation.setComments(dto.getComments());
        evaluation.setClassSessionId(dto.getClassSessionId());
        
        // Calculate overall score (average of three criteria)
        int overallScore = Math.round((dto.getTeachingQualityScore() + 
                                     dto.getStudentInteractionScore() + 
                                     dto.getPunctualityScore()) / 3.0f);
        evaluation.setOverallScore(overallScore);
        
        // Save evaluation
        TeacherEvaluation savedEvaluation = evaluationRepository.save(evaluation);
        
        log.info("Successfully created teacher evaluation with ID: {}", savedEvaluation.getId());
        return convertToDto(savedEvaluation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TeacherEvaluationDto> getEvaluationsByTeacher(Long teacherId) {
        log.info("Fetching evaluations for teacher with ID: {}", teacherId);
        
        List<TeacherEvaluation> evaluations = evaluationRepository.findByTeacherIdOrderByEvaluationDateDesc(teacherId);
        return evaluations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TeacherEvaluationDto> getEvaluationsByEvaluator(Long evaluatorId) {
        log.info("Fetching evaluations made by evaluator with ID: {}", evaluatorId);
        
        List<TeacherEvaluation> evaluations = evaluationRepository.findByEvaluatorIdOrderByEvaluationDateDesc(evaluatorId);
        return evaluations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getAverageScoreByTeacher(Long teacherId) {
        log.info("Calculating average score for teacher with ID: {}", teacherId);
        
        Double average = evaluationRepository.getAverageScoreByTeacherId(teacherId);
        return average != null ? Math.round(average * 100.0) / 100.0 : null; // Round to 2 decimal places
    }
    
    @Override
    public TeacherEvaluationDto updateEvaluation(Long id, CreateTeacherEvaluationDto dto) {
        log.info("Updating teacher evaluation with ID: {}", id);
        
        TeacherEvaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with ID: " + id));
        
        // Update evaluation fields
        evaluation.setTeachingQualityScore(dto.getTeachingQualityScore());
        evaluation.setStudentInteractionScore(dto.getStudentInteractionScore());
        evaluation.setPunctualityScore(dto.getPunctualityScore());
        evaluation.setComments(dto.getComments());
        
        // Recalculate overall score
        int overallScore = Math.round((dto.getTeachingQualityScore() + 
                                     dto.getStudentInteractionScore() + 
                                     dto.getPunctualityScore()) / 3.0f);
        evaluation.setOverallScore(overallScore);
        
        TeacherEvaluation updatedEvaluation = evaluationRepository.save(evaluation);
        
        log.info("Successfully updated teacher evaluation with ID: {}", id);
        return convertToDto(updatedEvaluation);
    }
    
    @Override
    public void deleteEvaluation(Long id) {
        log.info("Deleting teacher evaluation with ID: {}", id);
        
        if (!evaluationRepository.existsById(id)) {
            throw new IllegalArgumentException("Evaluation not found with ID: " + id);
        }
        
        evaluationRepository.deleteById(id);
        log.info("Successfully deleted teacher evaluation with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TeacherEvaluationDto getEvaluationById(Long id) {
        log.info("Fetching teacher evaluation with ID: {}", id);
        
        TeacherEvaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with ID: " + id));
        
        return convertToDto(evaluation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TeacherEvaluationDto> getAllEvaluations() {
        log.info("Fetching all teacher evaluations");
        
        List<TeacherEvaluation> evaluations = evaluationRepository.findAll();
        return evaluations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public TeacherEvaluationStatisticsDto getEvaluationStatistics(Long teacherId) {
        log.info("Fetching evaluation statistics for teacher with ID: {}", teacherId);
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + teacherId));
        
        Long evaluationCount = evaluationRepository.countByTeacherId(teacherId);
        Double averageOverallScore = evaluationRepository.getAverageScoreByTeacherId(teacherId);
        Object[] averageScores = evaluationRepository.getAverageScoresByCriteriaForTeacher(teacherId);
        
        Double avgTeachingQuality = null;
        Double avgStudentInteraction = null;
        Double avgPunctuality = null;
        
        if (averageScores != null && averageScores.length >= 3) {
            avgTeachingQuality = (Double) averageScores[0];
            avgStudentInteraction = (Double) averageScores[1];
            avgPunctuality = (Double) averageScores[2];
        }
        
        return new TeacherEvaluationStatisticsDto(
                teacherId,
                teacher.getFullName(),
                evaluationCount,
                roundToTwoDecimalPlaces(averageOverallScore),
                roundToTwoDecimalPlaces(avgTeachingQuality),
                roundToTwoDecimalPlaces(avgStudentInteraction),
                roundToTwoDecimalPlaces(avgPunctuality)
        );
    }
    
    private TeacherEvaluationDto convertToDto(TeacherEvaluation evaluation) {
        return new TeacherEvaluationDto(
                evaluation.getId(),
                evaluation.getTeacher().getId(),
                evaluation.getTeacher().getFullName(),
                evaluation.getEvaluator().getId(),
                evaluation.getEvaluator().getFullName(),
                evaluation.getEvaluationDate(),
                evaluation.getTeachingQualityScore(),
                evaluation.getStudentInteractionScore(),
                evaluation.getPunctualityScore(),
                evaluation.getOverallScore(),
                evaluation.getComments(),
                evaluation.getClassSessionId()
        );
    }
    
    private Double roundToTwoDecimalPlaces(Double value) {
        return value != null ? Math.round(value * 100.0) / 100.0 : null;
    }
}