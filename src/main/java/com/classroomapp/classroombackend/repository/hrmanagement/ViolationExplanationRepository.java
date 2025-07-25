package com.classroomapp.classroombackend.repository.hrmanagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;

/**
 * Repository interface for ViolationExplanation entity
 */
@Repository
public interface ViolationExplanationRepository extends JpaRepository<ViolationExplanation, Long> {
    
    /**
     * Find explanations by violation ID
     * @param violationId the violation ID
     * @return list of explanations
     */
    List<ViolationExplanation> findByViolationIdOrderBySubmittedAtDesc(Long violationId);
    
    /**
     * Find explanations by submitted user
     * @param submittedBy the user ID who submitted
     * @return list of explanations
     */
    List<ViolationExplanation> findBySubmittedByIdOrderBySubmittedAtDesc(Long submittedBy);
    
    /**
     * Find explanations by submitted user with pagination
     * @param submittedBy the user ID who submitted
     * @param pageable pagination parameters
     * @return page of explanations
     */
    Page<ViolationExplanation> findBySubmittedByIdOrderBySubmittedAtDesc(Long submittedBy, Pageable pageable);
    
    /**
     * Find explanations by status
     * @param status the explanation status
     * @return list of explanations
     */
    List<ViolationExplanation> findByStatusOrderBySubmittedAtDesc(ViolationExplanation.ExplanationStatus status);
    
    /**
     * Find explanations by status with pagination
     * @param status the explanation status
     * @param pageable pagination parameters
     * @return page of explanations
     */
    Page<ViolationExplanation> findByStatusOrderBySubmittedAtDesc(ViolationExplanation.ExplanationStatus status, Pageable pageable);
    
    /**
     * Find explanations pending review
     * @return list of explanations pending review
     */
    @Query("SELECT ve FROM ViolationExplanation ve " +
           "WHERE ve.status IN ('SUBMITTED', 'UNDER_REVIEW') " +
           "ORDER BY ve.submittedAt ASC")
    List<ViolationExplanation> findPendingReview();
    
    /**
     * Find explanations pending review with pagination
     * @param pageable pagination parameters
     * @return page of explanations pending review
     */
    @Query("SELECT ve FROM ViolationExplanation ve " +
           "WHERE ve.status IN ('SUBMITTED', 'UNDER_REVIEW') " +
           "ORDER BY ve.submittedAt ASC")
    Page<ViolationExplanation> findPendingReview(Pageable pageable);
    
    /**
     * Find explanations reviewed by specific user
     * @param reviewedBy the user ID who reviewed
     * @return list of reviewed explanations
     */
    List<ViolationExplanation> findByReviewedByIdOrderByReviewedAtDesc(Long reviewedBy);
    
    /**
     * Find explanations by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of explanations in date range
     */
    List<ViolationExplanation> findBySubmittedAtBetweenOrderBySubmittedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find latest explanation for a violation
     * @param violationId the violation ID
     * @return optional latest explanation
     */
    Optional<ViolationExplanation> findFirstByViolationIdOrderBySubmittedAtDesc(Long violationId);
    
    /**
     * Find overdue explanations (submitted but not reviewed after X days)
     * @param daysSince date threshold
     * @return list of overdue explanations
     */
    @Query("SELECT ve FROM ViolationExplanation ve " +
           "WHERE ve.status IN ('SUBMITTED', 'UNDER_REVIEW') " +
           "AND ve.submittedAt <= :daysSince " +
           "ORDER BY ve.submittedAt ASC")
    List<ViolationExplanation> findOverdueForReview(@Param("daysSince") LocalDateTime daysSince);
    
    /**
     * Count explanations by status
     * @param status the explanation status
     * @return count of explanations
     */
    long countByStatus(ViolationExplanation.ExplanationStatus status);
    
    /**
     * Count explanations by submitted user
     * @param submittedBy the user ID who submitted
     * @return count of explanations
     */
    long countBySubmittedById(Long submittedBy);
    
    /**
     * Count explanations by submitted user and status
     * @param submittedBy the user ID who submitted
     * @param status the explanation status
     * @return count of explanations
     */
    long countBySubmittedByIdAndStatus(Long submittedBy, ViolationExplanation.ExplanationStatus status);
    
    /**
     * Get explanation statistics by status
     * @param startDate start date
     * @param endDate end date
     * @return explanation statistics
     */
    @Query("SELECT " +
           "ve.status as status, " +
           "COUNT(ve) as count, " +
           "AVG(CASE WHEN ve.reviewedAt IS NOT NULL AND ve.submittedAt IS NOT NULL " +
           "         THEN DATEDIFF(HOUR, ve.submittedAt, ve.reviewedAt) ELSE NULL END) as avgReviewTimeHours " +
           "FROM ViolationExplanation ve " +
           "WHERE ve.submittedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ve.status " +
           "ORDER BY COUNT(ve) DESC")
    List<Object[]> getExplanationStatisticsByStatus(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get explanation statistics by user
     * @param startDate start date
     * @param endDate end date
     * @return explanation statistics by user
     */
    @Query("SELECT " +
           "ve.submittedBy.id as userId, " +
           "ve.submittedBy.fullName as userName, " +
           "COUNT(ve) as totalExplanations, " +
           "SUM(CASE WHEN ve.status = 'APPROVED' THEN 1 ELSE 0 END) as approvedExplanations, " +
           "SUM(CASE WHEN ve.status = 'REJECTED' THEN 1 ELSE 0 END) as rejectedExplanations " +
           "FROM ViolationExplanation ve " +
           "WHERE ve.submittedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ve.submittedBy.id, ve.submittedBy.fullName " +
           "ORDER BY COUNT(ve) DESC")
    List<Object[]> getExplanationStatisticsByUser(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Check if explanation exists for violation
     * @param violationId the violation ID
     * @return true if explanation exists
     */
    boolean existsByViolationId(Long violationId);
    
    /**
     * Find explanations with evidence files
     * @return list of explanations that have evidence files
     */
    @Query("SELECT DISTINCT ve FROM ViolationExplanation ve " +
           "JOIN ve.evidenceFiles ef " +
           "ORDER BY ve.submittedAt DESC")
    List<ViolationExplanation> findExplanationsWithEvidence();
    
    /**
     * Find explanations without evidence files
     * @return list of explanations without evidence files
     */
    @Query("SELECT ve FROM ViolationExplanation ve " +
           "WHERE NOT EXISTS (SELECT 1 FROM ExplanationEvidence ef WHERE ef.explanation = ve) " +
           "ORDER BY ve.submittedAt DESC")
    List<ViolationExplanation> findExplanationsWithoutEvidence();
    
    /**
     * Get monthly explanation summary
     * @param year the year
     * @param month the month
     * @return monthly explanation summary
     */
    @Query("SELECT " +
           "ve.status as status, " +
           "COUNT(ve) as count " +
           "FROM ViolationExplanation ve " +
           "WHERE YEAR(ve.submittedAt) = :year " +
           "AND MONTH(ve.submittedAt) = :month " +
           "GROUP BY ve.status " +
           "ORDER BY COUNT(ve) DESC")
    List<Object[]> getMonthlyExplanationSummary(@Param("year") int year, @Param("month") int month);
}
