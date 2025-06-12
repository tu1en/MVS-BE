package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.GradingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GradingDetailRepository extends JpaRepository<GradingDetail, Long> {
    
    /**
     * Find all grading details for a submission
     */
    List<GradingDetail> findBySubmissionIdOrderByRubricId(Long submissionId);
    
    /**
     * Find grading details by grader
     */
    List<GradingDetail> findByGradedByOrderByGradedAtDesc(Long gradedBy);
    
    /**
     * Find grading details for a specific rubric
     */
    List<GradingDetail> findByRubricIdOrderByGradedAtDesc(Long rubricId);
    
    /**
     * Get total points for a submission
     */
    @Query("SELECT SUM(gd.pointsAwarded) FROM GradingDetail gd WHERE gd.submissionId = :submissionId")
    BigDecimal getTotalPointsBySubmission(@Param("submissionId") Long submissionId);
    
    /**
     * Check if submission is fully graded
     */
    @Query("SELECT COUNT(gd) = (SELECT COUNT(gr) FROM GradingRubric gr WHERE gr.assignmentId = " +
           "(SELECT s.assignment.id FROM Submission s WHERE s.id = :submissionId)) " +
           "FROM GradingDetail gd WHERE gd.submissionId = :submissionId")
    boolean isSubmissionFullyGraded(@Param("submissionId") Long submissionId);
    
    /**
     * Get average points for a rubric across all submissions
     */
    @Query("SELECT AVG(gd.pointsAwarded) FROM GradingDetail gd WHERE gd.rubricId = :rubricId")
    BigDecimal getAveragePointsByRubric(@Param("rubricId") Long rubricId);
    
    /**
     * Find submissions graded by a teacher for a specific assignment
     */
    @Query("SELECT gd FROM GradingDetail gd " +
           "JOIN Submission s ON gd.submissionId = s.id " +
           "WHERE s.assignment.id = :assignmentId AND gd.gradedBy = :gradedBy")
    List<GradingDetail> findByAssignmentAndGrader(@Param("assignmentId") Long assignmentId,
                                                 @Param("gradedBy") Long gradedBy);
}
