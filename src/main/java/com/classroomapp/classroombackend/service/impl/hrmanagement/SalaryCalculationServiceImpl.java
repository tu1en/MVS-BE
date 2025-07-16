package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.hrmanagement.Payroll;
import com.classroomapp.classroombackend.model.hrmanagement.SalaryStructure;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.PayrollRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.SalaryStructureRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.AttendancePenalties;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.AttendanceSummary;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.BulkPayrollResult;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.DepartmentPayrollSummary;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.MonthlyPayrollSummary;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.MonthlyPayrollTrend;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.PayrollComparison;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.PayrollProcessingStatus;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.PayrollStatistics;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService.PayrollValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SalaryCalculationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalaryCalculationServiceImpl implements SalaryCalculationService {
    
    private final PayrollRepository payrollRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final StaffAttendanceLogRepository attendanceLogRepository;
    private final UserRepository userRepository;
    
    // Constants for calculation
    private static final BigDecimal STANDARD_WORKING_HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal STANDARD_WORKING_DAYS_PER_MONTH = new BigDecimal("22");
    private static final BigDecimal LATE_PENALTY_RATE = new BigDecimal("0.01"); // 1% per late
    private static final BigDecimal ABSENT_PENALTY_RATE = new BigDecimal("0.05"); // 5% per absent day
    
    @Override
    public Payroll calculatePayrollForUser(Long userId, YearMonth period) {
        log.info("Calculating payroll for user {} for period {}", userId, period);
        
        try {
            // Check if payroll already exists
            Optional<Payroll> existingPayroll = payrollRepository.findByUserIdAndPeriod(
                userId, period.getYear(), period.getMonthValue());
            
            if (existingPayroll.isPresent() && 
                existingPayroll.get().getStatus() != Payroll.PayrollStatus.DRAFT) {
                log.warn("Payroll already exists for user {} and period {}", userId, period);
                return existingPayroll.get();
            }
            
            // Get user and salary structure
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + userId));
            
            LocalDate periodStart = period.atDay(1);
            SalaryStructure salaryStructure = salaryStructureRepository
                .findActiveByUserIdAndDate(userId, periodStart)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy cấu trúc lương cho nhân viên " + user.getFullName()));
            
            // Get attendance summary
            AttendanceSummary attendanceSummary = getAttendanceSummaryForPayroll(userId, period);
            
            // Create or update payroll
            Payroll payroll = existingPayroll.orElse(new Payroll());
            payroll.setUser(user);
            payroll.setSalaryStructure(salaryStructure);
            payroll.setPayrollYear(period.getYear());
            payroll.setPayrollMonth(period.getMonthValue());
            payroll.setPayPeriodStart(periodStart);
            payroll.setPayPeriodEnd(period.atEndOfMonth());
            
            // Set attendance data
            setAttendanceData(payroll, attendanceSummary);
            
            // Calculate salary components
            calculateSalaryComponents(payroll, salaryStructure, attendanceSummary);
            
            // Set status and timestamps
            payroll.setStatus(Payroll.PayrollStatus.CALCULATED);
            payroll.setCalculatedAt(java.time.LocalDateTime.now());
            
            // Save payroll
            Payroll savedPayroll = payrollRepository.save(payroll);
            
            log.info("Successfully calculated payroll for user {} for period {}", userId, period);
            return savedPayroll;
            
        } catch (Exception e) {
            log.error("Error calculating payroll for user {} and period {}", userId, period, e);
            throw new RuntimeException("Lỗi khi tính lương: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Payroll> calculatePayrollForPeriod(YearMonth period) {
        log.info("Calculating payroll for all users for period {}", period);
        
        List<Payroll> results = new ArrayList<>();
        List<User> activeUsers = userRepository.findActiveUsers();
        
        for (User user : activeUsers) {
            try {
                Payroll payroll = calculatePayrollForUser(user.getId(), period);
                results.add(payroll);
            } catch (Exception e) {
                log.error("Failed to calculate payroll for user {}: {}", user.getFullName(), e.getMessage());
                // Continue with other users
            }
        }
        
        log.info("Completed payroll calculation for period {}. Processed {} users", 
                period, results.size());
        return results;
    }
    
    @Override
    public Payroll recalculatePayroll(Long payrollId) {
        log.info("Recalculating payroll with ID: {}", payrollId);
        
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bảng lương với ID: " + payrollId));
        
        if (!payroll.canBeEdited()) {
            throw new IllegalStateException("Bảng lương không thể chỉnh sửa ở trạng thái hiện tại");
        }
        
        YearMonth period = YearMonth.of(payroll.getPayrollYear(), payroll.getPayrollMonth());
        return calculatePayrollForUser(payroll.getUser().getId(), period);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payroll getPayrollById(Long payrollId) {
        return payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bảng lương với ID: " + payrollId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payroll getPayrollForUserAndPeriod(Long userId, YearMonth period) {
        return payrollRepository.findByUserIdAndPeriod(userId, period.getYear(), period.getMonthValue())
            .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Payroll> getPayrollsForUser(Long userId, Pageable pageable) {
        return payrollRepository.findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Payroll> getPayrollsForPeriod(YearMonth period, Pageable pageable) {
        return payrollRepository.findByPeriod(period.getYear(), period.getMonthValue(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Payroll> getPayrollsByStatus(Payroll.PayrollStatus status, Pageable pageable) {
        return payrollRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Payroll approvePayroll(Long payrollId, Long approvedBy) {
        log.info("Approving payroll {} by user {}", payrollId, approvedBy);
        
        Payroll payroll = getPayrollById(payrollId);
        payroll.approve(approvedBy);
        
        return payrollRepository.save(payroll);
    }
    
    @Override
    public List<Payroll> approvePayrolls(List<Long> payrollIds, Long approvedBy) {
        log.info("Bulk approving {} payrolls by user {}", payrollIds.size(), approvedBy);
        
        List<Payroll> approvedPayrolls = new ArrayList<>();
        
        for (Long payrollId : payrollIds) {
            try {
                Payroll approved = approvePayroll(payrollId, approvedBy);
                approvedPayrolls.add(approved);
            } catch (Exception e) {
                log.error("Failed to approve payroll {}: {}", payrollId, e.getMessage());
            }
        }
        
        return approvedPayrolls;
    }
    
    @Override
    public Payroll markPayrollAsPaid(Long payrollId, Long paidBy) {
        log.info("Marking payroll {} as paid by user {}", payrollId, paidBy);
        
        Payroll payroll = getPayrollById(payrollId);
        payroll.markAsPaid(paidBy);
        
        return payrollRepository.save(payroll);
    }
    
    @Override
    public void cancelPayroll(Long payrollId) {
        log.info("Cancelling payroll {}", payrollId);
        
        Payroll payroll = getPayrollById(payrollId);
        payroll.cancel();
        
        payrollRepository.save(payroll);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollStatistics getPayrollStatisticsForPeriod(YearMonth period) {
        Object[] stats = payrollRepository.getPayrollStatisticsForPeriod(
            period.getYear(), period.getMonthValue());
        
        if (stats != null && stats.length >= 5) {
            return new PayrollStatistics(
                (Long) stats[0],           // totalPayrolls
                (BigDecimal) stats[1],     // totalGrossSalary
                (BigDecimal) stats[2],     // totalNetSalary
                (BigDecimal) stats[3],     // totalDeductions
                (BigDecimal) stats[4],     // avgNetSalary
                period
            );
        }
        
        return new PayrollStatistics(0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                                   BigDecimal.ZERO, BigDecimal.ZERO, period);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPayrollSummary> getYearlyPayrollSummary(Long userId, Integer year) {
        List<Object[]> results = payrollRepository.getYearlyPayrollForUser(userId, year);
        
        return results.stream()
            .map(row -> new MonthlyPayrollSummary(
                (Integer) row[0],      // month
                year,                  // year
                (BigDecimal) row[1],   // grossSalary
                (BigDecimal) row[2],   // netSalary
                (BigDecimal) row[3]    // totalDeductions
            ))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentPayrollSummary> getDepartmentPayrollSummary(YearMonth period) {
        List<Object[]> results = payrollRepository.getDepartmentPayrollSummary(
            period.getYear(), period.getMonthValue());
        
        return results.stream()
            .map(row -> new DepartmentPayrollSummary(
                (String) row[0],       // department
                (Long) row[1],         // employeeCount
                (BigDecimal) row[2],   // totalGrossSalary
                (BigDecimal) row[3],   // totalNetSalary
                (BigDecimal) row[4]    // averageNetSalary
            ))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Payroll> getTopEarners(YearMonth period, int limit) {
        return payrollRepository.findTopEarnersForPeriod(
            period.getYear(), period.getMonthValue(), 
            org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    // Private helper methods
    
    private void setAttendanceData(Payroll payroll, AttendanceSummary attendanceSummary) {
        payroll.setTotalWorkingDays(attendanceSummary.getTotalWorkingDays());
        payroll.setActualWorkingDays(attendanceSummary.getActualWorkingDays());
        payroll.setRegularHours(attendanceSummary.getRegularHours());
        payroll.setOvertimeHours(attendanceSummary.getOvertimeHours());
        payroll.setHolidayHours(attendanceSummary.getHolidayHours());
        payroll.setWeekendHours(attendanceSummary.getWeekendHours());
        payroll.setLateArrivals(attendanceSummary.getLateArrivals());
        payroll.setEarlyDepartures(attendanceSummary.getEarlyDepartures());
        payroll.setAbsentDays(attendanceSummary.getAbsentDays());
        payroll.setLeaveDays(attendanceSummary.getLeaveDays());
        
        // Calculate total working hours
        BigDecimal totalHours = attendanceSummary.getRegularHours()
            .add(attendanceSummary.getOvertimeHours())
            .add(attendanceSummary.getHolidayHours())
            .add(attendanceSummary.getWeekendHours());
        payroll.setTotalWorkingHours(totalHours);
    }
    
    private void calculateSalaryComponents(Payroll payroll, SalaryStructure salaryStructure, 
                                         AttendanceSummary attendanceSummary) {
        
        // Set base salary
        payroll.setBaseSalary(salaryStructure.getBaseSalary());
        
        // Calculate regular pay based on working days
        BigDecimal regularPay = calculateRegularPay(salaryStructure, attendanceSummary);
        payroll.setRegularPay(regularPay);
        
        // Calculate overtime pay
        BigDecimal overtimePay = calculateOvertimePay(attendanceSummary.getOvertimeHours(), salaryStructure);
        payroll.setOvertimePay(overtimePay);
        
        // Calculate holiday pay
        BigDecimal holidayPay = calculateHolidayPay(attendanceSummary.getHolidayHours(), salaryStructure);
        payroll.setHolidayPay(holidayPay);
        
        // Calculate weekend pay
        BigDecimal weekendPay = calculateWeekendPay(attendanceSummary.getWeekendHours(), salaryStructure);
        payroll.setWeekendPay(weekendPay);
        
        // Set allowances
        setAllowances(payroll, salaryStructure);
        
        // Calculate deductions
        calculateDeductions(payroll, salaryStructure, attendanceSummary);
        
        // Recalculate totals
        payroll.recalculateTotals();
    }
    
    private BigDecimal calculateRegularPay(SalaryStructure salaryStructure, AttendanceSummary attendanceSummary) {
        if (salaryStructure.getSalaryType() == SalaryStructure.SalaryType.MONTHLY) {
            // For monthly salary, calculate based on working days ratio
            BigDecimal workingDaysRatio = attendanceSummary.getActualWorkingDays()
                .divide(attendanceSummary.getTotalWorkingDays(), 4, RoundingMode.HALF_UP);
            return salaryStructure.getBaseSalary().multiply(workingDaysRatio);
        } else if (salaryStructure.getSalaryType() == SalaryStructure.SalaryType.HOURLY) {
            // For hourly salary, calculate based on regular hours
            return attendanceSummary.getRegularHours().multiply(salaryStructure.getHourlyRate());
        } else {
            // For daily salary
            return attendanceSummary.getActualWorkingDays().multiply(
                salaryStructure.getBaseSalary().divide(STANDARD_WORKING_DAYS_PER_MONTH, 2, RoundingMode.HALF_UP));
        }
    }
    
    private BigDecimal calculateWeekendPay(BigDecimal weekendHours, SalaryStructure salaryStructure) {
        if (weekendHours == null || weekendHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal weekendRate = salaryStructure.getWeekendRate();
        if (weekendRate == null) {
            // Default weekend rate is 1.5x hourly rate
            BigDecimal hourlyRate = getHourlyRate(salaryStructure);
            weekendRate = hourlyRate.multiply(new BigDecimal("1.5"));
        }
        
        return weekendHours.multiply(weekendRate);
    }
    
    private void setAllowances(Payroll payroll, SalaryStructure salaryStructure) {
        payroll.setPositionAllowance(salaryStructure.getPositionAllowance());
        payroll.setTransportAllowance(salaryStructure.getTransportAllowance());
        payroll.setMealAllowance(salaryStructure.getMealAllowance());
        payroll.setPhoneAllowance(salaryStructure.getPhoneAllowance());
        payroll.setOtherAllowances(salaryStructure.getOtherAllowances());
    }
    
    private void calculateDeductions(Payroll payroll, SalaryStructure salaryStructure, 
                                   AttendanceSummary attendanceSummary) {
        
        // Calculate insurance deductions
        BigDecimal baseSalary = salaryStructure.getBaseSalary();
        payroll.setSocialInsurance(baseSalary.multiply(salaryStructure.getSocialInsuranceRate()));
        payroll.setHealthInsurance(baseSalary.multiply(salaryStructure.getHealthInsuranceRate()));
        payroll.setUnemploymentInsurance(baseSalary.multiply(salaryStructure.getUnemploymentInsuranceRate()));
        
        // Calculate personal income tax
        BigDecimal personalIncomeTax = salaryStructure.getPersonalIncomeTax();
        payroll.setPersonalIncomeTax(personalIncomeTax);
        
        // Calculate attendance penalties
        AttendancePenalties penalties = calculateAttendancePenalties(attendanceSummary, salaryStructure);
        payroll.setLatePenalty(penalties.getLatePenalty());
        payroll.setAbsentPenalty(penalties.getAbsentPenalty());
        
        // Set other deductions
        payroll.setOtherDeductions(salaryStructure.getOtherDeductions());
    }
    
    private BigDecimal getHourlyRate(SalaryStructure salaryStructure) {
        if (salaryStructure.getHourlyRate() != null) {
            return salaryStructure.getHourlyRate();
        }

        // Calculate hourly rate from monthly salary
        BigDecimal monthlyHours = STANDARD_WORKING_DAYS_PER_MONTH.multiply(STANDARD_WORKING_HOURS_PER_DAY);
        return salaryStructure.getBaseSalary().divide(monthlyHours, 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateOvertimePay(BigDecimal overtimeHours, SalaryStructure salaryStructure) {
        if (overtimeHours == null || overtimeHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal overtimeRate = salaryStructure.getOvertimeRate();
        if (overtimeRate == null) {
            // Default overtime rate is 1.5x hourly rate
            BigDecimal hourlyRate = getHourlyRate(salaryStructure);
            overtimeRate = hourlyRate.multiply(new BigDecimal("1.5"));
        }

        return overtimeHours.multiply(overtimeRate);
    }

    @Override
    public BigDecimal calculateHolidayPay(BigDecimal holidayHours, SalaryStructure salaryStructure) {
        if (holidayHours == null || holidayHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal holidayRate = salaryStructure.getHolidayRate();
        if (holidayRate == null) {
            // Default holiday rate is 2x hourly rate
            BigDecimal hourlyRate = getHourlyRate(salaryStructure);
            holidayRate = hourlyRate.multiply(new BigDecimal("2.0"));
        }

        return holidayHours.multiply(holidayRate);
    }

    @Override
    public AttendancePenalties calculateAttendancePenalties(AttendanceSummary attendanceSummary,
                                                          SalaryStructure salaryStructure) {

        BigDecimal baseSalary = salaryStructure.getBaseSalary();

        // Calculate late penalty
        BigDecimal latePenalty = BigDecimal.ZERO;
        if (attendanceSummary.getLateArrivals() != null && attendanceSummary.getLateArrivals() > 0) {
            latePenalty = baseSalary
                .multiply(LATE_PENALTY_RATE)
                .multiply(new BigDecimal(attendanceSummary.getLateArrivals()));
        }

        // Calculate absent penalty
        BigDecimal absentPenalty = BigDecimal.ZERO;
        if (attendanceSummary.getAbsentDays() != null &&
            attendanceSummary.getAbsentDays().compareTo(BigDecimal.ZERO) > 0) {
            absentPenalty = baseSalary
                .multiply(ABSENT_PENALTY_RATE)
                .multiply(attendanceSummary.getAbsentDays());
        }

        return new AttendancePenalties(latePenalty, absentPenalty);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummary getAttendanceSummaryForPayroll(Long userId, YearMonth period) {
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();

        AttendanceSummary summary = new AttendanceSummary();

        // Calculate total working days (excluding weekends)
        BigDecimal totalWorkingDays = calculateWorkingDaysInPeriod(startDate, endDate);
        summary.setTotalWorkingDays(totalWorkingDays);

        // Get attendance logs for the period
        List<StaffAttendanceLog> attendanceLogs = attendanceLogRepository
            .findByUserIdAndDateRange(userId, startDate, endDate);

        // Calculate actual working days
        BigDecimal actualWorkingDays = new BigDecimal(attendanceLogs.size());
        summary.setActualWorkingDays(actualWorkingDays);

        // Calculate hours and violations
        BigDecimal regularHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal holidayHours = BigDecimal.ZERO;
        BigDecimal weekendHours = BigDecimal.ZERO;
        int lateArrivals = 0;
        int earlyDepartures = 0;

        for (StaffAttendanceLog log : attendanceLogs) {
            // Calculate working hours for the day
            BigDecimal dailyHours = calculateDailyWorkingHours(log);

            if (isHoliday(log.getAttendanceDate())) {
                holidayHours = holidayHours.add(dailyHours);
            } else if (isWeekend(log.getAttendanceDate())) {
                weekendHours = weekendHours.add(dailyHours);
            } else {
                // Regular working day
                if (dailyHours.compareTo(STANDARD_WORKING_HOURS_PER_DAY) > 0) {
                    regularHours = regularHours.add(STANDARD_WORKING_HOURS_PER_DAY);
                    overtimeHours = overtimeHours.add(dailyHours.subtract(STANDARD_WORKING_HOURS_PER_DAY));
                } else {
                    regularHours = regularHours.add(dailyHours);
                }
            }

            // Check for violations
            if (log.isLateArrival()) {
                lateArrivals++;
            }
            if (log.isEarlyDeparture()) {
                earlyDepartures++;
            }
        }

        summary.setRegularHours(regularHours);
        summary.setOvertimeHours(overtimeHours);
        summary.setHolidayHours(holidayHours);
        summary.setWeekendHours(weekendHours);
        summary.setLateArrivals(lateArrivals);
        summary.setEarlyDepartures(earlyDepartures);

        // Calculate absent days
        BigDecimal absentDays = totalWorkingDays.subtract(actualWorkingDays);
        summary.setAbsentDays(absentDays.compareTo(BigDecimal.ZERO) > 0 ? absentDays : BigDecimal.ZERO);

        // TODO: Get leave days from leave management system
        summary.setLeaveDays(BigDecimal.ZERO);

        return summary;
    }

    private BigDecimal calculateWorkingDaysInPeriod(LocalDate startDate, LocalDate endDate) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long workingDays = 0;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!isWeekend(current) && !isHoliday(current)) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return new BigDecimal(workingDays);
    }

    private BigDecimal calculateDailyWorkingHours(StaffAttendanceLog log) {
        if (log.getCheckInTime() == null || log.getCheckOutTime() == null) {
            return BigDecimal.ZERO;
        }

        long minutes = ChronoUnit.MINUTES.between(log.getCheckInTime(), log.getCheckOutTime());

        // Subtract lunch break (1 hour = 60 minutes)
        if (minutes > 60) {
            minutes -= 60;
        }

        return new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
               date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
    }

    private boolean isHoliday(LocalDate date) {
        // TODO: Implement holiday checking logic
        // This should check against a holiday calendar
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollComparison> getPayrollComparison(YearMonth currentPeriod, YearMonth previousPeriod) {
        List<Object[]> results = payrollRepository.getPayrollComparison(
            currentPeriod.getYear(), currentPeriod.getMonthValue(),
            previousPeriod.getYear(), previousPeriod.getMonthValue());

        return results.stream()
            .map(row -> {
                PayrollComparison comparison = new PayrollComparison();
                comparison.setUserId((Long) row[0]);
                comparison.setUserFullName((String) row[1]);
                comparison.setCurrentSalary((BigDecimal) row[2]);
                comparison.setPreviousSalary((BigDecimal) row[3]);
                comparison.setDifference((BigDecimal) row[4]);

                // Calculate percentage change
                BigDecimal previousSalary = comparison.getPreviousSalary();
                if (previousSalary != null && previousSalary.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal percentageChange = comparison.getDifference()
                        .divide(previousSalary, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                    comparison.setPercentageChange(percentageChange);
                } else {
                    comparison.setPercentageChange(BigDecimal.ZERO);
                }

                return comparison;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPayrollTrend> getMonthlyPayrollTrends(int months) {
        YearMonth startPeriod = YearMonth.now().minusMonths(months - 1);

        List<Object[]> results = payrollRepository.getMonthlyPayrollTrends(
            startPeriod.getYear(), startPeriod.getMonthValue());

        return results.stream()
            .map(row -> {
                MonthlyPayrollTrend trend = new MonthlyPayrollTrend();
                trend.setYear((Integer) row[0]);
                trend.setMonth((Integer) row[1]);
                trend.setPayrollCount((Long) row[2]);
                trend.setTotalGrossSalary((BigDecimal) row[3]);
                trend.setTotalNetSalary((BigDecimal) row[4]);
                trend.setAverageNetSalary((BigDecimal) row[5]);
                return trend;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollValidationResult validatePayroll(Long payrollId) {
        PayrollValidationResult result = new PayrollValidationResult();

        try {
            Payroll payroll = getPayrollById(payrollId);

            // Validate basic data
            if (payroll.getUser() == null) {
                result.addError("Bảng lương không có thông tin nhân viên");
            }

            if (payroll.getSalaryStructure() == null) {
                result.addError("Bảng lương không có cấu trúc lương");
            }

            // Validate salary calculations
            if (payroll.getNetSalary().compareTo(BigDecimal.ZERO) < 0) {
                result.addError("Lương thực nhận không thể âm");
            }

            if (payroll.getTotalDeductions().compareTo(payroll.getGrossSalary()) > 0) {
                result.addWarning("Tổng khấu trừ lớn hơn lương gốc");
            }

            // Validate attendance data
            if (payroll.getActualWorkingDays().compareTo(payroll.getTotalWorkingDays()) > 0) {
                result.addError("Số ngày làm việc thực tế không thể lớn hơn tổng số ngày làm việc");
            }

            // Validate overtime
            if (payroll.getOvertimeHours().compareTo(new BigDecimal("100")) > 0) {
                result.addWarning("Số giờ làm thêm quá cao (>100 giờ/tháng)");
            }

            result.setValid(result.getErrors().isEmpty());

        } catch (Exception e) {
            result.addError("Lỗi khi validate bảng lương: " + e.getMessage());
            result.setValid(false);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollProcessingStatus getPayrollProcessingStatus(YearMonth period) {
        PayrollProcessingStatus status = new PayrollProcessingStatus();
        status.setPeriod(period);

        // Get total active employees
        List<User> activeUsers = userRepository.findActiveUsers();
        status.setTotalEmployees(activeUsers.size());

        // Count payrolls by status
        List<Object[]> statusCounts = payrollRepository.countByStatusForPeriod(
            period.getYear(), period.getMonthValue());

        int processedCount = 0;
        int pendingCount = 0;
        int errorCount = 0;

        for (Object[] row : statusCounts) {
            Payroll.PayrollStatus payrollStatus = (Payroll.PayrollStatus) row[0];
            Long count = (Long) row[1];

            switch (payrollStatus) {
                case CALCULATED:
                case APPROVED:
                case PAID:
                    processedCount += count.intValue();
                    break;
                case DRAFT:
                    pendingCount += count.intValue();
                    break;
                case CANCELLED:
                    errorCount += count.intValue();
                    break;
            }
        }

        status.setProcessedCount(processedCount);
        status.setPendingCount(pendingCount);
        status.setErrorCount(errorCount);
        status.setCompleted(pendingCount == 0 && processedCount == status.getTotalEmployees());

        return status;
    }

    @Override
    public BulkPayrollResult processBulkPayrollCalculation(YearMonth period, List<Long> userIds) {
        log.info("Processing bulk payroll calculation for {} users in period {}", userIds.size(), period);

        BulkPayrollResult result = new BulkPayrollResult();
        result.setPeriod(period);
        result.setTotalRequested(userIds.size());

        int successCount = 0;
        int errorCount = 0;

        for (Long userId : userIds) {
            try {
                Payroll payroll = calculatePayrollForUser(userId, period);
                result.addSuccessfulPayroll(payroll);
                successCount++;
            } catch (Exception e) {
                result.addError("User ID " + userId + ": " + e.getMessage());
                errorCount++;
                log.error("Failed to calculate payroll for user {}: {}", userId, e.getMessage());
            }
        }

        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);

        log.info("Bulk payroll calculation completed. Success: {}, Errors: {}", successCount, errorCount);
        return result;
    }
}
