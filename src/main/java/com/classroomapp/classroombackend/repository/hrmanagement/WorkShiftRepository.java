package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.WorkShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkShift entity
 */
@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    
    /**
     * Find work shift by name
     * @param name the shift name
     * @return Optional containing the shift if found
     */
    Optional<WorkShift> findByName(String name);
    
    /**
     * Check if shift name exists (case-insensitive)
     * @param name the shift name to check
     * @return true if name exists
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Check if shift name exists excluding specific ID (for updates)
     * @param name the shift name to check
     * @param id the ID to exclude
     * @return true if name exists for other records
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    /**
     * Find all active work shifts
     * @return list of active shifts
     */
    List<WorkShift> findByIsActiveTrueOrderByName();
    
    /**
     * Find active work shifts with pagination
     * @param pageable pagination parameters
     * @return page of active shifts
     */
    Page<WorkShift> findByIsActiveTrueOrderByName(Pageable pageable);
    
    /**
     * Find work shifts by name containing (case-insensitive)
     * @param name the name pattern to search
     * @param pageable pagination parameters
     * @return page of matching shifts
     */
    Page<WorkShift> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    
    /**
     * Find work shifts created by specific user
     * @param createdBy the user ID who created the shifts
     * @return list of shifts created by the user
     */
    List<WorkShift> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    /**
     * Get work shifts with assignment count
     * @return list of shifts with assignment counts
     */
    @Query("SELECT ws, COUNT(usa.id) as assignmentCount " +
           "FROM WorkShift ws " +
           "LEFT JOIN ws.assignments usa ON usa.isActive = true " +
           "WHERE ws.isActive = true " +
           "GROUP BY ws.id " +
           "ORDER BY ws.name")
    List<Object[]> findActiveShiftsWithAssignmentCount();
    
    /**
     * Find shifts that have no active assignments
     * @return list of unused shifts
     */
    @Query("SELECT ws FROM WorkShift ws " +
           "WHERE ws.isActive = true " +
           "AND NOT EXISTS (SELECT 1 FROM UserShiftAssignment usa " +
           "                WHERE usa.workShift = ws AND usa.isActive = true)")
    List<WorkShift> findUnusedActiveShifts();
    
    /**
     * Count active work shifts
     * @return number of active shifts
     */
    long countByIsActiveTrue();
    
    /**
     * Find shifts by time range overlap
     * @param startTime start time to check
     * @param endTime end time to check
     * @return list of overlapping shifts
     */
    @Query("SELECT ws FROM WorkShift ws " +
           "WHERE ws.isActive = true " +
           "AND ((ws.startTime <= :startTime AND ws.endTime > :startTime) " +
           "     OR (ws.startTime < :endTime AND ws.endTime >= :endTime) " +
           "     OR (ws.startTime >= :startTime AND ws.endTime <= :endTime))")
    List<WorkShift> findOverlappingShifts(@Param("startTime") java.time.LocalTime startTime, 
                                         @Param("endTime") java.time.LocalTime endTime);
}
