package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.GradingRubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GradingRubricRepository extends JpaRepository<GradingRubric, Long> {
    
    /**
     * Find all rubrics for an assignment
     */
    List<GradingRubric> findByAssignmentIdOrderByDisplayOrder(Long assignmentId);
    
    /**
     * Find rubrics created by a specific user
     */
    List<GradingRubric> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    /**
     * Get total maximum points for an assignment
     */
    @Query("SELECT SUM(gr.maxPoints) FROM GradingRubric gr WHERE gr.assignmentId = :assignmentId")
    BigDecimal getTotalMaxPointsByAssignment(@Param("assignmentId") Long assignmentId);
    
    /**
     * Find rubrics with highest weights
     */
    @Query("SELECT gr FROM GradingRubric gr WHERE gr.assignmentId = :assignmentId " +
           "ORDER BY gr.weightPercentage DESC")
    List<GradingRubric> findByAssignmentIdOrderByWeightDesc(@Param("assignmentId") Long assignmentId);
    
    /**
     * Check if assignment has rubrics
     */
    boolean existsByAssignmentId(Long assignmentId);
    
    /**
     * Count rubrics for assignment
     */
    long countByAssignmentId(Long assignmentId);
}
