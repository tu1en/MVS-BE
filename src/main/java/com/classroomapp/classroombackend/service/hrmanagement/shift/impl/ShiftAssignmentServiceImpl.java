package com.classroomapp.classroombackend.service.hrmanagement.shift.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftAssignmentRepository;
import com.classroomapp.classroombackend.service.firebase.FirebaseShiftEventListener;
import com.classroomapp.classroombackend.service.firebase.FirebaseShiftService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftAssignmentService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation cá»§a ShiftAssignmentService
 * Xá»­ lÃ½ business logic cho shift assignment management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftConflictDetectionService conflictDetectionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ShiftAssignment createAssignment(ShiftAssignment assignment) {
        log.info("Táº¡o shift assignment má»›i cho employee {} ngÃ y {}", 
                assignment.getAssignedUser().getId(), assignment.getAssignmentDate());

        validateAssignment(assignment);
        
        // Kiá»ƒm tra xung Ä‘á»™t
        ShiftConflictDetectionService.ConflictCheckResult conflicts = checkConflicts(assignment);
        if (conflicts.isHasConflict()) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ táº¡o assignment: " + conflicts.getSummary());
        }

        ShiftAssignment saved = shiftAssignmentRepository.save(assignment);
        log.info("ÄÃ£ táº¡o shift assignment vá»›i ID: {}", saved.getId());

        // Publish event for Firebase sync and notifications
        eventPublisher.publishEvent(new FirebaseShiftEventListener.ShiftAssignmentCreatedEvent(saved));

        return saved;
    }

    @Override
    public List<ShiftAssignment> createBulkAssignments(List<ShiftAssignment> assignments) {
        log.info("Táº¡o bulk assignments: {} assignments", assignments.size());

        List<ShiftAssignment> validAssignments = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < assignments.size(); i++) {
            ShiftAssignment assignment = assignments.get(i);
            try {
                validateAssignment(assignment);
                
                ShiftConflictDetectionService.ConflictCheckResult conflicts = checkConflicts(assignment);
                if (conflicts.isHasConflict()) {
                    errors.add(String.format("Assignment %d: %s", i + 1, conflicts.getSummary()));
                } else {
                    validAssignments.add(assignment);
                }
            } catch (Exception e) {
                errors.add(String.format("Assignment %d: %s", i + 1, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessLogicException("Bulk assignment failed: " + String.join("; ", errors));
        }

        List<ShiftAssignment> saved = shiftAssignmentRepository.saveAll(validAssignments);
        log.info("ÄÃ£ táº¡o {} bulk assignments thÃ nh cÃ´ng", saved.size());

        return saved;
    }

    @Override
    public ShiftAssignment updateAssignment(Long id, ShiftAssignment assignment) {
        log.info("Cáº­p nháº­t shift assignment ID: {}", id);

        ShiftAssignment existing = shiftAssignmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y assignment vá»›i ID: " + id));

        // Kiá»ƒm tra xem cÃ³ thá»ƒ cáº­p nháº­t khÃ´ng
        if (existing.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ cáº­p nháº­t assignment Ä‘Ã£ hoÃ n thÃ nh");
        }

        validateAssignment(assignment);

        // Kiá»ƒm tra xung Ä‘á»™t (exclude current assignment)
        assignment.setId(id);
        ShiftConflictDetectionService.ConflictCheckResult conflicts = checkConflicts(assignment);
        if (conflicts.isHasConflict()) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ cáº­p nháº­t assignment: " + conflicts.getSummary());
        }

        // Update fields
        existing.setAssignmentDate(assignment.getAssignmentDate());
        existing.setPlannedStartTime(assignment.getPlannedStartTime());
        existing.setPlannedEndTime(assignment.getPlannedEndTime());
        existing.setPlannedHours(assignment.getPlannedHours());
        existing.setNotes(assignment.getNotes());

        ShiftAssignment updated = shiftAssignmentRepository.save(existing);
        log.info("ÄÃ£ cáº­p nháº­t shift assignment ID: {}", id);

        // Publish event for Firebase sync and notifications
        eventPublisher.publishEvent(new FirebaseShiftEventListener.ShiftAssignmentUpdatedEvent(updated, true));

        return updated;
    }

    @Override
    public void deleteAssignment(Long id) {
        log.info("XÃ³a shift assignment ID: {}", id);

        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y assignment vá»›i ID: " + id));

        if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.IN_PROGRESS) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ xÃ³a assignment Ä‘ang thá»±c hiá»‡n");
        }

        if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ xÃ³a assignment Ä‘Ã£ hoÃ n thÃ nh");
        }

        shiftAssignmentRepository.delete(assignment);
        log.info("ÄÃ£ xÃ³a shift assignment ID: {}", id);
    }

    @Override
    public void cancelAssignment(Long id, String reason) {
        log.info("Há»§y shift assignment ID: {} vá»›i lÃ½ do: {}", id, reason);

        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y assignment vá»›i ID: " + id));

        assignment.cancel(reason);
        ShiftAssignment cancelled = shiftAssignmentRepository.save(assignment);

        // Publish event for Firebase sync and notifications
        eventPublisher.publishEvent(new FirebaseShiftEventListener.ShiftAssignmentCancelledEvent(cancelled, reason));

        log.info("ÄÃ£ há»§y shift assignment ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftAssignment> findById(Long id) {
        return shiftAssignmentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByEmployeeAndDate(Long assignedUserId, LocalDate date) {
        return shiftAssignmentRepository.findByAssignedUserIdAndAssignmentDateOrderByPlannedStartTimeAsc(assignedUserId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByEmployeeAndDateRange(Long assignedUserId, LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findByAssignedUserAndDateRange(assignedUserId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByAssignedUserAndDateRange(Long assignedUserId, LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findByAssignedUserAndDateRange(assignedUserId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByDate(LocalDate date) {
        return shiftAssignmentRepository.findByAssignmentDateOrderByPlannedStartTimeAsc(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByWeek(LocalDate weekStart, Long assignedUserId) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return shiftAssignmentRepository.findByWeek(weekStart, weekEnd, assignedUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByMonth(int year, int month, Long assignedUserId) {
        return shiftAssignmentRepository.findByMonth(year, month, assignedUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftAssignment> searchAssignments(Long assignedUserId, LocalDate startDate, LocalDate endDate,
                                                  ShiftAssignment.AssignmentStatus status,
                                                  ShiftAssignment.AttendanceStatus attendanceStatus,
                                                  String search, Pageable pageable) {
        return shiftAssignmentRepository.searchAssignments(
            assignedUserId, startDate, endDate, status, attendanceStatus, search, pageable);
    }

    @Override
    public ShiftAssignment checkIn(Long assignmentId, String location) {
        log.info("Check-in cho assignment ID: {}", assignmentId);

        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y assignment vá»›i ID: " + assignmentId));

        assignment.checkIn(location);
        ShiftAssignment updated = shiftAssignmentRepository.save(assignment);

        // Publish event for Firebase sync
        eventPublisher.publishEvent(new FirebaseShiftEventListener.EmployeeCheckedInEvent(updated));

        log.info("ÄÃ£ check-in assignment ID: {} lÃºc {}", assignmentId, updated.getCheckInTime());
        return updated;
    }

    @Override
    public ShiftAssignment checkOut(Long assignmentId, String location) {
        log.info("Check-out cho assignment ID: {}", assignmentId);

        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y assignment vá»›i ID: " + assignmentId));

        assignment.checkOut(location);
        ShiftAssignment updated = shiftAssignmentRepository.save(assignment);

        // Publish event for Firebase sync
        eventPublisher.publishEvent(new FirebaseShiftEventListener.EmployeeCheckedOutEvent(updated));

        log.info("ÄÃ£ check-out assignment ID: {} lÃºc {}", assignmentId, updated.getCheckOutTime());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findPendingCheckIns() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalTime startWindow = currentTime.minusMinutes(30); // 30 phÃºt trÆ°á»›c

        return shiftAssignmentRepository.findPendingCheckIns(today, currentTime, startWindow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findPendingCheckOuts() {
        LocalDate today = LocalDate.now();
        LocalTime endWindow = LocalTime.now().plusMinutes(30); // 30 phÃºt sau

        return shiftAssignmentRepository.findPendingCheckOuts(today, endWindow);
    }

    @Override
    public void validateAssignment(ShiftAssignment assignment) {
        if (assignment == null) {
            throw new BusinessLogicException("Assignment khÃ´ng Ä‘Æ°á»£c null");
        }

        if (!assignment.isValidAssignment()) {
            throw new BusinessLogicException("ThÃ´ng tin assignment khÃ´ng há»£p lá»‡");
        }

        if (assignment.getAssignmentDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ táº¡o assignment cho ngÃ y trong quÃ¡ khá»©");
        }

        if (assignment.getAssignedUser() == null) {
            throw new BusinessLogicException("Employee khÃ´ng Ä‘Æ°á»£c null");
        }

        if (assignment.getShiftTemplate() == null) {
            throw new BusinessLogicException("Shift template khÃ´ng Ä‘Æ°á»£c null");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftConflictDetectionService.ConflictCheckResult checkConflicts(ShiftAssignment assignment) {
        return conflictDetectionService.checkAllConflicts(
            assignment.getAssignedUser().getId(),
            assignment.getAssignmentDate(),
            assignment.getPlannedStartTime(),
            assignment.getPlannedEndTime(),
            assignment.getPlannedHours(),
            assignment.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkingHoursSummary calculateWorkingHours(Long assignedUserId, LocalDate startDate, LocalDate endDate) {
        Object[] result = shiftAssignmentRepository.calculateWorkingHours(assignedUserId, startDate, endDate);
        
        if (result != null && result.length >= 3) {
            BigDecimal plannedHours = (BigDecimal) result[0];
            BigDecimal actualHours = (BigDecimal) result[1];
            BigDecimal overtimeHours = (BigDecimal) result[2];

            List<ShiftAssignment> assignments = findByAssignedUserAndDateRange(assignedUserId, startDate, endDate);
            int totalAssignments = assignments.size();
            int completedAssignments = (int) assignments.stream()
                .filter(a -> a.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED)
                .count();

            return new WorkingHoursSummary(plannedHours, actualHours, overtimeHours, 
                                         totalAssignments, completedAssignments);
        }

        return new WorkingHoursSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findOvertimeAssignments(LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findOvertimeAssignments(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findAttendanceIssues(LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findAttendanceIssues(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findSwappableAssignments(Long assignedUserId, LocalDate date, Long shiftTemplateId) {
        return shiftAssignmentRepository.findSwappableAssignments(assignedUserId, date, shiftTemplateId);
    }

    @Override
    public List<ShiftAssignment> autoAssignShifts(List<Long> assignedUserIds, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement auto-assignment logic
        throw new BusinessLogicException("Auto-assignment functionality chÆ°a Ä‘Æ°á»£c implement");
    }

    @Override
    public List<ShiftAssignment> copyAssignments(LocalDate sourceStart, LocalDate sourceEnd, 
                                                LocalDate targetStart, List<Long> assignedUserIds) {
        // TODO: Implement copy assignments logic
        throw new BusinessLogicException("Copy assignments functionality chÆ°a Ä‘Æ°á»£c implement");
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentStatistics getAssignmentStatistics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> statusCounts = shiftAssignmentRepository.countByStatusInDateRange(startDate, endDate);
        
        long totalAssignments = 0;
        long scheduledAssignments = 0;
        long completedAssignments = 0;
        long cancelledAssignments = 0;

        for (Object[] row : statusCounts) {
            ShiftAssignment.AssignmentStatus status = (ShiftAssignment.AssignmentStatus) row[0];
            long count = ((Number) row[1]).longValue();
            
            totalAssignments += count;
            switch (status) {
                case SCHEDULED: scheduledAssignments = count; break;
                case COMPLETED: completedAssignments = count; break;
                case CANCELLED: cancelledAssignments = count; break;
                case NO_SHOW: break; // No specific handling needed
                case IN_PROGRESS: break; // No specific handling needed
            }
        }

        return new AssignmentStatistics(totalAssignments, scheduledAssignments, 
                                      completedAssignments, cancelledAssignments,
                                      BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAssignments(LocalDate startDate, LocalDate endDate, String format) {
        // TODO: Implement export functionality
        throw new BusinessLogicException("Export functionality chÆ°a Ä‘Æ°á»£c implement");
    }

    @Override
    public void sendShiftReminders(LocalDate date) {
        // TODO: Implement reminder notifications
        log.info("Sending shift reminders for date: {}", date);
    }

    @Override
    public void updateAssignmentStatus(Long id, ShiftAssignment.AssignmentStatus status) {
        int updated = shiftAssignmentRepository.updateStatus(id, status);
        if (updated == 0) {
            throw new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y assignment vá»›i ID: " + id);
        }
        log.info("ÄÃ£ cáº­p nháº­t status = {} cho assignment ID: {}", status, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findCurrentWeekAssignments(Long assignedUserId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        return shiftAssignmentRepository.findCurrentWeekAssignments(assignedUserId, weekStart, weekEnd);
    }
}
