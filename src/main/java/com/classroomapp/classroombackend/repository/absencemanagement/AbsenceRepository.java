package com.classroomapp.classroombackend.repository.absencemanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Absence;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    
    // Find absences by user ID
    List<Absence> findByUserId(Long userId);
    
    // Find absences by status
    List<Absence> findByStatus(String status);
    
    // Find pending absences for management
    List<Absence> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find absences by user and status
    List<Absence> findByUserIdAndStatus(Long userId, String status);
    
    // Find absences within date range for a user (to calculate used leave)
    @Query("SELECT a FROM Absence a WHERE a.userId = :userId AND a.status = 'APPROVED' " +
           "AND a.startDate >= :fromDate AND a.endDate <= :toDate")
    List<Absence> findApprovedAbsencesByUserIdAndDateRange(
        @Param("userId") Long userId, 
        @Param("fromDate") LocalDate fromDate, 
        @Param("toDate") LocalDate toDate);
    
    // Find pending absences for a user (to calculate pending leave)
    @Query("SELECT a FROM Absence a WHERE a.userId = :userId AND a.status = 'PENDING'")
    List<Absence> findPendingAbsencesByUserId(@Param("userId") Long userId);
    
    // Calculate total approved leave days for a user within date range
    @Query("SELECT COALESCE(SUM(a.numberOfDays), 0) FROM Absence a WHERE a.userId = :userId " +
           "AND a.status = 'APPROVED' AND a.startDate >= :fromDate AND a.endDate <= :toDate")
    Integer calculateUsedLeaveDays(
        @Param("userId") Long userId, 
        @Param("fromDate") LocalDate fromDate, 
        @Param("toDate") LocalDate toDate);
    
    // Calculate total pending leave days for a user
    @Query("SELECT COALESCE(SUM(a.numberOfDays), 0) FROM Absence a WHERE a.userId = :userId " +
           "AND a.status = 'PENDING'")
    Integer calculatePendingLeaveDays(@Param("userId") Long userId);
    
    // Check for overlapping leave requests
    @Query("SELECT COUNT(a) > 0 FROM Absence a WHERE a.userId = :userId " +
           "AND a.status IN ('PENDING', 'APPROVED') " +
           "AND ((a.startDate <= :endDate AND a.endDate >= :startDate))")
    Boolean hasOverlappingLeave(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
} 