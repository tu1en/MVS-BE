package com.classroomapp.classroombackend.repository.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Repository interface for AttendanceViolation entity
 * Fixed version with proper @Query annotations
 */
@Repository
public interface AttendanceViolationRepository extends JpaRepository<AttendanceViolation, Long> {
    
    /**
     * Find violations by user
     * @param user the user
     * @return list of violations
     */
    List<AttendanceViolation> findByUserOrderByViolationDateDesc(User user);
    
    /**
     * Find violations by user ID
     * @param userId the user ID
     * @return list of violations
     */
    List<AttendanceViolation> findByUserIdOrderByViolationDateDesc(Long userId);
    
    /**
     * Find violations by user ID with pagination
     * @param userId the user ID
     * @param pageable pagination info
     * @return page of violations
     */
    Page<AttendanceViolation> findByUserIdOrderByViolationDateDesc(Long userId, Pageable pageable);
    
    /**
     * Find violations by status
     * @param status the violation status
     * @return list of violations
     */
    List<AttendanceViolation> findByStatusOrderByViolationDateDesc(AttendanceViolation.ViolationStatus status);
    
    /**
     * Find violations by status with pagination
     * @param status the violation status
     * @param pageable pagination info
     * @return page of violations
     */
    Page<AttendanceViolation> findByStatusOrderByViolationDateDesc(AttendanceViolation.ViolationStatus status, Pageable pageable);
    
