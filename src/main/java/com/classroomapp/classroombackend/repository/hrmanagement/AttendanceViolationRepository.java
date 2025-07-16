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

/**
 * Repository interface for AttendanceViolation entity
 */
@Repository
public interface AttendanceViolationRepository extends JpaRepository<AttendanceViolation, Long> {
    
    /**
     * Find violations by user ID
     * @param userId the user ID
     * @return list of violations
     */
    List<AttendanceViolation> findByUserIdOrderByViolationDateDesc(Long userId);
    
    /**
     * Find violations by user ID with pagination
     * @param userId the user ID
     * @param pageable pagination parameters
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
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolation> findByStatusOrderByViolationDateDesc(AttendanceViolation.ViolationStatus status, Pageable pageable);
    
    /**
     * Find violations by user and status
     * @param userId the user ID
     * @param status the violation status
     * @return list of violations
     */
    List<AttendanceViolation> findByUserIdAndStatusOrderByViolationDateDesc(Long userId, AttendanceViolation.ViolationStatus status);
    
    /**
     * Find violations by violation type
     * @param violationType the violation type
     * @return list of violations
     */
    List<AttendanceViolation> findByViolationTypeOrderByViolationDateDesc(AttendanceViolation.ViolationType violationType);

    /**
     * Find violations by violation type with pagination
     * @param violationType the violation type
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolation> findByViolationTypeOrderByViolationDateDesc(AttendanceViolation.ViolationType violationType, Pageable pageable);
    
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
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolation> findByViolationDateBetweenOrderByViolationDateDesc(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find violations by user and date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return list of violations
     */
    List<AttendanceViolation> findByUserIdAndViolationDateBetweenOrderByViolationDateDesc(
        Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find violations for a specific date
     * @param violationDate the violation date
     * @return list of violations
     */
    List<AttendanceViolation> findByViolationDateOrderByUserIdAsc(LocalDate violationDate);
    
    /**
     * Find violations that need explanation
     * @return list of violations needing explanation
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_EXPLANATION' " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsNeedingExplanation();
    
    /**
     * Find violations that need explanation with pagination
     * @param pageable pagination parameters
     * @return page of violations needing explanation
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_EXPLANATION' " +
           "ORDER BY av.violationDate DESC")
    Page<AttendanceViolation> findViolationsNeedingExplanation(Pageable pageable);
    
    /**
     * Find violations pending review
     * @return list of violations pending review
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status IN ('EXPLANATION_SUBMITTED', 'UNDER_REVIEW') " +
           "ORDER BY av.violationDate DESC")
    List<AttendanceViolation> findViolationsPendingReview();
    
    /**
     * Find violations pending review with pagination
     * @param pageable pagination parameters
     * @return page of violations pending review
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status IN ('EXPLANATION_SUBMITTED', 'UNDER_REVIEW') " +
           "ORDER BY av.violationDate DESC")
    Page<AttendanceViolation> findViolationsPendingReview(Pageable pageable);
    
    /**
     * Find overdue violations (no explanation after X days)
     * @param daysSince days since violation
     * @return list of overdue violations
     */
    @Query("SELECT av FROM AttendanceViolation av " +
           "WHERE av.status = 'PENDING_EXPLANATION' " +
           "AND av.violationDate <= :daysSince " +
           "ORDER BY av.violationDate ASC")
    List<AttendanceViolation> findOverdueViolations(@Param("daysSince") LocalDate daysSince);
    
    /**
     * Find violations by severity
     * @param severity the violation severity
     * @return list of violations
     */
    List<AttendanceViolation> findBySeverityOrderByViolationDateDesc(AttendanceViolation.ViolationSeverity severity);

    /**
     * Find violations by severity with pagination
     * @param severity the violation severity
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolation> findBySeverityOrderByViolationDateDesc(AttendanceViolation.ViolationSeverity severity, Pageable pageable);
    
    /**
     * Find auto-detected violations
     * @param autoDetected whether auto-detected
     * @return list of violations
     */
    List<AttendanceViolation> findByAutoDetectedOrderByDetectionTimeDesc(Boolean autoDetected);

    /**
     * Find auto-detected violations with pagination
     * @param autoDetected whether auto-detected
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolation> findByAutoDetectedOrderByDetectionTimeDesc(Boolean autoDetected, Pageable pageable);
    
    /**
     * Check if violation exists for user and date
     * @param userId the user ID
     * @param violationDate the violation date
     * @param violationType the violation type
     * @return true if exists
     */
    boolean existsByUserIdAndViolationDateAndViolationType(Long userId, LocalDate violationDate, 
                                                          AttendanceViolation.ViolationType violationType);
    
    /**
     * Count violations by user
     * @param userId the user ID
     * @return count of violations
     */
    long countByUserId(Long userId);
    
    /**
     * Count violations by user and status
     * @param userId the user ID
     * @param status the violation status
     * @return count of violations
     */
    long countByUserIdAndStatus(Long userId, AttendanceViolation.ViolationStatus status);

    /**
     * Count violations by status
     * @param status the violation status
     * @return count of violations
     */
    long countByStatus(AttendanceViolation.ViolationStatus status);
    
    /**
     * Count violations by user in date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return count of violations
     */
    long countByUserIdAndViolationDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
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
    List<Object[]> getViolationStatisticsByType(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Get violation statistics by user
     * @param startDate start date
     * @param endDate end date
     * @return violation statistics by user
     */
    @Query("SELECT " +
           "av.user.id as userId, " +
           "av.user.fullName as userName, " +
           "COUNT(av) as totalViolations, " +
           "SUM(CASE WHEN av.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolvedViolations " +
           "FROM AttendanceViolation av " +
           "WHERE av.violationDate BETWEEN :startDate AND :endDate " +
           "GROUP BY av.user.id, av.user.fullName " +
           "ORDER BY COUNT(av) DESC")
    List<Object[]> getViolationStatisticsByUser(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly violation summary
     * @param year the year
     * @param month the month
     * @return monthly violation summary
     */
    @Query("SELECT " +
           "av.violationType as violationType, " +
           "COUNT(av) as count, " +
           "AVG(CASE WHEN av.deviationMinutes IS NOT NULL THEN av.deviationMinutes ELSE 0 END) as avgDeviation " +
           "FROM AttendanceViolation av " +
           "WHERE YEAR(av.violationDate) = :year " +
           "AND MONTH(av.violationDate) = :month " +
           "GROUP BY av.violationType " +
           "ORDER BY COUNT(av) DESC")
    List<Object[]> getMonthlyViolationSummary(@Param("year") int year, @Param("month") int month);
    
    /**
     * Find violations resolved by specific user
     * @param resolvedBy the user ID who resolved violations
     * @return list of resolved violations
     */
    List<AttendanceViolation> findByResolvedByOrderByResolvedAtDesc(Long resolvedBy);
    
    /**
     * Find violations by shift assignment
     * @param shiftAssignmentId the shift assignment ID
     * @return list of violations
     */
    List<AttendanceViolation> findByShiftAssignmentIdOrderByViolationDateDesc(Long shiftAssignmentId);
}
