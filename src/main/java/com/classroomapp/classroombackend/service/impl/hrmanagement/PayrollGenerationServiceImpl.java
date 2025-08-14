package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    
    // private static final int STANDARD_WORKING_DAYS_PER_MONTH = 22; // unused
    
    @Override
    public PayrollResult generatePayrollForUser(Long userId, YearMonth period) {
        log.info("Generating payroll for user {} for period {}", userId, period);
        
        try {
            // Get user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
            
            // Get active contract
            Contract contract = contractRepository.findActiveContractByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active contract found for user: " + user.getFullName()));
            
            // Get attendance data
            LocalDate periodStart = period.atDay(1);
            LocalDate periodEnd = period.atEndOfMonth();
            
            List<StaffAttendanceLog> attendanceLogs = attendanceLogRepository
                .findByUserIdAndDateRange(userId, periodStart, periodEnd);

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
                userId,
                user.getFullName(),
                period,
                totalWorkingDays,
                actualWorkingDays,
                contractSalary,
                proratedGrossSalary,
                netSalary,
                calculationResult
            );
            
            payrollResult.setUserEmail(user.getEmail());
            payrollResult.setContractType(contract.getContractType());
            payrollResult.setContractOffer(contract.getOffer());
            // Bổ sung đơn giá theo giờ nếu có trong hợp đồng (giúp FE hiển thị)
            if (contract.getHourlySalary() != null && contract.getHourlySalary() > 0) {
                payrollResult.setHourlySalary(new BigDecimal(contract.getHourlySalary()));
            }
            // Đặt cờ đủ công đóng BH cho FE hiển thị
            int absentDaysComputed = Math.max(totalWorkingDays - actualWorkingDays, 0);
            payrollResult.setInsuranceApplicable(absentDaysComputed < 14);
            // Gán thông tin giờ/ngày cuối tuần và tiền cuối tuần để FE hiển thị chi tiết cách tính
            payrollResult.setWeekendWorkingDays(weekendDaysWorked);
            payrollResult.setWeekendWorkingHours(weekendHoursWorked);
            payrollResult.setWeekdayWorkingHours(weekdayHoursWorked);
            if (weekendPay.compareTo(BigDecimal.ZERO) > 0) {
                payrollResult.setWeekendPay(weekendPay.setScale(0, RoundingMode.HALF_UP));
            }

            // Cập nhật giờ công chuẩn và giờ thực tế
            int dailyHours = getDailyHoursFromContract(contract);
            int standardMonthlyHours = totalWorkingDays * dailyHours;
            int actualWorkingHours = (int) Math.round(totalHours);
            payrollResult.setStandardMonthlyHours(standardMonthlyHours);
            payrollResult.setActualWorkingHours(actualWorkingHours);
            
            log.info("Successfully generated payroll for user {} - Net Salary: {}",
                    user.getFullName(), netSalary);
            
            return payrollResult;
            
        } catch (IllegalArgumentException ex) {
            // Giữ nguyên để controller trả 204 No Content theo yêu cầu nghiệp vụ
            throw ex;
        } catch (Exception e) {
            log.error("Error generating payroll for user {} and period {}", userId, period, e);
            throw new RuntimeException("Error generating payroll: " + e.getMessage(), e);
        }
    }

    // New helper: generate payroll directly from a Contract, resolving user by userId or email
    private PayrollResult generatePayrollForContract(Contract contract, YearMonth period) {
        Long userId = contract.getUserId();
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && contract.getEmail() != null) {
            user = userRepository.findByEmail(contract.getEmail()).orElse(null);
        }
        if (user == null) {
            throw new IllegalArgumentException("No user found for contract (id=" + contract.getId() + ") by userId or email");
        }

        try {
            LocalDate periodStart = period.atDay(1);
            LocalDate periodEnd = period.atEndOfMonth();

            List<StaffAttendanceLog> attendanceLogs = attendanceLogRepository
                .findByUserIdAndDateRange(user.getId(), periodStart, periodEnd);

            // Sử dụng toàn bộ kỳ lương vì không cần ràng buộc theo ngày bắt đầu/kết thúc hợp đồng
            LocalDate standardStart = periodStart;
            LocalDate standardEnd = periodEnd;
            Set<DayOfWeek> allowedDays = parseAllowedWorkDays(contract.getWorkDays());
            int totalWorkingDays = calculateWorkingDaysInPeriod(standardStart, standardEnd, allowedDays);
            int actualWorkingDays = (int) attendanceLogs.stream()
                .filter(log -> allowedDays.contains(log.getAttendanceDate().getDayOfWeek()))
                .count();

            // Weekend / weekday hours split
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

            // Contract salary
            BigDecimal contractSalary = getContractSalary(contract);

            // Prorate
            BigDecimal proratedGrossSalary = calculateProratedSalary(contractSalary, actualWorkingDays, totalWorkingDays);

            boolean isTeacherHourly = contract.getContractType() != null &&
                    "TEACHER".equalsIgnoreCase(contract.getContractType()) &&
                    contract.getHourlySalary() != null && contract.getHourlySalary() > 0;

            BigDecimal weekendPay = BigDecimal.ZERO;
            if (isTeacherHourly && contract.getHourlySalary() != null && contract.getHourlySalary() > 0) {
                BigDecimal hourlyRate = new BigDecimal(contract.getHourlySalary());
                BigDecimal totalWeekdayPay = hourlyRate.multiply(new BigDecimal(weekdayHoursWorked));
                weekendPay = hourlyRate.multiply(new BigDecimal(2)).multiply(new BigDecimal(weekendHoursWorked));
                BigDecimal grossByHour = totalWeekdayPay.add(weekendPay);
                proratedGrossSalary = grossByHour.setScale(0, RoundingMode.HALF_UP);
            }

            TopCVCalculation.SalaryCalculationResult calculationResult;
            BigDecimal netSalary;

            if (isTeacherHourly) {
                calculationResult = null;
                netSalary = proratedGrossSalary;
            } else {
                calculationResult = TopCVCalculation.calculateFromGrossToNet(proratedGrossSalary, 0);
                int absentDays = Math.max(totalWorkingDays - actualWorkingDays, 0);
                boolean insuranceApplicable = absentDays < 14;
                if (!insuranceApplicable && calculationResult.getInsuranceDetails() != null) {
                    TopCVCalculation.InsuranceDetails ins = calculationResult.getInsuranceDetails();
                    ins.setSocialInsuranceEmployer(BigDecimal.ZERO);
                    ins.setHealthInsuranceEmployer(BigDecimal.ZERO);
                    ins.setUnemploymentInsuranceEmployer(BigDecimal.ZERO);
                    ins.setWorkAccidentInsurance(BigDecimal.ZERO);
                    ins.setTotalEmployerContribution(BigDecimal.ZERO);
                }
                netSalary = calculationResult.getNetSalary();
            }

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

            payrollResult.setUserEmail(user.getEmail());
            payrollResult.setContractType(contract.getContractType());
            payrollResult.setContractOffer(contract.getOffer());
            // Bỏ việc set startDate và endDate vì không cần thiết
            if (contract.getHourlySalary() != null && contract.getHourlySalary() > 0) {
                payrollResult.setHourlySalary(new BigDecimal(contract.getHourlySalary()));
            }
            int absentDaysComputed = Math.max(totalWorkingDays - actualWorkingDays, 0);
            payrollResult.setInsuranceApplicable(absentDaysComputed < 14);
            payrollResult.setWeekendWorkingDays(weekendDaysWorked);
            payrollResult.setWeekendWorkingHours(weekendHoursWorked);
            payrollResult.setWeekdayWorkingHours(weekdayHoursWorked);
            if (weekendPay.compareTo(BigDecimal.ZERO) > 0) {
                payrollResult.setWeekendPay(weekendPay.setScale(0, RoundingMode.HALF_UP));
            }
            int dailyHours = getDailyHoursFromContract(contract);
            int standardMonthlyHours = totalWorkingDays * dailyHours;
            int actualWorkingHours = (int) Math.round(totalHours);
            payrollResult.setStandardMonthlyHours(standardMonthlyHours);
            payrollResult.setActualWorkingHours(actualWorkingHours);

            return payrollResult;
        } catch (IllegalArgumentException ex) {
            // Bảo toàn IllegalArgumentException cho luồng xử lý phía trên
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Error generating payroll from contract: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<PayrollResult> generatePayrollForAllEmployees(YearMonth period) {
        log.info("Generating payroll for all employees for period {}", period);
        
        // Use ACTIVE contracts as the source of truth to avoid missing users not marked 'active'
        List<Contract> activeContracts = contractRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
        List<PayrollResult> results = new ArrayList<>();
        
        for (Contract contract : activeContracts) {
            try {
                PayrollResult result = generatePayrollForContract(contract, period);
                results.add(result);
            } catch (Exception e) {
                log.error("Tạo bảng lương thất bại cho hợp đồng {}: {}", contract.getId(), e.getMessage());
                log.error("Failed to generate payroll for contract {}: {}", contract.getId(), e.getMessage());
            }
        }
        
        log.info("Generated payroll for {} employees from {} active contracts", results.size(), activeContracts.size());
        return results;
    }
    
    @Override
    public List<PayrollResult> generatePayrollByContractType(String contractType, YearMonth period) {
        log.info("Generating payroll for contract type {} for period {}", contractType, period);
        
        List<Contract> contracts = contractRepository.findByContractTypeAndStatus(contractType, "ACTIVE");
        List<PayrollResult> results = new ArrayList<>();
        
        for (Contract contract : contracts) {
            try {
                PayrollResult result = generatePayrollForUser(contract.getUserId(), period);
                results.add(result);
            } catch (Exception e) {
                log.error("Tạo bảng lương thất bại cho hợp đồng {}: {}", contract.getId(), e.getMessage());
            }
        }
        
        log.info("Generated payroll for {} {} employees", results.size(), contractType);
        return results;
    }
    
    @Override
    public List<PayrollResult> getPayrollHistory(Long userId, YearMonth fromPeriod, YearMonth toPeriod) {
        log.info("Getting payroll history for user {} from {} to {}", userId, fromPeriod, toPeriod);
        
        List<PayrollResult> history = new ArrayList<>();
        YearMonth current = fromPeriod;
        
        while (!current.isAfter(toPeriod)) {
            try {
                PayrollResult result = generatePayrollForUser(userId, current);
                history.add(result);
            } catch (Exception e) {
                log.warn("Could not generate payroll for user {} in period {}: {}", 
                        userId, current, e.getMessage());
            }
            current = current.plusMonths(1);
        }
        
        return history;
    }
    
    /**
     * Get salary from contract, preferring offer over salary field
     */
    private BigDecimal getContractSalary(Contract contract) {
        // First try to get from offer field
        if (contract.getOffer() != null && !contract.getOffer().trim().isEmpty()) {
            try {
                String cleanOffer = contract.getOffer().replaceAll("[^0-9]", "");
                if (!cleanOffer.isEmpty()) {
                    Double offerSalary = Double.parseDouble(cleanOffer.substring(0, Math.min(cleanOffer.length(), 8)));
                    return new BigDecimal(offerSalary);
                }
            } catch (Exception e) {
                log.warn("Could not parse offer '{}', falling back to salary field", contract.getOffer());
            }
        }
        
        // Fall back to salary field
        if (contract.getSalary() != null && contract.getSalary() > 0) {
            return new BigDecimal(contract.getSalary());
        }
        
        // Default salary
        log.warn("No valid salary found for contract {}, using default 5,000,000", contract.getId());
        return new BigDecimal("5000000");
    }
    
    /**
     * Calculate prorated salary based on attendance
     */
    private BigDecimal calculateProratedSalary(BigDecimal contractSalary, int actualDays, int totalDays) {
        if (totalDays <= 0 || actualDays < 0) {
            return BigDecimal.ZERO;
        }
        
        if (actualDays >= totalDays) {
            return contractSalary; // Full salary if worked all days
        }
        
        BigDecimal ratio = new BigDecimal(actualDays).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP);
        return contractSalary.multiply(ratio).setScale(0, RoundingMode.HALF_UP);
    }
    
    // Overload giữ lại để tương thích, sử dụng allowedDays mặc định Mon-Fri
    private int calculateWorkingDaysInPeriod(LocalDate startDate, LocalDate endDate) {
        return calculateWorkingDaysInPeriod(startDate, endDate, defaultWeekdays());
    }

    private int calculateWorkingDaysInPeriod(LocalDate startDate, LocalDate endDate, java.util.Set<java.time.DayOfWeek> allowedDays) {
        if (endDate.isBefore(startDate)) {
            return 0;
        }
        int workingDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (allowedDays.contains(current.getDayOfWeek())) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        return workingDays;
    }

    private java.util.Set<java.time.DayOfWeek> defaultWeekdays() {
        java.util.Set<java.time.DayOfWeek> set = new java.util.HashSet<>();
        set.add(java.time.DayOfWeek.MONDAY);
        set.add(java.time.DayOfWeek.TUESDAY);
        set.add(java.time.DayOfWeek.WEDNESDAY);
        set.add(java.time.DayOfWeek.THURSDAY);
        set.add(java.time.DayOfWeek.FRIDAY);
        return set;
    }

    private java.util.Set<java.time.DayOfWeek> parseAllowedWorkDays(String workDays) {
        if (workDays == null || workDays.trim().isEmpty()) {
            return defaultWeekdays();
        }
        java.util.Set<java.time.DayOfWeek> set = new java.util.HashSet<>();
        String[] parts = workDays.split(",");
        for (String p : parts) {
            String trimmed = p.trim().toUpperCase();
            java.time.DayOfWeek dow;
            switch (trimmed) {
                case "MON": case "MONDAY": dow = java.time.DayOfWeek.MONDAY; break;
                case "TUE": case "TUESDAY": dow = java.time.DayOfWeek.TUESDAY; break;
                case "WED": case "WEDNESDAY": dow = java.time.DayOfWeek.WEDNESDAY; break;
                case "THU": case "THURSDAY": dow = java.time.DayOfWeek.THURSDAY; break;
                case "FRI": case "FRIDAY": dow = java.time.DayOfWeek.FRIDAY; break;
                case "SAT": case "SATURDAY": dow = java.time.DayOfWeek.SATURDAY; break;
                case "SUN": case "SUNDAY": dow = java.time.DayOfWeek.SUNDAY; break;
                default: continue;
            }
            set.add(dow);
        }
        if (set.isEmpty()) {
            return defaultWeekdays();
        }
        return set;
    }

    private int getDailyHoursFromContract(Contract contract) {
        return 8;
    }
}