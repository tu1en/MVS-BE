package com.classroomapp.classroombackend.service.hrmanagement.shift.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftAssignmentRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation cá»§a ShiftConflictDetectionService
 * Xá»­ lÃ½ logic phÃ¡t hiá»‡n xung Ä‘á»™t ca lÃ m viá»‡c vá»›i business rules phá»©c táº¡p
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShiftConflictDetectionServiceImpl implements ShiftConflictDetectionService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Value("${shift.management.min-rest-hours:8}")
    private int minRestHours;

    @Value("${shift.management.max-weekly-hours:40}")
    private int maxWeeklyHours;

    @Value("${shift.management.max-daily-hours:12}")
    private int maxDailyHours;

    @Override
    public ConflictCheckResult checkTimeConflicts(Long assignedUserId, LocalDate date,
                                                 LocalTime startTime, LocalTime endTime,
                                                 Long excludeAssignmentId) {
        log.debug("Kiá»ƒm tra xung Ä‘á»™t thá»i gian cho employee {} ngÃ y {} tá»« {} Ä‘áº¿n {}",
                assignedUserId, date, startTime, endTime);

        List<ShiftAssignment> conflicts = shiftAssignmentRepository.findConflictingAssignments(
                assignedUserId, date, startTime, endTime, excludeAssignmentId);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        for (ShiftAssignment conflict : conflicts) {
            ConflictDetail detail = new ConflictDetail(
                    ConflictType.TIME_OVERLAP,
                    String.format("Xung Ä‘á»™t vá»›i ca %s tá»« %s Ä‘áº¿n %s",
                            conflict.getShiftTemplate().getTemplateName(),
                            conflict.getPlannedStartTime(),
                            conflict.getPlannedEndTime()),
                    conflict,
                    ConflictSeverity.HIGH,
                    "Vui lÃ²ng chá»n thá»i gian khÃ¡c hoáº·c Ä‘iá»u chá»‰nh ca hiá»‡n táº¡i"
            );
            conflictDetails.add(detail);
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("PhÃ¡t hiá»‡n %d xung Ä‘á»™t thá»i gian", conflictDetails.size()) :
                "KhÃ´ng cÃ³ xung Ä‘á»™t thá»i gian";

        return new ConflictCheckResult(hasConflict, conflictDetails, summary,
                hasConflict ? ConflictSeverity.HIGH : ConflictSeverity.LOW);
    }

    @Override
    public ConflictCheckResult checkRestTimeViolations(Long assignedUserId, LocalDate date,
                                                      LocalTime startTime, LocalTime endTime,
                                                      Long excludeAssignmentId) {
        log.debug("Kiá»ƒm tra vi pháº¡m thá»i gian nghá»‰ cho employee {} ngÃ y {}", assignedUserId, date);

        LocalDate previousDate = date.minusDays(1);
        LocalDate nextDate = date.plusDays(1);

        List<ShiftAssignment> violations = shiftAssignmentRepository.findRestTimeViolations(
                assignedUserId, previousDate, nextDate, startTime, endTime, excludeAssignmentId);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        for (ShiftAssignment violation : violations) {
            long hoursBetween = calculateHoursBetween(violation, startTime, endTime, date);

            if (hoursBetween < minRestHours) {
                ConflictDetail detail = new ConflictDetail(
                        ConflictType.INSUFFICIENT_REST,
                        String.format("Chá»‰ cÃ³ %d giá» nghá»‰ vá»›i ca %s (tá»‘i thiá»ƒu %d giá»)",
                                hoursBetween,
                                violation.getShiftTemplate().getTemplateName(),
                                minRestHours),
                        violation,
                        hoursBetween < 4 ? ConflictSeverity.CRITICAL : ConflictSeverity.HIGH,
                        String.format("Cáº§n Ã­t nháº¥t %d giá» nghá»‰ giá»¯a cÃ¡c ca", minRestHours)
                );
                conflictDetails.add(detail);
            }
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("PhÃ¡t hiá»‡n %d vi pháº¡m thá»i gian nghá»‰", conflictDetails.size()) :
                "Thá»i gian nghá»‰ há»£p lá»‡";

        return new ConflictCheckResult(hasConflict, conflictDetails, summary,
                hasConflict ? ConflictSeverity.HIGH : ConflictSeverity.LOW);
    }

    @Override
    public ConflictCheckResult checkWeeklyHourLimits(Long assignedUserId, LocalDate date,
                                                    BigDecimal additionalHours) {
        log.debug("Kiá»ƒm tra giá»›i háº¡n giá» lÃ m viá»‡c hÃ ng tuáº§n cho employee {}", assignedUserId);

        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        BigDecimal currentWeeklyHours = getWeeklyWorkingHours(assignedUserId, weekStart);
        BigDecimal totalHours = currentWeeklyHours.add(additionalHours);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        if (totalHours.compareTo(BigDecimal.valueOf(maxWeeklyHours)) > 0) {
            ConflictDetail detail = new ConflictDetail(
                    ConflictType.WEEKLY_HOUR_LIMIT,
                    String.format("Tá»•ng giá» lÃ m viá»‡c trong tuáº§n sáº½ lÃ  %.2f giá» (giá»›i háº¡n %d giá»)",
                            totalHours.doubleValue(), maxWeeklyHours),
                    null,
                    ConflictSeverity.MEDIUM,
                    "CÃ¢n nháº¯c giáº£m giá» lÃ m viá»‡c hoáº·c phÃ¢n bá»• sang tuáº§n khÃ¡c"
            );
            conflictDetails.add(detail);
        }

        BigDecimal dailyHours = getDailyWorkingHours(assignedUserId, date).add(additionalHours);
        if (dailyHours.compareTo(BigDecimal.valueOf(maxDailyHours)) > 0) {
            ConflictDetail detail = new ConflictDetail(
                    ConflictType.WEEKLY_HOUR_LIMIT,
                    String.format("Tá»•ng giá» lÃ m viá»‡c trong ngÃ y sáº½ lÃ  %.2f giá» (giá»›i háº¡n %d giá»)",
                            dailyHours.doubleValue(), maxDailyHours),
                    null,
                    ConflictSeverity.HIGH,
                    "Giáº£m sá»‘ giá» lÃ m viá»‡c trong ngÃ y"
            );
            conflictDetails.add(detail);
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("Vi pháº¡m giá»›i háº¡n giá» lÃ m viá»‡c (%.2f/%.2f giá»)",
                        totalHours.doubleValue(), (double) maxWeeklyHours) :
                String.format("Giá» lÃ m viá»‡c há»£p lá»‡ (%.2f/%.2f giá»)",
                        totalHours.doubleValue(), (double) maxWeeklyHours);

        return new ConflictCheckResult(hasConflict, conflictDetails, summary,
                hasConflict ? ConflictSeverity.MEDIUM : ConflictSeverity.LOW);
    }

    @Override
    public ConflictCheckResult checkAllConflicts(Long assignedUserId, LocalDate date,
                                                LocalTime startTime, LocalTime endTime,
                                                BigDecimal hours, Long excludeAssignmentId) {
        log.debug("Kiá»ƒm tra táº¥t cáº£ xung Ä‘á»™t cho employee {} ngÃ y {}", assignedUserId, date);

        List<ConflictDetail> allConflicts = new ArrayList<>();

        ConflictCheckResult timeConflicts = checkTimeConflicts(
                assignedUserId, date, startTime, endTime, excludeAssignmentId);
        if (timeConflicts.hasConflict()) {
            allConflicts.addAll(timeConflicts.getConflicts());
        }

        ConflictCheckResult restViolations = checkRestTimeViolations(
                assignedUserId, date, startTime, endTime, excludeAssignmentId);
        if (restViolations.hasConflict()) {
            allConflicts.addAll(restViolations.getConflicts());
        }

        ConflictCheckResult hourLimits = checkWeeklyHourLimits(assignedUserId, date, hours);
        if (hourLimits.hasConflict()) {
            allConflicts.addAll(hourLimits.getConflicts());
        }

        ConflictSeverity maxSeverity = allConflicts.stream()
                .map(ConflictDetail::getSeverity)
                .max((s1, s2) -> s1.ordinal() - s2.ordinal())
                .orElse(ConflictSeverity.LOW);

        boolean hasConflict = !allConflicts.isEmpty();
        String summary = hasConflict ?
                String.format("PhÃ¡t hiá»‡n %d xung Ä‘á»™t (má»©c Ä‘á»™: %s)",
                        allConflicts.size(), maxSeverity.getDisplayName()) :
                "KhÃ´ng cÃ³ xung Ä‘á»™t";

        return new ConflictCheckResult(hasConflict, allConflicts, summary, maxSeverity);
    }

    @Override
    public ConflictCheckResult checkSwapConflicts(Long requesterId, Long targetUserId,
                                                 ShiftAssignment requesterAssignment,
                                                 ShiftAssignment targetAssignment) {
        log.debug("Kiá»ƒm tra xung Ä‘á»™t cho swap request giá»¯a {} vÃ  {}", requesterId, targetUserId);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        ConflictCheckResult requesterConflicts = checkAllConflicts(
                requesterId,
                targetAssignment.getAssignmentDate(),
                targetAssignment.getPlannedStartTime(),
                targetAssignment.getPlannedEndTime(),
                targetAssignment.getPlannedHours(),
                requesterAssignment.getId()
        );
        if (requesterConflicts.hasConflict()) {
            conflictDetails.addAll(requesterConflicts.getConflicts());
        }

        ConflictCheckResult targetConflicts = checkAllConflicts(
                targetUserId,
                requesterAssignment.getAssignmentDate(),
                requesterAssignment.getPlannedStartTime(),
                requesterAssignment.getPlannedEndTime(),
                requesterAssignment.getPlannedHours(),
                targetAssignment.getId()
        );
        if (targetConflicts.hasConflict()) {
            conflictDetails.addAll(targetConflicts.getConflicts());
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("Swap khÃ´ng thá»ƒ thá»±c hiá»‡n do %d xung Ä‘á»™t", conflictDetails.size()) :
                "Swap cÃ³ thá»ƒ thá»±c hiá»‡n";

        ConflictSeverity maxSeverity = conflictDetails.stream()
                .map(ConflictDetail::getSeverity)
                .max((s1, s2) -> s1.ordinal() - s2.ordinal())
                .orElse(ConflictSeverity.LOW);

        return new ConflictCheckResult(hasConflict, conflictDetails, summary, maxSeverity);
    }

    @Override
    public List<AvailableTimeSlot> findAvailableTimeSlots(Long assignedUserId, LocalDate date) {
        log.debug("TÃ¬m time slots available cho employee {} ngÃ y {}", assignedUserId, date);

        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository
                .findByAssignedUserIdAndAssignmentDateOrderByPlannedStartTimeAsc(assignedUserId, date);

        List<AvailableTimeSlot> availableSlots = new ArrayList<>();
        LocalTime currentTime = LocalTime.of(6, 0);
        LocalTime endOfDay = LocalTime.of(22, 0);

        for (ShiftAssignment assignment : existingAssignments) {
            if (currentTime.isBefore(assignment.getPlannedStartTime())) {
                LocalTime slotEnd = assignment.getPlannedStartTime().minusHours(minRestHours);
                if (slotEnd.isAfter(currentTime)) {
                    availableSlots.add(new AvailableTimeSlot(
                            currentTime, slotEnd,
                            calculateMaxHours(currentTime, slotEnd),
                            "Khoáº£ng trá»‘ng trÆ°á»›c ca " + assignment.getShiftTemplate().getTemplateName(),
                            false
                    ));
                }
            }
            currentTime = assignment.getPlannedEndTime().plusHours(minRestHours);
        }

        if (currentTime.isBefore(endOfDay)) {
            availableSlots.add(new AvailableTimeSlot(
                    currentTime, endOfDay,
                    calculateMaxHours(currentTime, endOfDay),
                    "Khoáº£ng trá»‘ng cuá»‘i ngÃ y",
                    true
            ));
        }

        return availableSlots;
    }

    @Override
    public boolean isEmployeeAvailable(Long assignedUserId, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        ConflictCheckResult result = checkTimeConflicts(assignedUserId, date, startTime, endTime, null);
        return !result.hasConflict();
    }

    @Override
    public BigDecimal getWeeklyWorkingHours(Long assignedUserId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        Object[] result = shiftAssignmentRepository.calculateWorkingHours(
                assignedUserId, weekStartDate, weekEndDate);

        if (result != null && result.length > 0 && result[0] != null) {
            return (BigDecimal) result[0];
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<ShiftAssignment> getConflictingAssignments(Long assignedUserId, LocalDate date,
                                                          LocalTime startTime, LocalTime endTime,
                                                          Long excludeAssignmentId) {
        return shiftAssignmentRepository.findConflictingAssignments(
                assignedUserId, date, startTime, endTime, excludeAssignmentId);
    }

    @Override
    public void validateAssignmentCreation(ShiftAssignment assignment) {
        if (assignment == null) {
            throw new BusinessLogicException("Assignment khÃ´ng Ä‘Æ°á»£c null");
        }

        ConflictCheckResult result = checkAllConflicts(
                assignment.getAssignedUser().getId(),
                assignment.getAssignmentDate(),
                assignment.getPlannedStartTime(),
                assignment.getPlannedEndTime(),
                assignment.getPlannedHours(),
                null
        );

        if (result.hasConflict()) {
            String errorMessage = "KhÃ´ng thá»ƒ táº¡o assignment do xung Ä‘á»™t: " + result.getSummary();
            log.warn("Validation failed: {}", errorMessage);
            throw new BusinessLogicException(errorMessage);
        }
    }

    @Override
    public List<AvailableTimeSlot> suggestAlternativeTimeSlots(Long assignedUserId, LocalDate date,
                                                              BigDecimal requiredHours) {
        List<AvailableTimeSlot> availableSlots = findAvailableTimeSlots(assignedUserId, date);

        return availableSlots.stream()
                .filter(slot -> slot.getMaxHours().compareTo(requiredHours) >= 0)
                .sorted((s1, s2) -> {
                    if (s1.isPreferred() != s2.isPreferred()) {
                        return s1.isPreferred() ? -1 : 1;
                    }
                    return s1.getStartTime().compareTo(s2.getStartTime());
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private long calculateHoursBetween(ShiftAssignment assignment, LocalTime startTime,
                                      LocalTime endTime, LocalDate date) {
        if (assignment.getAssignmentDate().isBefore(date)) {
            return java.time.Duration.between(assignment.getPlannedEndTime(), startTime).toHours();
        } else {
            return java.time.Duration.between(endTime, assignment.getPlannedStartTime()).toHours();
        }
    }

    private BigDecimal getDailyWorkingHours(Long assignedUserId, LocalDate date) {
        Object[] result = shiftAssignmentRepository.calculateWorkingHours(assignedUserId, date, date);
        if (result != null && result.length > 0 && result[0] != null) {
            return (BigDecimal) result[0];
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateMaxHours(LocalTime startTime, LocalTime endTime) {
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
