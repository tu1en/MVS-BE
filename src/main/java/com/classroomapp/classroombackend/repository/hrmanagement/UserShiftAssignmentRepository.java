package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.UserShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserShiftAssignment entity
 */
@Repository
public interface UserShiftAssignmentRepository extends JpaRepository<UserShiftAssignment, Long> {
    
    /**
     * Find assignments by user ID
     * @param userId the user ID
     * @return list of assignments for the user
     */
    List<UserShiftAssignment> findByUserIdOrderByStartDateDesc(Long userId);
    
    /**
     * Find active assignments by user ID
     * @param userId the user ID
     * @return list of active assignments for the user
     */
    List<UserShiftAssignment> findByUserIdAndIsActiveTrueOrderByStartDateDesc(Long userId);
    
    /**
     * Find assignments by shift ID
     * @param shiftId the shift ID
     * @return list of assignments for the shift
     */
    List<UserShiftAssignment> findByWorkShiftIdOrderByStartDateDesc(Long shiftId);
    
    /**
     * Find active assignments by shift ID
     * @param shiftId the shift ID
     * @return list of active assignments for the shift
     */
    List<UserShiftAssignment> findByWorkShiftIdAndIsActiveTrueOrderByStartDateDesc(Long shiftId);
    
    /**
     * Find assignments for a specific date
     * @param date the date to check
     * @return list of assignments valid for the date
     */
    @Query("SELECT usa FROM UserShiftAssignment usa " +
           "WHERE usa.isActive = true " +
           "AND usa.startDate <= :date " +
           "AND usa.endDate >= :date")
    List<UserShiftAssignment> findActiveAssignmentsForDate(@Param("date") LocalDate date);
    
    /**
     * Find user's assignment for a specific date
     * @param userId the user ID
     * @param date the date to check
     * @return optional assignment for the user on the date
     */
    @Query("SELECT usa FROM UserShiftAssignment usa " +
           "WHERE usa.user.id = :userId " +
           "AND usa.isActive = true " +
           "AND usa.startDate <= :date " +
           "AND usa.endDate >= :date")
    Optional<UserShiftAssignment> findUserAssignmentForDate(@Param("userId") Long userId, 
                                                           @Param("date") LocalDate date);
    
    /**
     * Find overlapping assignments for a user
     * @param userId the user ID
     * @param startDate start date of new assignment
     * @param endDate end date of new assignment
     * @param excludeId ID to exclude (for updates)
     * @return list of overlapping assignments
     */
    @Query("SELECT usa FROM UserShiftAssignment usa " +
           "WHERE usa.user.id = :userId " +
           "AND usa.isActive = true " +
           "AND usa.id != :excludeId " +
           "AND NOT (usa.endDate < :startDate OR usa.startDate > :endDate)")
    List<UserShiftAssignment> findOverlappingAssignments(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("excludeId") Long excludeId);
    
    /**
     * Find overlapping assignments for a user (for new assignments)
     * @param userId the user ID
     * @param startDate start date of new assignment
     * @param endDate end date of new assignment
     * @return list of overlapping assignments
     */
    @Query("SELECT usa FROM UserShiftAssignment usa " +
           "WHERE usa.user.id = :userId " +
           "AND usa.isActive = true " +
           "AND NOT (usa.endDate < :startDate OR usa.startDate > :endDate)")
    List<UserShiftAssignment> findOverlappingAssignments(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    
    /**
     * Find assignments with pagination
     * @param pageable pagination parameters
     * @return page of assignments
     */
    Page<UserShiftAssignment> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find assignments by date range
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination parameters
     * @return page of assignments in date range
     */
    @Query("SELECT usa FROM UserShiftAssignment usa " +
           "WHERE usa.isActive = true " +
           "AND NOT (usa.endDate < :startDate OR usa.startDate > :endDate) " +
           "ORDER BY usa.startDate DESC")
    Page<UserShiftAssignment> findAssignmentsInDateRange(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        Pageable pageable);
    
    /**
     * Count assignments for a shift
     * @param shiftId the shift ID
     * @return number of active assignments for the shift
     */
    long countByWorkShiftIdAndIsActiveTrue(Long shiftId);
    
    /**
     * Count assignments for a user
     * @param userId the user ID
     * @return number of active assignments for the user
     */
    long countByUserIdAndIsActiveTrue(Long userId);
    
    /**
     * Find assignments created by specific user
     * @param createdBy the user ID who created the assignments
     * @return list of assignments created by the user
     */
    List<UserShiftAssignment> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}
