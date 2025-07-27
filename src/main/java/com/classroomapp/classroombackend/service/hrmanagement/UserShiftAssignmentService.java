package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftAssignmentDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UserShiftAssignmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for UserShiftAssignment management
 */
public interface UserShiftAssignmentService {
    
    /**
     * Create shift assignments for multiple users
     * @param createDto the assignment data
     * @param createdBy the user ID who creates the assignments
     * @return list of created assignment DTOs
     */
    List<UserShiftAssignmentDto> createShiftAssignments(CreateShiftAssignmentDto createDto, Long createdBy);
    
    /**
     * Update an existing shift assignment
     * @param id the assignment ID
     * @param updateDto the updated assignment data
     * @return updated assignment DTO
     */
    UserShiftAssignmentDto updateShiftAssignment(Long id, CreateShiftAssignmentDto updateDto);
    
    /**
     * Get shift assignment by ID
     * @param id the assignment ID
     * @return assignment DTO
     */
    UserShiftAssignmentDto getAssignmentById(Long id);
    
    /**
     * Get assignments for a specific user
     * @param userId the user ID
     * @param activeOnly whether to return only active assignments
     * @return list of assignments for the user
     */
    List<UserShiftAssignmentDto> getAssignmentsByUser(Long userId, boolean activeOnly);
    
    /**
     * Get assignments for a specific shift
     * @param shiftId the shift ID
     * @param activeOnly whether to return only active assignments
     * @return list of assignments for the shift
     */
    List<UserShiftAssignmentDto> getAssignmentsByShift(Long shiftId, boolean activeOnly);
    
    /**
     * Get assignments with pagination
     * @param pageable pagination parameters
     * @return page of assignments
     */
    Page<UserShiftAssignmentDto> getAssignments(Pageable pageable);
    
    /**
     * Get assignments for a specific date range
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination parameters
     * @return page of assignments in date range
     */
    Page<UserShiftAssignmentDto> getAssignmentsInDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Get user's assignment for a specific date
     * @param userId the user ID
     * @param date the date to check
     * @return assignment DTO or null if no assignment
     */
    UserShiftAssignmentDto getUserAssignmentForDate(Long userId, LocalDate date);
    
    /**
     * Get all assignments for a specific date
     * @param date the date to check
     * @return list of assignments for the date
     */
    List<UserShiftAssignmentDto> getAssignmentsForDate(LocalDate date);
    
    /**
     * Delete a shift assignment (soft delete)
     * @param id the assignment ID
     */
    void deleteAssignment(Long id);
    
    /**
     * Activate/deactivate a shift assignment
     * @param id the assignment ID
     * @param isActive the new active status
     * @return updated assignment DTO
     */
    UserShiftAssignmentDto toggleAssignmentStatus(Long id, boolean isActive);
    
    /**
     * Check for overlapping assignments
     * @param userId the user ID
     * @param startDate start date of new assignment
     * @param endDate end date of new assignment
     * @param excludeId ID to exclude (for updates)
     * @return list of overlapping assignments
     */
    List<UserShiftAssignmentDto> checkOverlappingAssignments(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId);
    
    /**
     * Validate assignment data
     * @param createDto the assignment data to validate
     * @return validation result
     */
    boolean validateAssignmentData(CreateShiftAssignmentDto createDto);
    
    /**
     * Get eligible users for shift assignment (exclude Teachers)
     * @return list of eligible users
     */
    List<Object> getEligibleUsersForShiftAssignment();
    
    /**
     * Get assignment statistics for a user
     * @param userId the user ID
     * @return assignment statistics
     */
    Object getAssignmentStatistics(Long userId);
    
    /**
     * Get assignment statistics for a shift
     * @param shiftId the shift ID
     * @return assignment statistics
     */
    Object getShiftAssignmentStatistics(Long shiftId);
}
