package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateWorkShiftDto;
import com.classroomapp.classroombackend.dto.hrmanagement.WorkShiftDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for WorkShift management
 */
public interface WorkShiftService {
    
    /**
     * Create a new work shift
     * @param createDto the shift data
     * @param createdBy the user ID who creates the shift
     * @return created shift DTO
     */
    WorkShiftDto createShift(CreateWorkShiftDto createDto, Long createdBy);
    
    /**
     * Update an existing work shift
     * @param id the shift ID
     * @param updateDto the updated shift data
     * @return updated shift DTO
     */
    WorkShiftDto updateShift(Long id, CreateWorkShiftDto updateDto);
    
    /**
     * Get work shift by ID
     * @param id the shift ID
     * @return shift DTO
     */
    WorkShiftDto getShiftById(Long id);
    
    /**
     * Get all active work shifts
     * @return list of active shifts
     */
    List<WorkShiftDto> getAllActiveShifts();
    
    /**
     * Get work shifts with pagination
     * @param pageable pagination parameters
     * @return page of shifts
     */
    Page<WorkShiftDto> getShifts(Pageable pageable);
    
    /**
     * Search work shifts by name
     * @param name the name pattern to search
     * @param pageable pagination parameters
     * @return page of matching shifts
     */
    Page<WorkShiftDto> searchShiftsByName(String name, Pageable pageable);
    
    /**
     * Delete a work shift (soft delete)
     * @param id the shift ID
     */
    void deleteShift(Long id);
    
    /**
     * Activate/deactivate a work shift
     * @param id the shift ID
     * @param isActive the new active status
     * @return updated shift DTO
     */
    WorkShiftDto toggleShiftStatus(Long id, boolean isActive);
    
    /**
     * Check if shift name is available
     * @param name the shift name
     * @param excludeId the ID to exclude (for updates)
     * @return true if name is available
     */
    boolean isShiftNameAvailable(String name, Long excludeId);
    
    /**
     * Get shifts with assignment counts
     * @return list of shifts with assignment statistics
     */
    List<WorkShiftDto> getShiftsWithAssignmentCounts();
    
    /**
     * Get unused shifts (no active assignments)
     * @return list of unused shifts
     */
    List<WorkShiftDto> getUnusedShifts();
    
    /**
     * Validate shift data
     * @param createDto the shift data to validate
     * @return validation result
     */
    boolean validateShiftData(CreateWorkShiftDto createDto);
}