    /**
     * Find violations by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of violations
     */
    List<AttendanceViolation> findByViolationDateBetweenOrderByViolationDateDesc(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find violations by date range with pagination
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination info
     * @return page of violations
     */
    Page<AttendanceViolation> findByViolationDateBetweenOrderByViolationDateDesc(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find violations by user and status
     * @param user the user
     * @param status the violation status
     * @return list of violations
     */
    List<AttendanceViolation> findByUserAndStatusOrderByViolationDateDesc(User user, AttendanceViolation.ViolationStatus status);
    
    /**
     * Find violations by user and date range
     * @param user the user
     * @param startDate start date
     * @param endDate end date
     * @return list of violations
     */
    List<AttendanceViolation> findByUserAndViolationDateBetweenOrderByViolationDateDesc(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find violations needing explanation
     * @return list of violations that need explanation
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_EXPLANATION' OR av.status = 'NEEDS_EXPLANATION' " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsNeedingExplanation();
    
    /**
     * Find violations needing explanation with pagination
     * @param pageable pagination info
     * @return page of violations that need explanation
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_EXPLANATION' OR av.status = 'NEEDS_EXPLANATION' " +
           "ORDER BY av.violationDate DESC")
    Page<AttendanceViolation> findViolationsNeedingExplanation(Pageable pageable);
    
    /**
     * Find violations by user that need explanation
     * @param userId the user ID
     * @return list of violations needing explanation
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.user.id = :userId " +
           "AND (av.status = 'PENDING_EXPLANATION' OR av.status = 'NEEDS_EXPLANATION') " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsNeedingExplanationByUser(@Param("userId") Long userId);
    
    /**
     * Find overdue violations (pending explanation for more than specified days)
     * @param cutoffDate cutoff date
     * @return list of overdue violations
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE (av.status = 'PENDING_EXPLANATION' OR av.status = 'NEEDS_EXPLANATION') " +
           "AND av.violationDate < :cutoffDate " +
           "ORDER BY av.violationDate ASC")
    List<AttendanceViolation> findOverdueViolations(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Count violations by user and status
     * @param user the user
     * @param status the status
     * @return count of violations
     */
    long countByUserAndStatus(User user, AttendanceViolation.ViolationStatus status);
    
    /**
     * Count violations by user in date range
     * @param user the user
     * @param startDate start date
     * @param endDate end date
     * @return count of violations
     */
    long countByUserAndViolationDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find violations by type
     * @param violationType the violation type
     * @return list of violations
     */
    List<AttendanceViolation> findByViolationTypeOrderByViolationDateDesc(AttendanceViolation.ViolationType violationType);
    
    /**
     * Find violations by type with pagination
     * @param violationType the violation type
     * @param pageable pagination info
     * @return page of violations
     */
    Page<AttendanceViolation> findByViolationTypeOrderByViolationDateDesc(AttendanceViolation.ViolationType violationType, Pageable pageable);
    
    /**
     * Find violations by severity
     * @param severity the violation severity
     * @return list of violations
     */
    List<AttendanceViolation> findBySeverityOrderByViolationDateDesc(AttendanceViolation.ViolationSeverity severity);
    
    /**
     * Find violations by severity with pagination
     * @param severity the violation severity
     * @param pageable pagination info
     * @return page of violations
     */
    Page<AttendanceViolation> findBySeverityOrderByViolationDateDesc(AttendanceViolation.ViolationSeverity severity, Pageable pageable);
    
    /**
     * Get violation statistics by type
     * @param startDate start date
     * @param endDate end date
     * @return violation statistics
     */
    @Query("SELECT " +
           "av.violationType as violationType, " +
           "COUNT(av) as count, " +
           "AVG(av.deviationMinutes) as avgDeviation " +
           "FROM AttendanceViolation av " +
           "WHERE av.violationDate BETWEEN :startDate AND :endDate " +
           "GROUP BY av.violationType " +
           "ORDER BY COUNT(av) DESC")
    List<Object[]> getViolationStatisticsByType(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly violation statistics
     * @param year the year
     * @param month the month
     * @return monthly statistics
     */
    @Query("SELECT " +
           "av.violationType as violationType, " +
           "COUNT(av) as count, " +
           "av.severity as severity " +
           "FROM AttendanceViolation av " +
           "WHERE YEAR(av.violationDate) = :year " +
           "AND MONTH(av.violationDate) = :month " +
           "GROUP BY av.violationType, av.severity " +
           "ORDER BY COUNT(av) DESC")
    List<Object[]> getMonthlyViolationSummary(@Param("year") int year, @Param("month") int month);
    
    /**
     * Find violations with explanations
     * @return list of violations that have explanations
     */
    @Query("SELECT DISTINCT av FROM AttendanceViolation av " +
           "LEFT JOIN FETCH av.explanations " +
           "WHERE av.explanations IS NOT EMPTY " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsWithExplanations();
    
    /**
     * Find violations without explanations
     * @return list of violations that don't have explanations
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.explanations IS EMPTY " +
           "AND (av.status = 'PENDING_EXPLANATION' OR av.status = 'NEEDS_EXPLANATION') " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsWithoutExplanations();
    
    /**
     * Find recent violations (within specified days)
     * @param cutoffDate cutoff date
     * @return list of recent violations
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.violationDate >= :cutoffDate " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findRecentViolations(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Find violations by escalation level
     * @param escalationLevel the escalation level
     * @return list of violations
     */
    List<AttendanceViolation> findByEscalationLevelOrderByViolationDateDesc(Long escalationLevel);
    
    /**
     * Find auto-detected violations
     * @param autoDetected true for auto-detected, false for manual
     * @return list of violations
     */
    List<AttendanceViolation> findByAutoDetectedOrderByDetectionTimeDesc(Boolean autoDetected);
    
    /**
     * Find auto-detected violations with pagination
     * @param autoDetected true for auto-detected, false for manual
     * @param pageable pagination info
     * @return page of violations
     */
    Page<AttendanceViolation> findByAutoDetectedOrderByDetectionTimeDesc(Boolean autoDetected, Pageable pageable);
    
    /**
     * Count violations by user ID
     * @param userId the user ID
     * @return count of violations
     */
    long countByUserId(Long userId);
    
    /**
     * Count violations by status
     * @param status the violation status
     * @return count of violations
     */
    long countByStatus(AttendanceViolation.ViolationStatus status);
    
    /**
     * Count violations by user and date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return count of violations
     */
    long countByUserIdAndViolationDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Count violations by user and status
     * @param userId the user ID
     * @param status the violation status
     * @return count of violations
     */
    long countByUserIdAndStatus(Long userId, AttendanceViolation.ViolationStatus status);
    
    /**
     * Find violations by user ID and date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return list of violations
     */
    List<AttendanceViolation> findByUserIdAndViolationDateBetweenOrderByViolationDateDesc(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Check if violation exists for user on specific date and type
     * @param userId the user ID
     * @param date the date
     * @param violationType the violation type
     * @return true if exists, false otherwise
     */
    boolean existsByUserIdAndViolationDateAndViolationType(Long userId, LocalDate date, AttendanceViolation.ViolationType violationType);
    
    // ============ FIXED METHODS - Use @Query instead of method name parsing ============
    
    /**
     * Find violations pending review with pagination
     * Fixed with proper @Query annotation
     * @param pageable pagination info
     * @return page of violations pending review
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_REVIEW' OR av.status = 'UNDER_REVIEW' " +
           "ORDER BY av.violationDate DESC")
    Page<AttendanceViolation> findViolationsPendingReview(Pageable pageable);
    
    /**
     * Find violations pending review
     * Fixed with proper @Query annotation
     * @return list of violations pending review
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_REVIEW' OR av.status = 'UNDER_REVIEW' " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsPendingReview();
    
    // ============ OTHER METHODS ============
    
    /**
     * Find violations by resolved by user
     * @param resolvedBy the user ID who resolved the violation
     * @return list of violations
     */
    List<AttendanceViolation> findByResolvedByOrderByResolvedAtDesc(Long resolvedBy);
    
    /**
     * Find violations by shift assignment ID
     * @param shiftAssignmentId the shift assignment ID
     * @return list of violations
     */
    List<AttendanceViolation> findByShiftAssignmentIdOrderByViolationDateDesc(Long shiftAssignmentId);
    
    /**
     * Find violations by violation date
     * @param date the violation date
     * @return list of violations
     */
    List<AttendanceViolation> findByViolationDateOrderByUserIdAsc(LocalDate date);
    
    /**
     * Get violation statistics by user
     * @param startDate start date
     * @param endDate end date
     * @return violation statistics by user
     */
    @Query("SELECT " +
           "av.user.id as userId, " +
           "av.user.fullName as fullName, " +
           "COUNT(av) as totalViolations, " +
           "COUNT(CASE WHEN av.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN av.status = 'REJECTED' THEN 1 END) as rejectedCount " +
           "FROM AttendanceViolation av " +
           "WHERE av.violationDate BETWEEN :startDate AND :endDate " +
           "GROUP BY av.user.id, av.user.fullName " +
           "ORDER BY COUNT(av) DESC")
    List<Object[]> getViolationStatisticsByUser(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}