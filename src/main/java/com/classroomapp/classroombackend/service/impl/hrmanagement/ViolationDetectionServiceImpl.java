package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

/**
 * Implementation of ViolationDetectionService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViolationDetectionServiceImpl implements ViolationDetectionService {
    
    private final AttendanceViolationRepository violationRepository;
    private final StaffAttendanceLogRepository attendanceLogRepository;
    private final UserShiftAssignmentRepository shiftAssignmentRepository;
    private final UserRepository userRepository;
    
    // Default configuration - in production this should be configurable
    private ViolationDetectionConfig config = new ViolationDetectionConfig();
    
    @Override
    public List<AttendanceViolation> detectViolationsForDate(LocalDate date) {
        log.info("Detecting violations for date: {}", date);
        
        List<AttendanceViolation> allViolations = new ArrayList<>();
        
        // Detect different types of violations
        allViolations.addAll(detectLateArrivals(date, config.getLateArrivalToleranceMinutes()));
        allViolations.addAll(detectEarlyDepartures(date, config.getEarlyDepartureToleranceMinutes()));
        allViolations.addAll(detectMissingCheckIns(date));
        allViolations.addAll(detectMissingCheckOuts(date));
        allViolations.addAll(detectAbsentWithoutLeave(date));
        
        log.info("Detected {} violations for date: {}", allViolations.size(), date);
        return allViolations;
    }
    
    @Override
    public List<AttendanceViolation> detectViolationsForUser(Long userId, LocalDate date) {
        log.info("Detecting violations for user {} on date: {}", userId, date);
        
        List<AttendanceViolation> violations = new ArrayList<>();
        
        // Get user's shift assignment for the date
        Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository
            .findUserAssignmentForDate(userId, date);
        
        if (assignmentOpt.isEmpty()) {
            log.debug("No shift assignment found for user {} on date {}", userId, date);
            return violations;
        }
        
        UserShiftAssignment assignment = assignmentOpt.get();
        WorkShift shift = assignment.getWorkShift();
        
        // Get attendance log for the date
        Optional<StaffAttendanceLog> logOpt = attendanceLogRepository
            .findByUserIdAndAttendanceDate(userId, date);
        
        if (logOpt.isEmpty()) {
            // No attendance log - absent without leave
            violations.add(createAbsentViolation(assignment.getUser(), assignment, date));
        } else {
            StaffAttendanceLog log = logOpt.get();
            
            // Check for late arrival
            if (log.getCheckInTime() != null && 
                log.isLate(shift.getStartTime(), config.getLateArrivalToleranceMinutes())) {
                violations.add(createLateArrivalViolation(assignment.getUser(), assignment, log, date));
            }
            
            // Check for early departure
            if (log.getCheckOutTime() != null && 
                log.isEarly(shift.getEndTime(), config.getEarlyDepartureToleranceMinutes())) {
                violations.add(createEarlyDepartureViolation(assignment.getUser(), assignment, log, date));
            }
            
            // Check for missing check-in
            if (log.getCheckInTime() == null) {
                violations.add(createMissingCheckInViolation(assignment.getUser(), assignment, log, date));
            }
            
            // Check for missing check-out
            if (log.getCheckOutTime() == null) {
                violations.add(createMissingCheckOutViolation(assignment.getUser(), assignment, log, date));
            }
        }
        
        // Filter out duplicates
        violations.removeIf(violation -> 
            violationExists(violation.getUser().getId(), violation.getViolationDate(), violation.getViolationType()));
        
        log.info("Detected {} violations for user {} on date: {}", violations.size(), userId, date);
        return violations;
    }
    
    @Override
    public List<AttendanceViolation> detectLateArrivals(LocalDate date, int toleranceMinutes) {
        log.debug("Detecting late arrivals for date: {} with tolerance: {} minutes", date, toleranceMinutes);
        
        List<AttendanceViolation> violations = new ArrayList<>();
        List<StaffAttendanceLog> lateLogs = attendanceLogRepository.findLateArrivals(date, toleranceMinutes);
        
        for (StaffAttendanceLog log : lateLogs) {
            // Skip if violation already exists
            if (violationExists(log.getUser().getId(), date, AttendanceViolation.ViolationType.LATE_ARRIVAL)) {
                continue;
            }
            
            // Get shift assignment
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository
                .findUserAssignmentForDate(log.getUser().getId(), date);
            
            if (assignmentOpt.isPresent()) {
                violations.add(createLateArrivalViolation(log.getUser(), assignmentOpt.get(), log, date));
            }
        }
        
        log.debug("Detected {} late arrival violations", violations.size());
        return violations;
    }
    
    @Override
    public List<AttendanceViolation> detectEarlyDepartures(LocalDate date, int toleranceMinutes) {
        log.debug("Detecting early departures for date: {} with tolerance: {} minutes", date, toleranceMinutes);
        
        List<AttendanceViolation> violations = new ArrayList<>();
        List<StaffAttendanceLog> earlyLogs = attendanceLogRepository.findEarlyDepartures(date, toleranceMinutes);
        
        for (StaffAttendanceLog log : earlyLogs) {
            // Skip if violation already exists
            if (violationExists(log.getUser().getId(), date, AttendanceViolation.ViolationType.EARLY_DEPARTURE)) {
                continue;
            }
            
            // Get shift assignment
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository
                .findUserAssignmentForDate(log.getUser().getId(), date);
            
            if (assignmentOpt.isPresent()) {
                violations.add(createEarlyDepartureViolation(log.getUser(), assignmentOpt.get(), log, date));
            }
        }
        
        log.debug("Detected {} early departure violations", violations.size());
        return violations;
    }
    
    @Override
    public List<AttendanceViolation> detectMissingCheckIns(LocalDate date) {
        log.debug("Detecting missing check-ins for date: {}", date);
        
        List<AttendanceViolation> violations = new ArrayList<>();
        List<Long> usersWithoutCheckIn = attendanceLogRepository.findUsersWithoutCheckIn(date);
        
        for (Long userId : usersWithoutCheckIn) {
            // Skip if violation already exists
            if (violationExists(userId, date, AttendanceViolation.ViolationType.MISSING_CHECK_IN)) {
                continue;
            }
            
            // Get shift assignment
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository
                .findUserAssignmentForDate(userId, date);
            
            if (assignmentOpt.isPresent()) {
                User user = assignmentOpt.get().getUser();
                
                // Get attendance log (might have check-out but no check-in)
                Optional<StaffAttendanceLog> logOpt = attendanceLogRepository
                    .findByUserIdAndAttendanceDate(userId, date);
                
                violations.add(createMissingCheckInViolation(user, assignmentOpt.get(), 
                    logOpt.orElse(null), date));
            }
        }
        
        log.debug("Detected {} missing check-in violations", violations.size());
        return violations;
    }
    
    @Override
    public List<AttendanceViolation> detectMissingCheckOuts(LocalDate date) {
        log.debug("Detecting missing check-outs for date: {}", date);
        
        List<AttendanceViolation> violations = new ArrayList<>();
        List<Long> usersWithoutCheckOut = attendanceLogRepository.findUsersWithoutCheckOut(date);
        
        for (Long userId : usersWithoutCheckOut) {
            // Skip if violation already exists
            if (violationExists(userId, date, AttendanceViolation.ViolationType.MISSING_CHECK_OUT)) {
                continue;
            }
            
            // Get shift assignment
            Optional<UserShiftAssignment> assignmentOpt = shiftAssignmentRepository
                .findUserAssignmentForDate(userId, date);
            
            if (assignmentOpt.isPresent()) {
                User user = assignmentOpt.get().getUser();
                
                // Get attendance log
                Optional<StaffAttendanceLog> logOpt = attendanceLogRepository
                    .findByUserIdAndAttendanceDate(userId, date);
                
                if (logOpt.isPresent()) {
                    violations.add(createMissingCheckOutViolation(user, assignmentOpt.get(), 
                        logOpt.get(), date));
                }
            }
        }
        
        log.debug("Detected {} missing check-out violations", violations.size());
        return violations;
    }
    
    @Override
    public List<AttendanceViolation> detectAbsentWithoutLeave(LocalDate date) {
        log.debug("Detecting absent without leave for date: {}", date);
        
        List<AttendanceViolation> violations = new ArrayList<>();
        
        // Get all active shift assignments for the date
        List<UserShiftAssignment> assignments = shiftAssignmentRepository.findActiveAssignmentsForDate(date);
        
        for (UserShiftAssignment assignment : assignments) {
            Long userId = assignment.getUser().getId();
            
            // Skip if violation already exists
            if (violationExists(userId, date, AttendanceViolation.ViolationType.ABSENT_WITHOUT_LEAVE)) {
                continue;
            }
            
            // Check if user has any attendance record for the date
            boolean hasAttendanceRecord = attendanceLogRepository
                .existsByUserIdAndAttendanceDate(userId, date);
            
            if (!hasAttendanceRecord) {
                violations.add(createAbsentViolation(assignment.getUser(), assignment, date));
            }
        }
        
        log.debug("Detected {} absent without leave violations", violations.size());
        return violations;
    }
    
    @Override
    public ViolationDetectionSummary runDailyDetection(LocalDate date) {
        log.info("Running daily violation detection for date: {}", date);
        
        long startTime = System.currentTimeMillis();
        ViolationDetectionSummary summary = new ViolationDetectionSummary(date);
        
        try {
            // Detect all types of violations
            List<AttendanceViolation> allViolations = detectViolationsForDate(date);
            
            // Save violations and update summary
            for (AttendanceViolation violation : allViolations) {
                try {
                    violationRepository.save(violation);
                    updateSummaryForViolation(summary, violation);
                } catch (Exception e) {
                    log.error("Error saving violation: {}", e.getMessage());
                    summary.incrementDuplicatesSkipped();
                }
            }
            
            summary.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            log.info("Daily violation detection completed: {}", summary);
            return summary;
            
        } catch (Exception e) {
            log.error("Error during daily violation detection", e);
            summary.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return summary;
        }
    }

    @Override
    public ViolationDetectionSummary reprocessViolations(LocalDate startDate, LocalDate endDate) {
        log.info("Reprocessing violations from {} to {}", startDate, endDate);

        long startTime = System.currentTimeMillis();
        ViolationDetectionSummary summary = new ViolationDetectionSummary(startDate);

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            ViolationDetectionSummary dailySummary = runDailyDetection(currentDate);

            // Aggregate summaries
            summary.setTotalViolationsDetected(summary.getTotalViolationsDetected() + dailySummary.getTotalViolationsDetected());
            summary.setLateArrivals(summary.getLateArrivals() + dailySummary.getLateArrivals());
            summary.setEarlyDepartures(summary.getEarlyDepartures() + dailySummary.getEarlyDepartures());
            summary.setMissingCheckIns(summary.getMissingCheckIns() + dailySummary.getMissingCheckIns());
            summary.setMissingCheckOuts(summary.getMissingCheckOuts() + dailySummary.getMissingCheckOuts());
            summary.setAbsentWithoutLeave(summary.getAbsentWithoutLeave() + dailySummary.getAbsentWithoutLeave());
            summary.setDuplicatesSkipped(summary.getDuplicatesSkipped() + dailySummary.getDuplicatesSkipped());

            currentDate = currentDate.plusDays(1);
        }

        summary.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        log.info("Reprocessing completed: {}", summary);

        return summary;
    }

    @Override
    public ViolationDetectionConfig getDetectionConfig() {
        return config;
    }

    @Override
    public void updateDetectionConfig(ViolationDetectionConfig config) {
        this.config = config;
        log.info("Violation detection configuration updated");
    }

    @Override
    public boolean violationExists(Long userId, LocalDate date, AttendanceViolation.ViolationType violationType) {
        return violationRepository.existsByUserIdAndViolationDateAndViolationType(userId, date, violationType);
    }

    @Override
    public AttendanceViolation.ViolationSeverity calculateSeverity(AttendanceViolation.ViolationType violationType,
                                                                  int deviationMinutes) {
        // Different violation types may have different severity thresholds
        switch (violationType) {
            case ABSENT_WITHOUT_LEAVE:
                return AttendanceViolation.ViolationSeverity.MAJOR;

            case MISSING_CHECK_IN:
            case MISSING_CHECK_OUT:
                return AttendanceViolation.ViolationSeverity.MODERATE;

            case LATE_ARRIVAL:
            case EARLY_DEPARTURE:
                if (deviationMinutes >= config.getMajorViolationThresholdMinutes()) {
                    return AttendanceViolation.ViolationSeverity.MAJOR;
                } else if (deviationMinutes >= config.getModerateViolationThresholdMinutes()) {
                    return AttendanceViolation.ViolationSeverity.MODERATE;
                } else {
                    return AttendanceViolation.ViolationSeverity.MINOR;
                }

            default:
                return AttendanceViolation.ViolationSeverity.MINOR;
        }
    }

    @Override
    public String generateSystemDescription(AttendanceViolation.ViolationType violationType,
                                          Integer deviationMinutes,
                                          LocalTime expectedTime,
                                          LocalTime actualTime) {
        StringBuilder description = new StringBuilder();

        switch (violationType) {
            case LATE_ARRIVAL:
                description.append("Nhân viên đi trễ ");
                if (deviationMinutes != null) {
                    description.append(deviationMinutes).append(" phút. ");
                }
                if (expectedTime != null && actualTime != null) {
                    description.append("Dự kiến vào lúc ").append(expectedTime)
                              .append(", thực tế vào lúc ").append(actualTime).append(".");
                }
                break;

            case EARLY_DEPARTURE:
                description.append("Nhân viên về sớm ");
                if (deviationMinutes != null) {
                    description.append(deviationMinutes).append(" phút. ");
                }
                if (expectedTime != null && actualTime != null) {
                    description.append("Dự kiến ra lúc ").append(expectedTime)
                              .append(", thực tế ra lúc ").append(actualTime).append(".");
                }
                break;

            case MISSING_CHECK_IN:
                description.append("Nhân viên không chấm công vào. ");
                if (expectedTime != null) {
                    description.append("Dự kiến vào lúc ").append(expectedTime).append(".");
                }
                break;

            case MISSING_CHECK_OUT:
                description.append("Nhân viên không chấm công ra. ");
                if (expectedTime != null) {
                    description.append("Dự kiến ra lúc ").append(expectedTime).append(".");
                }
                break;

            case ABSENT_WITHOUT_LEAVE:
                description.append("Nhân viên vắng mặt không phép. ");
                if (expectedTime != null) {
                    description.append("Dự kiến có mặt từ ").append(expectedTime).append(".");
                }
                break;

            default:
                description.append("Vi phạm chấm công được phát hiện tự động.");
        }

        return description.toString();
    }

    // Helper methods for creating violations

    private AttendanceViolation createLateArrivalViolation(User user, UserShiftAssignment assignment,
                                                          StaffAttendanceLog log, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();
        int deviationMinutes = calculateDeviationMinutes(shift.getStartTime(), log.getCheckInTime());

        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setShiftAssignment(assignment);
        violation.setAttendanceLog(log);
        violation.setViolationDate(date);
        violation.setViolationType(AttendanceViolation.ViolationType.LATE_ARRIVAL);
        violation.setSeverity(calculateSeverity(AttendanceViolation.ViolationType.LATE_ARRIVAL, deviationMinutes));
        violation.setExpectedTime(shift.getStartTime());
        violation.setActualTime(log.getCheckInTime());
        violation.setDeviationMinutes(deviationMinutes);
        violation.setSystemDescription(generateSystemDescription(
            AttendanceViolation.ViolationType.LATE_ARRIVAL, deviationMinutes,
            shift.getStartTime(), log.getCheckInTime()));

        return violation;
    }

    private AttendanceViolation createEarlyDepartureViolation(User user, UserShiftAssignment assignment,
                                                             StaffAttendanceLog log, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();
        int deviationMinutes = calculateDeviationMinutes(log.getCheckOutTime(), shift.getEndTime());

        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setShiftAssignment(assignment);
        violation.setAttendanceLog(log);
        violation.setViolationDate(date);
        violation.setViolationType(AttendanceViolation.ViolationType.EARLY_DEPARTURE);
        violation.setSeverity(calculateSeverity(AttendanceViolation.ViolationType.EARLY_DEPARTURE, deviationMinutes));
        violation.setExpectedTime(shift.getEndTime());
        violation.setActualTime(log.getCheckOutTime());
        violation.setDeviationMinutes(deviationMinutes);
        violation.setSystemDescription(generateSystemDescription(
            AttendanceViolation.ViolationType.EARLY_DEPARTURE, deviationMinutes,
            shift.getEndTime(), log.getCheckOutTime()));

        return violation;
    }

    private AttendanceViolation createMissingCheckInViolation(User user, UserShiftAssignment assignment,
                                                             StaffAttendanceLog log, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();

        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setShiftAssignment(assignment);
        violation.setAttendanceLog(log);
        violation.setViolationDate(date);
        violation.setViolationType(AttendanceViolation.ViolationType.MISSING_CHECK_IN);
        violation.setSeverity(AttendanceViolation.ViolationSeverity.MODERATE);
        violation.setExpectedTime(shift.getStartTime());
        violation.setSystemDescription(generateSystemDescription(
            AttendanceViolation.ViolationType.MISSING_CHECK_IN, null,
            shift.getStartTime(), null));

        return violation;
    }

    private AttendanceViolation createMissingCheckOutViolation(User user, UserShiftAssignment assignment,
                                                              StaffAttendanceLog log, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();

        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setShiftAssignment(assignment);
        violation.setAttendanceLog(log);
        violation.setViolationDate(date);
        violation.setViolationType(AttendanceViolation.ViolationType.MISSING_CHECK_OUT);
        violation.setSeverity(AttendanceViolation.ViolationSeverity.MODERATE);
        violation.setExpectedTime(shift.getEndTime());
        violation.setSystemDescription(generateSystemDescription(
            AttendanceViolation.ViolationType.MISSING_CHECK_OUT, null,
            shift.getEndTime(), null));

        return violation;
    }

    private AttendanceViolation createAbsentViolation(User user, UserShiftAssignment assignment, LocalDate date) {
        WorkShift shift = assignment.getWorkShift();

        AttendanceViolation violation = new AttendanceViolation();
        violation.setUser(user);
        violation.setShiftAssignment(assignment);
        violation.setViolationDate(date);
        violation.setViolationType(AttendanceViolation.ViolationType.ABSENT_WITHOUT_LEAVE);
        violation.setSeverity(AttendanceViolation.ViolationSeverity.MAJOR);
        violation.setExpectedTime(shift.getStartTime());
        violation.setSystemDescription(generateSystemDescription(
            AttendanceViolation.ViolationType.ABSENT_WITHOUT_LEAVE, null,
            shift.getStartTime(), null));

        return violation;
    }

    private int calculateDeviationMinutes(LocalTime actualTime, LocalTime expectedTime) {
        if (actualTime == null || expectedTime == null) {
            return 0;
        }

        long seconds = Math.abs(actualTime.toSecondOfDay() - expectedTime.toSecondOfDay());
        return (int) (seconds / 60);
    }

    private void updateSummaryForViolation(ViolationDetectionSummary summary, AttendanceViolation violation) {
        switch (violation.getViolationType()) {
            case LATE_ARRIVAL:
                summary.incrementLateArrivals();
                break;
            case EARLY_DEPARTURE:
                summary.incrementEarlyDepartures();
                break;
            case MISSING_CHECK_IN:
                summary.incrementMissingCheckIns();
                break;
            case MISSING_CHECK_OUT:
                summary.incrementMissingCheckOuts();
                break;
            case ABSENT_WITHOUT_LEAVE:
                summary.incrementAbsentWithoutLeave();
                break;
        }
    }
}
