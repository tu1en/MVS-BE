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
 * Implementation của ShiftConflictDetectionService
 * Xử lý logic phát hiện xung đột ca làm việc với business rules phức tạp
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
    public ConflictCheckResult checkTimeConflicts(Long employeeId, LocalDate date,
                                                 LocalTime startTime, LocalTime endTime,
                                                 Long excludeAssignmentId) {
        log.debug("Kiểm tra xung đột thời gian cho employee {} ngày {} từ {} đến {}",
                employeeId, date, startTime, endTime);

        List<ShiftAssignment> conflicts = shiftAssignmentRepository.findConflictingAssignments(
                employeeId, date, startTime, endTime, excludeAssignmentId);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        for (ShiftAssignment conflict : conflicts) {
            ConflictDetail detail = new ConflictDetail(
                    ConflictType.TIME_OVERLAP,
                    String.format("Xung đột với ca %s từ %s đến %s",
                            conflict.getShiftTemplate().getTemplateName(),
                            conflict.getPlannedStartTime(),
                            conflict.getPlannedEndTime()),
                    conflict,
                    ConflictSeverity.HIGH,
                    "Vui lòng chọn thời gian khác hoặc điều chỉnh ca hiện tại"
            );
            conflictDetails.add(detail);
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("Phát hiện %d xung đột thời gian", conflictDetails.size()) :
                "Không có xung đột thời gian";

        return new ConflictCheckResult(hasConflict, conflictDetails, summary,
                hasConflict ? ConflictSeverity.HIGH : ConflictSeverity.LOW);
    }

    @Override
    public ConflictCheckResult checkRestTimeViolations(Long employeeId, LocalDate date,
                                                      LocalTime startTime, LocalTime endTime,
                                                      Long excludeAssignmentId) {
        log.debug("Kiểm tra vi phạm thời gian nghỉ cho employee {} ngày {}", employeeId, date);

        LocalDate previousDate = date.minusDays(1);
        LocalDate nextDate = date.plusDays(1);

        List<ShiftAssignment> violations = shiftAssignmentRepository.findRestTimeViolations(
                employeeId, previousDate, nextDate, startTime, endTime, excludeAssignmentId);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        for (ShiftAssignment violation : violations) {
            long hoursBetween = calculateHoursBetween(violation, startTime, endTime, date);

            if (hoursBetween < minRestHours) {
                ConflictDetail detail = new ConflictDetail(
                        ConflictType.INSUFFICIENT_REST,
                        String.format("Chỉ có %d giờ nghỉ với ca %s (tối thiểu %d giờ)",
                                hoursBetween,
                                violation.getShiftTemplate().getTemplateName(),
                                minRestHours),
                        violation,
                        hoursBetween < 4 ? ConflictSeverity.CRITICAL : ConflictSeverity.HIGH,
                        String.format("Cần ít nhất %d giờ nghỉ giữa các ca", minRestHours)
                );
                conflictDetails.add(detail);
            }
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("Phát hiện %d vi phạm thời gian nghỉ", conflictDetails.size()) :
                "Thời gian nghỉ hợp lệ";

        return new ConflictCheckResult(hasConflict, conflictDetails, summary,
                hasConflict ? ConflictSeverity.HIGH : ConflictSeverity.LOW);
    }

    @Override
    public ConflictCheckResult checkWeeklyHourLimits(Long employeeId, LocalDate date,
                                                    BigDecimal additionalHours) {
        log.debug("Kiểm tra giới hạn giờ làm việc hàng tuần cho employee {}", employeeId);

        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        BigDecimal currentWeeklyHours = getWeeklyWorkingHours(employeeId, weekStart);
        BigDecimal totalHours = currentWeeklyHours.add(additionalHours);

        List<ConflictDetail> conflictDetails = new ArrayList<>();

        if (totalHours.compareTo(BigDecimal.valueOf(maxWeeklyHours)) > 0) {
            ConflictDetail detail = new ConflictDetail(
                    ConflictType.WEEKLY_HOUR_LIMIT,
                    String.format("Tổng giờ làm việc trong tuần sẽ là %.2f giờ (giới hạn %d giờ)",
                            totalHours.doubleValue(), maxWeeklyHours),
                    null,
                    ConflictSeverity.MEDIUM,
                    "Cân nhắc giảm giờ làm việc hoặc phân bổ sang tuần khác"
            );
            conflictDetails.add(detail);
        }

        BigDecimal dailyHours = getDailyWorkingHours(employeeId, date).add(additionalHours);
        if (dailyHours.compareTo(BigDecimal.valueOf(maxDailyHours)) > 0) {
            ConflictDetail detail = new ConflictDetail(
                    ConflictType.WEEKLY_HOUR_LIMIT,
                    String.format("Tổng giờ làm việc trong ngày sẽ là %.2f giờ (giới hạn %d giờ)",
                            dailyHours.doubleValue(), maxDailyHours),
                    null,
                    ConflictSeverity.HIGH,
                    "Giảm số giờ làm việc trong ngày"
            );
            conflictDetails.add(detail);
        }

        boolean hasConflict = !conflictDetails.isEmpty();
        String summary = hasConflict ?
                String.format("Vi phạm giới hạn giờ làm việc (%.2f/%.2f giờ)",
                        totalHours.doubleValue(), (double) maxWeeklyHours) :
                String.format("Giờ làm việc hợp lệ (%.2f/%.2f giờ)",
                        totalHours.doubleValue(), (double) maxWeeklyHours);

        return new ConflictCheckResult(hasConflict, conflictDetails, summary,
                hasConflict ? ConflictSeverity.MEDIUM : ConflictSeverity.LOW);
    }

    @Override
    public ConflictCheckResult checkAllConflicts(Long employeeId, LocalDate date,
                                                LocalTime startTime, LocalTime endTime,
                                                BigDecimal hours, Long excludeAssignmentId) {
        log.debug("Kiểm tra tất cả xung đột cho employee {} ngày {}", employeeId, date);

        List<ConflictDetail> allConflicts = new ArrayList<>();

        ConflictCheckResult timeConflicts = checkTimeConflicts(
                employeeId, date, startTime, endTime, excludeAssignmentId);
        if (timeConflicts.hasConflict()) {
            allConflicts.addAll(timeConflicts.getConflicts());
        }

        ConflictCheckResult restViolations = checkRestTimeViolations(
                employeeId, date, startTime, endTime, excludeAssignmentId);
        if (restViolations.hasConflict()) {
            allConflicts.addAll(restViolations.getConflicts());
        }

        ConflictCheckResult hourLimits = checkWeeklyHourLimits(employeeId, date, hours);
        if (hourLimits.hasConflict()) {
            allConflicts.addAll(hourLimits.getConflicts());
        }

        ConflictSeverity maxSeverity = allConflicts.stream()
                .map(ConflictDetail::getSeverity)
                .max((s1, s2) -> s1.ordinal() - s2.ordinal())
                .orElse(ConflictSeverity.LOW);

        boolean hasConflict = !allConflicts.isEmpty();
        String summary = hasConflict ?
                String.format("Phát hiện %d xung đột (mức độ: %s)",
                        allConflicts.size(), maxSeverity.getDisplayName()) :
                "Không có xung đột";

        return new ConflictCheckResult(hasConflict, allConflicts, summary, maxSeverity);
    }

    @Override
    public ConflictCheckResult checkSwapConflicts(Long requesterId, Long targetEmployeeId,
                                                 ShiftAssignment requesterAssignment,
                                                 ShiftAssignment targetAssignment) {
        log.debug("Kiểm tra xung đột cho swap request giữa {} và {}", requesterId, targetEmployeeId);

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
                targetEmployeeId,
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
                String.format("Swap không thể thực hiện do %d xung đột", conflictDetails.size()) :
                "Swap có thể thực hiện";

        ConflictSeverity maxSeverity = conflictDetails.stream()
                .map(ConflictDetail::getSeverity)
                .max((s1, s2) -> s1.ordinal() - s2.ordinal())
                .orElse(ConflictSeverity.LOW);

        return new ConflictCheckResult(hasConflict, conflictDetails, summary, maxSeverity);
    }

    @Override
    public List<AvailableTimeSlot> findAvailableTimeSlots(Long employeeId, LocalDate date) {
        log.debug("Tìm time slots available cho employee {} ngày {}", employeeId, date);

        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository
                .findByEmployeeIdAndAssignmentDateOrderByPlannedStartTimeAsc(employeeId, date);

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
                            "Khoảng trống trước ca " + assignment.getShiftTemplate().getTemplateName(),
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
                    "Khoảng trống cuối ngày",
                    true
            ));
        }

        return availableSlots;
    }

    @Override
    public boolean isEmployeeAvailable(Long employeeId, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        ConflictCheckResult result = checkTimeConflicts(employeeId, date, startTime, endTime, null);
        return !result.hasConflict();
    }

    @Override
    public BigDecimal getWeeklyWorkingHours(Long employeeId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        Object[] result = shiftAssignmentRepository.calculateWorkingHours(
                employeeId, weekStartDate, weekEndDate);

        if (result != null && result.length > 0 && result[0] != null) {
            return (BigDecimal) result[0];
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<ShiftAssignment> getConflictingAssignments(Long employeeId, LocalDate date,
                                                          LocalTime startTime, LocalTime endTime,
                                                          Long excludeAssignmentId) {
        return shiftAssignmentRepository.findConflictingAssignments(
                employeeId, date, startTime, endTime, excludeAssignmentId);
    }

    @Override
    public void validateAssignmentCreation(ShiftAssignment assignment) {
        if (assignment == null) {
            throw new BusinessLogicException("Assignment không được null");
        }

        ConflictCheckResult result = checkAllConflicts(
                assignment.getEmployee().getId(),
                assignment.getAssignmentDate(),
                assignment.getPlannedStartTime(),
                assignment.getPlannedEndTime(),
                assignment.getPlannedHours(),
                null
        );

        if (result.hasConflict()) {
            String errorMessage = "Không thể tạo assignment do xung đột: " + result.getSummary();
            log.warn("Validation failed: {}", errorMessage);
            throw new BusinessLogicException(errorMessage);
        }
    }

    @Override
    public List<AvailableTimeSlot> suggestAlternativeTimeSlots(Long employeeId, LocalDate date,
                                                              BigDecimal requiredHours) {
        List<AvailableTimeSlot> availableSlots = findAvailableTimeSlots(employeeId, date);

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

    private BigDecimal getDailyWorkingHours(Long employeeId, LocalDate date) {
        Object[] result = shiftAssignmentRepository.calculateWorkingHours(employeeId, date, date);
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
