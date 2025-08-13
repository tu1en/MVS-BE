package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.TeacherEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeacherEvaluationRepository extends JpaRepository<TeacherEvaluation, Long> {
    
    /**
     * Find all evaluations for a specific teacher, ordered by evaluation date (newest first)
     */
    List<TeacherEvaluation> findByTeacherIdOrderByEvaluationDateDesc(Long teacherId);
    
    /**
     * Find all evaluations made by a specific evaluator, ordered by evaluation date (newest first)
     */
    List<TeacherEvaluation> findByEvaluatorIdOrderByEvaluationDateDesc(Long evaluatorId);
    
    /**
     * Find evaluations for a specific class session
     */
    List<TeacherEvaluation> findByClassSessionId(Long classSessionId);
    
    /**
     * Get average overall score for a teacher
     */
    @Query("SELECT AVG(te.overallScore) FROM TeacherEvaluation te WHERE te.teacher.id = :teacherId")
    Double getAverageScoreByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * Find evaluations for a teacher within a date range
     */
    @Query("SELECT te FROM TeacherEvaluation te WHERE te.teacher.id = :teacherId AND te.evaluationDate >= :fromDate")
    List<TeacherEvaluation> findByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("fromDate") LocalDateTime fromDate);
    
    /**
     * Get average scores by criteria for a teacher
     */
    @Query("SELECT AVG(te.teachingQualityScore), AVG(te.studentInteractionScore), AVG(te.punctualityScore) " +
           "FROM TeacherEvaluation te WHERE te.teacher.id = :teacherId")
    Object[] getAverageScoresByCriteriaForTeacher(@Param("teacherId") Long teacherId);
    
    /**
     * Count evaluations for a teacher
     */
    Long countByTeacherId(Long teacherId);
    
    /**
     * Check if evaluation already exists for a specific teacher in a specific class session by a specific evaluator
     */
    boolean existsByTeacherIdAndClassSessionIdAndEvaluatorId(Long teacherId, Long classSessionId, Long evaluatorId);
}