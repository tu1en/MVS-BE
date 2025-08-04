package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.StaffShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.Shift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StaffShiftAssignmentRepository extends JpaRepository<StaffShiftAssignment, Long> {
    
    List<StaffShiftAssignment> findByStaff(User staff);
    
    List<StaffShiftAssignment> findByShift(Shift shift);
    
    @Query("SELECT sa FROM StaffShiftAssignment sa WHERE sa.staff = :staff AND sa.effectiveFrom <= :date AND (sa.effectiveUntil IS NULL OR sa.effectiveUntil >= :date)")
    List<StaffShiftAssignment> findCurrentAssignments(@Param("staff") User staff, @Param("date") LocalDate date);
    
    @Query("SELECT s FROM Shift s JOIN StaffShiftAssignment sa ON sa.shift = s WHERE sa.staff = :staff AND sa.effectiveFrom <= :date AND (sa.effectiveUntil IS NULL OR sa.effectiveUntil >= :date)")
    List<Shift> findAssignedShifts(@Param("staff") User staff, @Param("date") LocalDate date);
    
    boolean existsByStaffAndShiftAndEffectiveFromBeforeAndEffectiveUntilAfter(
            @Param("staff") User staff, 
            @Param("shift") Shift shift, 
            @Param("start") LocalDate start, 
            @Param("end") LocalDate end);
    
    // Method for range checking
    @Query("SELECT COUNT(sa) > 0 FROM StaffShiftAssignment sa WHERE sa.staff = :staff AND sa.shift = :shift AND sa.effectiveFrom <= :endDate AND (sa.effectiveUntil IS NULL OR sa.effectiveUntil >= :startDate)")
    boolean existsByStaffAndShiftAndRange(@Param("staff") User staff, @Param("shift") Shift shift, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}