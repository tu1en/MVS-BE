package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for AttendanceViolation management
 */
public interface AttendanceViolationService {
    
    /**
     * Get violation by ID
     * @param id violation ID
     * @return violation DTO
     */
    AttendanceViolationDto getViolationById(Long id);
    
    /**
     * Get violations by user
     * @param userId user ID
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolationDto> getViolationsByUser(Long userId, Pageable pageable);
    
    /**
     * Get violations by status
     * @param status violation status
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolationDto> getViolationsByStatus(AttendanceViolation.ViolationStatus status, Pageable pageable);
    
    /**
     * Get violations by type
     * @param violationType violation type
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolationDto> getViolationsByType(AttendanceViolation.ViolationType violationType, Pageable pageable);
    
    /**
     * Get violations by date range
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolationDto> getViolationsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Get violations for a specific date
     * @param date violation date
     * @return list of violations
     */
    List<AttendanceViolationDto> getViolationsForDate(LocalDate date);
    
    /**
     * Get violations needing explanation
     * @param pageable pagination parameters
     * @return page of violations needing explanation
     */
    Page<AttendanceViolationDto> getViolationsNeedingExplanation(Pageable pageable);
    
    /**
     * Get violations pending review
     * @param pageable pagination parameters
     * @return page of violations pending review
     */
    Page<AttendanceViolationDto> getViolationsPendingReview(Pageable pageable);
    
    /**
     * Get overdue violations (no explanation after X days)
     * @param daysSince days since violation
     * @return list of overdue violations
     */
    List<AttendanceViolationDto> getOverdueViolations(int daysSince);
    
    /**
     * Resolve a violation manually
     * @param id violation ID
     * @param resolvedBy user ID who resolves
     * @param resolutionNotes resolution notes
     * @return updated violation DTO
     */
    AttendanceViolationDto resolveViolation(Long id, Long resolvedBy, String resolutionNotes);
    
    /**
     * Escalate a violation to higher authority
     * @param id violation ID
     * @param escalatedBy user ID who escalates
     * @param escalationNotes escalation notes
     * @return updated violation DTO
     */
    AttendanceViolationDto escalateViolation(Long id, Long escalatedBy, String escalationNotes);
    
    /**
     * Get violation statistics by type
     * @param startDate start date
     * @param endDate end date
     * @return violation statistics
     */
    Object getViolationStatisticsByType(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get violation statistics by user
     * @param startDate start date
     * @param endDate end date
     * @return violation statistics by user
     */
    Object getViolationStatisticsByUser(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get monthly violation summary
     * @param year year
     * @param month month
     * @return monthly summary
     */
    Object getMonthlyViolationSummary(int year, int month);
    
    /**
     * Count violations by user
     * @param userId user ID
     * @return count of violations
     */
    long countViolationsByUser(Long userId);
    
    /**
     * Count violations by status
     * @param status violation status
     * @return count of violations
     */
    long countViolationsByStatus(AttendanceViolation.ViolationStatus status);
    
    /**
     * Count violations by user in date range
     * @param userId user ID
     * @param startDate start date
     * @param endDate end date
     * @return count of violations
     */
    long countViolationsByUserInDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get violations by severity
     * @param severity violation severity
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolationDto> getViolationsBySeverity(AttendanceViolation.ViolationSeverity severity, Pageable pageable);
    
    /**
     * Get auto-detected violations
     * @param autoDetected whether auto-detected
     * @param pageable pagination parameters
     * @return page of violations
     */
    Page<AttendanceViolationDto> getViolationsByAutoDetected(Boolean autoDetected, Pageable pageable);
    
    /**
     * Get violations resolved by specific user
     * @param resolvedBy user ID who resolved violations
     * @return list of resolved violations
     */
    List<AttendanceViolationDto> getViolationsResolvedBy(Long resolvedBy);
    
    /**
     * Get violations by shift assignment
     * @param shiftAssignmentId shift assignment ID
     * @return list of violations
     */
    List<AttendanceViolationDto> getViolationsByShiftAssignment(Long shiftAssignmentId);
    
    /**
     * Check if user can view violation
     * @param violationId violation ID
     * @param userId user ID
     * @return true if user can view
     */
    boolean canViewViolation(Long violationId, Long userId);
    
    /**
     * Check if user can resolve violation
     * @param violationId violation ID
     * @param userId user ID
     * @return true if user can resolve
     */
    boolean canResolveViolation(Long violationId, Long userId);
    
    /**
     * Get violation dashboard data for user
     * @param userId user ID
     * @return dashboard data
     */
    Object getViolationDashboardData(Long userId);
    
    /**
     * Get violation dashboard data for manager
     * @return dashboard data for manager
     */
    Object getManagerViolationDashboardData();
}
