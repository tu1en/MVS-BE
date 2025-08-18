package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.hrmanagement.PayrollResult;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.PayrollGenerationService;
import com.classroomapp.classroombackend.service.impl.hrmanagement.TeacherSalaryCalculationService;
import com.classroomapp.classroombackend.util.TopCVCalculation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payroll generation service using TopCV calculations and contract data
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayrollGenerationServiceImpl implements PayrollGenerationService {
    
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final StaffAttendanceLogRepository attendanceLogRepository;
    
    @Autowired
    private TeacherSalaryCalculationService teacherSalaryCalculationService;
    
    @Override
    public PayrollResult generatePayrollForUser(Long userId, YearMonth period) {
        log.info("🔄 Generating payroll for user {} for period {}", userId, period);
        
        try {
            // Get user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
            
            // Get active contract
            Contract contract = contractRepository.findActiveContractByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active contract found for user: " + user.getFullName()));
            
            // Parse period
            LocalDate periodStart = period.atDay(1);
            LocalDate periodEnd = period.atEndOfMonth();
            
            log.info("📅 Payroll period: {} to {}", periodStart, periodEnd);
            log.info("👤 User: {}, Contract type: {}", user.getFullName(), contract.getContractType());
            
            // Check if this is a teacher with hourly salary
            boolean isTeacherHourly = contract.getContractType() != null &&
                    "TEACHER".equalsIgnoreCase(contract.getContractType()) &&
                    contract.getHourlySalary() != null && contract.getHourlySalary() > 0;
            
            if (isTeacherHourly) {
                // Sử dụng service mới để tính lương từ lịch sử giảng dạy
                log.info("🎓 Teacher with hourly salary detected, using teaching history calculation");
                return generateTeacherPayrollFromTeachingHistory(user, contract, periodStart, periodEnd, period);
            } else {
                // Sử dụng logic cũ cho nhân viên
                log.info("👔 Staff contract detected, using attendance log calculation");
                return generateStaffPayrollFromAttendanceLogs(user, contract, periodStart, periodEnd, period);
            }
            
        } catch (Exception e) {
            log.error("❌ Error generating payroll for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate payroll", e);
        }
    }
    
    @Override
    public List<PayrollResult> generatePayrollForAllEmployees(YearMonth period) {
        log.info("🔄 Generating payroll for all employees for period {}", period);
        
        try {
            // Get all active contracts
            List<Contract> activeContracts = contractRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
            
            return activeContracts.stream()
                    .map(contract -> {
                        try {
                            return generatePayrollForUser(contract.getUserId(), period);
                        } catch (Exception e) {
                            log.warn("⚠️ Failed to generate payroll for user {}: {}", contract.getUserId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("❌ Error generating payroll for all employees: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate payroll for all employees", e);
        }
    }
    
    @Override
    public List<PayrollResult> generatePayrollByContractType(String contractType, YearMonth period) {
        log.info("🔄 Generating payroll for contract type {} for period {}", contractType, period);
        
        try {
            // Get contracts by type and status
            List<Contract> contracts = contractRepository.findByContractTypeAndStatusOrderByCreatedAtDesc(contractType, "ACTIVE");
            
            return contracts.stream()
                    .map(contract -> {
                        try {
                            return generatePayrollForUser(contract.getUserId(), period);
                        } catch (Exception e) {
                            log.warn("⚠️ Failed to generate payroll for user {}: {}", contract.getUserId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("❌ Error generating payroll by contract type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate payroll by contract type", e);
        }
    }
    
    @Override
    public List<PayrollResult> getPayrollHistory(Long userId, YearMonth fromPeriod, YearMonth toPeriod) {
        log.info("🔄 Getting payroll history for user {} from {} to {}", userId, fromPeriod, toPeriod);
        
        try {
            List<PayrollResult> history = new ArrayList<>();
            
            // Generate payroll for each period in the range
            YearMonth current = fromPeriod;
            while (!current.isAfter(toPeriod)) {
                try {
                    PayrollResult result = generatePayrollForUser(userId, current);
                    history.add(result);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to generate payroll for user {} in period {}: {}", userId, current, e.getMessage());
                }
                current = current.plusMonths(1);
            }
            
            return history;
            
        } catch (Exception e) {
            log.error("❌ Error getting payroll history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get payroll history", e);
        }
    }
    
    /**
     * Tính lương giáo viên từ lịch sử giảng dạy
     */
    private PayrollResult generateTeacherPayrollFromTeachingHistory(
            User user, 
            Contract contract, 
            LocalDate periodStart, 
            LocalDate periodEnd, 
            YearMonth period) {
        
        try {
            // Sử dụng service mới để tính lương
            TeacherSalaryCalculationService.TeacherSalaryResult salaryResult = 
                teacherSalaryCalculationService.calculateSalaryFromTeachingHistory(
                    user.getId(), periodStart, periodEnd, contract);
            
            // Chuyển đổi kết quả sang PayrollResult
            PayrollResult payrollResult = new PayrollResult(
                user.getId(),
                user.getFullName(),
                period,
                (int) salaryResult.getTotalTeachingDays(), // totalWorkingDays
                (int) salaryResult.getTotalTeachingDays(), // actualWorkingDays (giáo viên làm việc theo giờ)
                contract.getSalary() != null ? new BigDecimal(contract.getSalary()) : BigDecimal.ZERO, // contractSalary
                salaryResult.getTotalSalary(), // proratedGrossSalary
                salaryResult.getTotalSalary(), // netSalary (giáo viên không có BHXH)
                null // calculationResult
            );
            
            log.info("✅ Teacher payroll generated: {} VND for {} hours", 
                    salaryResult.getTotalSalary(), salaryResult.getTotalTeachingHours());
            
            return payrollResult;
            
        } catch (Exception e) {
            log.error("❌ Error calculating teacher salary from teaching history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate teacher salary from teaching history", e);
        }
    }
    
    /**
     * Tính lương nhân viên từ attendance logs (giữ nguyên logic cũ)
     */
    private PayrollResult generateStaffPayrollFromAttendanceLogs(
            User user, 
            Contract contract, 
            LocalDate periodStart, 
            LocalDate periodEnd, 
            YearMonth period) {
        
        // Logic cũ giữ nguyên
        List<StaffAttendanceLog> attendanceLogs = attendanceLogRepository
                .findByUserIdAndDateRange(user.getId(), periodStart, periodEnd);

        // Sử dụng toàn bộ kỳ lương vì không cần ràng buộc theo ngày bắt đầu/kết thúc hợp đồng
        LocalDate standardStart = periodStart;
        LocalDate standardEnd = periodEnd;

        // Tập ngày trong tuần làm việc theo hợp đồng (mặc định: Mon-Fri)
        Set<DayOfWeek> allowedDays = parseAllowedWorkDays(contract.getWorkDays());

        int totalWorkingDays = calculateWorkingDaysInPeriod(standardStart, standardEnd, allowedDays);
        int actualWorkingDays = (int) attendanceLogs.stream()
                .filter(log -> allowedDays.contains(log.getAttendanceDate().getDayOfWeek()))
                .count();

        // Tính toán cuối tuần để trả lương gấp đôi
        int weekendDaysWorked = 0;
        int weekendHoursWorked = 0;
        int weekdayHoursWorked = 0;
        double totalHours = 0.0;
        for (StaffAttendanceLog logEntry : attendanceLogs) {
            boolean isWeekend = logEntry.getAttendanceDate().getDayOfWeek().getValue() >= 6;
            int hours = (int) Math.round(logEntry.getWorkingHours());
            totalHours += logEntry.getWorkingHours();
            if (isWeekend) {
                weekendDaysWorked += 1;
                weekendHoursWorked += hours;
            } else {
                weekdayHoursWorked += hours;
            }
        }
        
        // Get contract salary
        BigDecimal contractSalary = getContractSalary(contract);
        
        // Calculate prorated salary based on attendance
        BigDecimal proratedGrossSalary = calculateProratedSalary(contractSalary, actualWorkingDays, totalWorkingDays);
        
        // Apply different calculation rules by contract type
        // Chỉ coi là giáo viên trả theo giờ khi có hourlySalary > 0
        boolean isTeacherHourly = contract.getContractType() != null &&
                "TEACHER".equalsIgnoreCase(contract.getContractType()) &&
                contract.getHourlySalary() != null && contract.getHourlySalary() > 0;

        // Nếu là giáo viên và có hourlySalary thì tính theo giờ, có cộng gấp đôi cho giờ cuối tuần
        BigDecimal weekendPay = BigDecimal.ZERO;
        if (isTeacherHourly && contract.getHourlySalary() != null && contract.getHourlySalary() > 0) {
            BigDecimal hourlyRate = new BigDecimal(contract.getHourlySalary());
            BigDecimal totalWeekdayPay = hourlyRate.multiply(new BigDecimal(weekdayHoursWorked));
            // Cuối tuần gấp đôi: 2x
            weekendPay = hourlyRate.multiply(new BigDecimal(2)).multiply(new BigDecimal(weekendHoursWorked));
            BigDecimal grossByHour = totalWeekdayPay.add(weekendPay);
            proratedGrossSalary = grossByHour.setScale(0, RoundingMode.HALF_UP);
        }

        TopCVCalculation.SalaryCalculationResult calculationResult;
        BigDecimal netSalary;

        if (isTeacherHourly) {
            // Hourly/teaching contracts: pay by gross without insurance calculation
            calculationResult = null; // No insurance/tax breakdown for hourly teachers
            netSalary = proratedGrossSalary;
        } else {
            // Staff contracts: calculate insurance and PIT using TopCV rules
            calculationResult = TopCVCalculation.calculateFromGrossToNet(proratedGrossSalary, 0); // 0 dependents by default

            // Áp dụng quy tắc đủ công BHXH: nếu nghỉ không lương >= 14 ngày làm việc ⇒ không đóng BH tháng đó
            int absentDays = Math.max(totalWorkingDays - actualWorkingDays, 0);
            boolean insuranceApplicable = absentDays < 14;
            if (!insuranceApplicable && calculationResult != null && calculationResult.getInsuranceDetails() != null) {
                TopCVCalculation.InsuranceDetails ins = calculationResult.getInsuranceDetails();
                // Cho phía DN về 0 (21.5%); có thể mở tuỳ chọn cho phía NLĐ nếu cần bám sát thực tế
                ins.setSocialInsuranceEmployer(BigDecimal.ZERO);
                ins.setHealthInsuranceEmployer(BigDecimal.ZERO);
                ins.setUnemploymentInsuranceEmployer(BigDecimal.ZERO);
                ins.setWorkAccidentInsurance(BigDecimal.ZERO);
                ins.setTotalEmployerContribution(BigDecimal.ZERO);
                // Nếu muốn không đóng cả phía NLĐ khi không đủ công, bỏ comment 5 dòng dưới:
                // ins.setSocialInsuranceEmployee(BigDecimal.ZERO);
                // ins.setHealthInsuranceEmployee(BigDecimal.ZERO);
                // ins.setUnemploymentInsuranceEmployee(BigDecimal.ZERO);
                // ins.setTotalEmployeeContribution(BigDecimal.ZERO);
                // calculationResult.setNetSalary(proratedGrossSalary); // khi không đóng gì, NET = GROSS (nếu không tính PIT)
            }

            netSalary = (calculationResult != null && calculationResult.getNetSalary() != null)
                    ? calculationResult.getNetSalary()
                    : proratedGrossSalary;
        }
        
        // Create payroll result
        PayrollResult payrollResult = new PayrollResult(
            user.getId(),
            user.getFullName(),
            period,
            totalWorkingDays,
            actualWorkingDays,
            contractSalary,
            proratedGrossSalary,
            netSalary,
            calculationResult
        );
        
        return payrollResult;
    }

    // Helper methods
    private BigDecimal getContractSalary(Contract contract) {
        if (contract.getGrossSalary() != null) {
            return new BigDecimal(contract.getGrossSalary());
        } else if (contract.getSalary() != null) {
            return new BigDecimal(contract.getSalary());
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateProratedSalary(BigDecimal contractSalary, int actualDays, int totalDays) {
        if (totalDays == 0) return BigDecimal.ZERO;
        return contractSalary.multiply(new BigDecimal(actualDays))
                           .divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
    }
    
    private Set<DayOfWeek> parseAllowedWorkDays(String workDays) {
        if (workDays == null || workDays.trim().isEmpty()) {
            // Default: Monday to Friday
            return Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                         DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        }
        
        // Parse workDays string (e.g., "Monday,Tuesday,Wednesday")
        return java.util.Arrays.stream(workDays.split(","))
                .map(String::trim)
                .map(this::parseDayOfWeek)
                .collect(Collectors.toSet());
    }
    
    private DayOfWeek parseDayOfWeek(String dayStr) {
        switch (dayStr.toLowerCase()) {
            case "monday": return DayOfWeek.MONDAY;
            case "tuesday": return DayOfWeek.TUESDAY;
            case "wednesday": return DayOfWeek.WEDNESDAY;
            case "thursday": return DayOfWeek.THURSDAY;
            case "friday": return DayOfWeek.FRIDAY;
            case "saturday": return DayOfWeek.SATURDAY;
            case "sunday": return DayOfWeek.SUNDAY;
            default: return DayOfWeek.MONDAY;
        }
    }
    
    private int calculateWorkingDaysInPeriod(LocalDate start, LocalDate end, Set<DayOfWeek> allowedDays) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (allowedDays.contains(current.getDayOfWeek())) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }
}
