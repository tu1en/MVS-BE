package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.hrmanagement.UserShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.WorkShift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViolationDetectionServiceImpl implements ViolationDetectionService {

    private final AttendanceViolationRepository violationRepository;
    private final StaffAttendanceLogRepository attendanceLogRepository;
    private final UserShiftAssignmentRepository shiftAssignmentRepository;
    private final UserRepository userRepository;

    private ViolationDetectionConfig config = new ViolationDetectionConfig();

    @Override
    public List<AttendanceViolation> detectViolationsForDate(LocalDate date) {
        log.info("Detecting violations for date: {}", date);
        List<AttendanceViolation> allViolations = new ArrayList<>();

        allViolations.addAll(detectLateArrivals(date, config.getLateArrivalToleranceMinutes()));
        allViolations.addAll(detectEarlyDepartures(date, config.getEarlyDepartureToleranceMinutes()));
        allViolations.addAll(detectMissingCheckIns(date));
        allViolations.addAll(detectMissingCheckOuts(date));
        allViolations.addAll(detectAbsentWithoutLeave(date));

        return allViolations;
    }

    @Override
    public List<AttendanceViolation> detectViolationsForUser(Long userId, LocalDate date) {
        List<AttendanceViolation> violations = new ArrayList<>();
        Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository.findUserAssignmentForDate(userId, date);

        if (assignmentOpt.isEmpty()) return violations;

        UserShiftAssignment assignment = assignmentOpt.get();
        WorkShift shift = assignment.getWorkShift();
        Optional<StaffAttendanceLog> logOpt = attendanceLogRepository.findByUserIdAndAttendanceDate(userId, date);

        if (logOpt.isEmpty()) {
            violations.add(createAbsentViolation(assignment.getUser(), assignment, date));
        } else {
            StaffAttendanceLog log = logOpt.get();

            if (log.getCheckInTime() != null && log.isLate(shift.getStartTime(), config.getLateArrivalToleranceMinutes())) {
                violations.add(createLateArrivalViolation(assignment.getUser(), assignment, log, date));
            }

            if (log.getCheckOutTime() != null && log.isEarly(shift.getEndTime(), config.getEarlyDepartureToleranceMinutes())) {
                violations.add(createEarlyDepartureViolation(assignment.getUser(), assignment, log, date));
            }

            if (log.getCheckInTime() == null) {
                violations.add(createMissingCheckInViolation(assignment.getUser(), assignment, log, date));
            }

            if (log.getCheckOutTime() == null) {
                violations.add(createMissingCheckOutViolation(assignment.getUser(), assignment, log, date));
            }
        }

        return violations;
    }

    @Override
    public List<AttendanceViolation> detectLateArrivals(LocalDate date, int toleranceMinutes) {
        List<AttendanceViolation> violations = new ArrayList<>();
        List<StaffAttendanceLog> lateLogs = attendanceLogRepository.findLateArrivals(date, toleranceMinutes);

        for (StaffAttendanceLog log : lateLogs) {
            if (violationExists(log.getUser().getId(), date, AttendanceViolation.ViolationType.LATE_ARRIVAL)) continue;
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository.findUserAssignmentForDate(log.getUser().getId(), date);
            assignmentOpt.ifPresent(a -> violations.add(createLateArrivalViolation(log.getUser(), a, log, date)));
        }

        return violations;
    }

    @Override
    public List<AttendanceViolation> detectEarlyDepartures(LocalDate date, int toleranceMinutes) {
        List<AttendanceViolation> violations = new ArrayList<>();
        List<StaffAttendanceLog> earlyLogs = attendanceLogRepository.findEarlyDepartures(date, toleranceMinutes);

        for (StaffAttendanceLog log : earlyLogs) {
            if (violationExists(log.getUser().getId(), date, AttendanceViolation.ViolationType.EARLY_DEPARTURE)) continue;
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository.findUserAssignmentForDate(log.getUser().getId(), date);
            assignmentOpt.ifPresent(a -> violations.add(createEarlyDepartureViolation(log.getUser(), a, log, date)));
        }

        return violations;
    }

    @Override
    public List<AttendanceViolation> detectMissingCheckIns(LocalDate date) {
        List<AttendanceViolation> violations = new ArrayList<>();
        List<Long> usersWithoutCheckIn = attendanceLogRepository.findUsersWithoutCheckIn(date);

        for (Long userId : usersWithoutCheckIn) {
            if (violationExists(userId, date, AttendanceViolation.ViolationType.MISSING_CHECK_IN)) continue;
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository.findUserAssignmentForDate(userId, date);
            assignmentOpt.ifPresent(a -> {
                Optional<StaffAttendanceLog> logOpt = attendanceLogRepository.findByUserIdAndAttendanceDate(userId, date);
                violations.add(createMissingCheckInViolation(a.getUser(), a, logOpt.orElse(null), date));
            });
        }

        return violations;
    }

    @Override
    public List<AttendanceViolation> detectMissingCheckOuts(LocalDate date) {
        List<AttendanceViolation> violations = new ArrayList<>();
        List<Long> usersWithoutCheckOut = attendanceLogRepository.findUsersWithoutCheckOut(date);

        for (Long userId : usersWithoutCheckOut) {
            if (violationExists(userId, date, AttendanceViolation.ViolationType.MISSING_CHECK_OUT)) continue;
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository.findUserAssignmentForDate(userId, date);
            if (assignmentOpt.isPresent()) {
                Optional<StaffAttendanceLog> logOpt = attendanceLogRepository.findByUserIdAndAttendanceDate(userId, date);
                logOpt.ifPresent(log -> violations.add(createMissingCheckOutViolation(assignmentOpt.get().getUser(), assignmentOpt.get(), log, date)));
            }
        }

        return violations;
    }

    @Override
    public List<AttendanceViolation> detectAbsentWithoutLeave(LocalDate date) {
        List<AttendanceViolation> violations = new ArrayList<>();
        List<UserShiftAssignment> assignments = shiftAssignmentRepository.findActiveAssignmentsForDate(date);

        for (UserShiftAssignment assignment : assignments) {
            Long userId = assignment.getUser().getId();
            if (violationExists(userId, date, AttendanceViolation.ViolationType.ABSENT_WITHOUT_LEAVE)) continue;

            if (!attendanceLogRepository.existsByUserIdAndAttendanceDate(userId, date)) {
                violations.add(createAbsentViolation(assignment.getUser(), assignment, date));
            }
        }

        return violations;
    }

    @Override
    public ViolationDetectionSummary runDailyDetection(LocalDate date) {
        ViolationDetectionSummary summary = new ViolationDetectionSummary(date);
        List<AttendanceViolation> violations = detectViolationsForDate(date);
        for (AttendanceViolation violation : violations) {
            try {
                violationRepository.save(violation);
                updateSummaryForViolation(summary, violation);
            } catch (Exception e) {
                summary.incrementDuplicatesSkipped();
            }
        }
        return summary;
    }

    @Override
    public ViolationDetectionSummary reprocessViolations(LocalDate startDate, LocalDate endDate) {
        ViolationDetectionSummary summary = new ViolationDetectionSummary(startDate);
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            ViolationDetectionSummary daily = runDailyDetection(current);
            summary.aggregate(daily);
            current = current.plusDays(1);
        }
        return summary;
    }

    @Override
    public ViolationDetectionConfig getDetectionConfig() {
        return config;
    }

    @Override
    public void updateDetectionConfig(ViolationDetectionConfig config) {
        this.config = config;
    }

    @Override
    public boolean violationExists(Long userId, LocalDate date, AttendanceViolation.ViolationType type) {
        return violationRepository.existsByUserIdAndViolationDateAndViolationType(userId, date, type);
    }

    @Override
    public AttendanceViolation.ViolationSeverity calculateSeverity(AttendanceViolation.ViolationType violationType, int deviationMinutes) {
        if (violationType == null) {
            return AttendanceViolation.ViolationSeverity.MINOR;
        }

        switch (violationType) {
            case LATE_ARRIVAL:
                if (deviationMinutes <= 5) {
                    return AttendanceViolation.ViolationSeverity.MINOR;
                } else if (deviationMinutes <= 15) {
                    return AttendanceViolation.ViolationSeverity.MODERATE;
                } else {
                    return AttendanceViolation.ViolationSeverity.MAJOR;
                }
            case EARLY_DEPARTURE:
                return deviationMinutes > 10 ? AttendanceViolation.ViolationSeverity.MODERATE : AttendanceViolation.ViolationSeverity.MINOR;
            case ABSENT_WITHOUT_LEAVE:
                return AttendanceViolation.ViolationSeverity.CRITICAL;
            case MISSING_CHECK_IN:
            case MISSING_CHECK_OUT:
                return AttendanceViolation.ViolationSeverity.MODERATE;
            default:
                return AttendanceViolation.ViolationSeverity.MINOR;
        }
    }

    /** ✅ NEW METHODS REQUIRED BY INTERFACE */
    @Override
    public List<com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto> getOverdueViolations(int daysOverdue) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOverdue);
        List<AttendanceViolation> violations = violationRepository.findOverdueViolations(cutoffDate);
        return violations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void escalateViolation(Long violationId, long escalatedBy, String escalationReason) {
        AttendanceViolation violation = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Violation not found with id: " + violationId));
        violation.setEscalationLevel(escalatedBy);
        violation.setEscalationReason(escalationReason);
        violation.setStatus(AttendanceViolation.ViolationStatus.ESCALATED);
        violationRepository.save(violation);
    }

    // === Helper methods === //
    private AttendanceViolation createLateArrivalViolation(User user, UserShiftAssignment assignment, StaffAttendanceLog log, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();
        int deviation = calculateDeviationMinutes(shift.getStartTime(), log.getCheckInTime());
        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setViolationType(AttendanceViolation.ViolationType.LATE_ARRIVAL);
        violation.setViolationDate(date);
        violation.setDeviationMinutes(deviation);
        return violation;
    }

    private AttendanceViolation createEarlyDepartureViolation(User user, UserShiftAssignment assignment, StaffAttendanceLog log, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();
        int deviation = calculateDeviationMinutes(log.getCheckOutTime(), shift.getEndTime());
        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setViolationType(AttendanceViolation.ViolationType.EARLY_DEPARTURE);
        violation.setViolationDate(date);
        violation.setDeviationMinutes(deviation);
        return violation;
    }

    private AttendanceViolation createMissingCheckInViolation(User user, UserShiftAssignment assignment, StaffAttendanceLog log, LocalDate date) {
        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setViolationType(AttendanceViolation.ViolationType.MISSING_CHECK_IN);
        violation.setViolationDate(date);
        return violation;
    }

    private AttendanceViolation createMissingCheckOutViolation(User user, UserShiftAssignment assignment, StaffAttendanceLog log, LocalDate date) {
        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setViolationType(AttendanceViolation.ViolationType.MISSING_CHECK_OUT);
        violation.setViolationDate(date);
        return violation;
    }

    private AttendanceViolation createAbsentViolation(User user, UserShiftAssignment assignment, LocalDate date) {
        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setViolationType(AttendanceViolation.ViolationType.ABSENT_WITHOUT_LEAVE);
        violation.setViolationDate(date);
        return violation;
    }

    private int calculateDeviationMinutes(LocalTime expected, LocalTime actual) {
        if (expected == null || actual == null) return 0;
        return Math.abs(expected.toSecondOfDay() - actual.toSecondOfDay()) / 60;
    }

    private void updateSummaryForViolation(ViolationDetectionSummary summary, AttendanceViolation violation) {
        switch (violation.getViolationType()) {
            case LATE_ARRIVAL: summary.incrementLateArrivals(); break;
            case EARLY_DEPARTURE: summary.incrementEarlyDepartures(); break;
            case MISSING_CHECK_IN: summary.incrementMissingCheckIns(); break;
            case MISSING_CHECK_OUT: summary.incrementMissingCheckOuts(); break;
            case ABSENT_WITHOUT_LEAVE: summary.incrementAbsentWithoutLeave(); break;
        }
    }

    private com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto convertToDto(AttendanceViolation entity) {
        com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto dto = 
            new com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto();
        
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setUserFullName(entity.getUser() != null ? entity.getUser().getFullName() : "Unknown");
        dto.setUserEmail(entity.getUser() != null ? entity.getUser().getEmail() : null);
        dto.setUserDepartment(entity.getUser() != null ? entity.getUser().getDepartment() : null);
        
        dto.setViolationDate(entity.getViolationDate());
        dto.setViolationType(entity.getViolationType());
        dto.setSeverity(entity.getSeverity());
        dto.setExpectedTime(entity.getExpectedTime());
        dto.setActualTime(entity.getActualTime());
        dto.setDeviationMinutes(entity.getDeviationMinutes());
        dto.setSystemDescription(entity.getSystemDescription());
        dto.setStatus(entity.getStatus());
        dto.setAutoDetected(entity.getAutoDetected());
        dto.setDetectionTime(entity.getDetectionTime());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    @Override
    public String generateSystemDescription(AttendanceViolation.ViolationType type,
                                          Integer deviationMinutes,
                                          LocalTime expectedTime,
                                          LocalTime actualTime) {
        StringBuilder description = new StringBuilder("System detected: ");
        if (type != null) {
            String formattedType = type.name().replace("_", " ").toLowerCase();
            formattedType = Arrays.stream(formattedType.split("\\s+"))
                    .filter(word -> !word.isEmpty())
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
            description.append(formattedType);
        }
        
        if (deviationMinutes != null) {
            description.append(" - Deviation: ").append(deviationMinutes).append(" minutes");
        }
        if (expectedTime != null && actualTime != null) {
            description.append(" (Expected: ").append(expectedTime)
                       .append(", Actual: ").append(actualTime).append(")");
        } else if (expectedTime != null) {
            description.append(" (Expected: ").append(expectedTime).append(")");
        }
        
        return description.toString();
    }
}
