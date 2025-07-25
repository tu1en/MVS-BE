package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.StaffShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.Shift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffShiftAssignmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Staff Shift Assignment - Maps staff to shifts
 * Implements Phase 2: Task #27 - Assign Shift to Staff
 */
@Service
public class StaffShiftAssignmentService {

    @Autowired
    private StaffShiftAssignmentRepository assignmentRepository;

    /**
     * Assign shift to staff member
     */
    @Transactional
    public StaffShiftAssignment assignShift(User staff, Shift shift, LocalDate effectiveFrom, 
                                           LocalDate effectiveUntil, User assignedBy) {
        validateAssignment(staff, shift, effectiveFrom, effectiveUntil);
        
        StaffShiftAssignment assignment = new StaffShiftAssignment();
        assignment.setStaff(staff);
        assignment.setShift(shift);
        assignment.setEffectiveFrom(effectiveFrom);
        assignment.setEffectiveUntil(effectiveUntil);
        assignment.setAssignedBy(assignedBy);
        
        return assignmentRepository.save(assignment);
    }

    /**
     * Bulk assignment of shifts to multiple staff
     */
    @Transactional
    public List<StaffShiftAssignment> bulkAssignShift(List<User> staffList, Shift shift, 
                                                       LocalDate effectiveFrom, LocalDate effectiveUntil, 
                                                       User assignedBy) {
        return staffList.stream()
                .map(staff -> assignShift(staff, shift, effectiveFrom, effectiveUntil, assignedBy))
                .collect(Collectors.toList());
    }

    /**
     * Get current shift assignments for staff
     */
    public List<StaffShiftAssignment> getCurrentAssignments(User staff) {
        return assignmentRepository.findCurrentAssignments(staff, LocalDate.now());
    }

    /**
     * Get shifts assigned to staff for specific date
     */
    public List<Shift> getAssignedShifts(User staff, LocalDate date) {
        return assignmentRepository.findAssignedShifts(staff, date);
    }

    /**
     * Get all assignments for staff
     */
    public List<StaffShiftAssignment> getStaffAssignments(User staff) {
        return assignmentRepository.findByStaff(staff);
    }

    /**
     * Check for shift conflicts
     */
    public boolean hasShiftConflict(User staff, LocalDate date, LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.existsByStaffAndShiftAndRange(staff, null, startDate, endDate);
    }

    /**
     * End assignment early
     */
    @Transactional
    public void endAssignmentEarly(Long assignmentId, LocalDate endDate) {
        StaffShiftAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        if (endDate.isBefore(assignment.getEffectiveFrom())) {
            throw new IllegalArgumentException("End date cannot be before effective from date");
        }
        
        assignment.setEffectiveUntil(endDate);
        assignmentRepository.save(assignment);
    }

    /**
     * Cancel assignment
     */
    @Transactional
    public void cancelAssignment(Long assignmentId, String reason) {
        assignmentRepository.deleteById(assignmentId);
    }

    /**
     * Validate assignment parameters
     */
    private void validateAssignment(User staff, Shift shift, LocalDate effectiveFrom, LocalDate effectiveUntil) {
        if (staff == null || shift == null) {
            throw new IllegalArgumentException("Staff and shift are required");
        }
        
        if (effectiveFrom == null) {
            throw new IllegalArgumentException("Effective from date is required");
        }
        
        if (effectiveUntil != null && effectiveUntil.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective until date must be after or equal to effective from date");
        }
        
        // Check for overlapping assignments
        List<StaffShiftAssignment> existingAssignments = getCurrentAssignments(staff);
        for (StaffShiftAssignment existing : existingAssignments) {
            if (hasOverlap(existing, effectiveFrom, effectiveUntil)) {
                throw new IllegalArgumentException("Staff already has a conflicting assignment");
            }
        }
    }

    /**
     * Check if date ranges overlap
     */
    private boolean hasOverlap(StaffShiftAssignment assignment, LocalDate newStart, LocalDate newEnd) {
        LocalDate existingStart = assignment.getEffectiveFrom();
        LocalDate existingEnd = assignment.getEffectiveUntil() != null ? assignment.getEffectiveUntil() : LocalDate.MAX;
        
        LocalDate start = newStart != null ? newStart : LocalDate.MIN;
        LocalDate end = newEnd != null ? newEnd : LocalDate.MAX;
        
        return !end.isBefore(existingStart) && !start.isAfter(existingEnd);
    }

    /**
     * Get assignment statistics
     */
    public java.util.Map<String, Long> getAssignmentStatistics() {
        List<StaffShiftAssignment> assignments = assignmentRepository.findAll();
        
        return java.util.Map.of(
            "totalAssignments", (long) assignments.size(),
            "activeAssignments", assignments.stream()
                .filter(a -> a.getEffectiveFrom().isBefore(LocalDate.now()) && 
                           (a.getEffectiveUntil() == null || a.getEffectiveUntil().isAfter(LocalDate.now())))
                .count(),
            "completedAssignments", assignments.stream()
                .filter(a -> a.getEffectiveUntil() != null && a.getEffectiveUntil().isBefore(LocalDate.now()))
                .count()
        );
    }
}