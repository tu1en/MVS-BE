package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

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
            
            int totalWorkingDays = calculateWorkingDaysInPeriod(periodStart, periodEnd);
            int actualWorkingDays = attendanceLogs.size();
            
            // Get contract salary
            BigDecimal contractSalary = getContractSalary(contract);
            
            // Calculate prorated salary based on attendance
            BigDecimal proratedGrossSalary = calculateProratedSalary(contractSalary, actualWorkingDays, totalWorkingDays);
            
            // Apply different calculation rules by contract type
            boolean isTeacherHourly = contract.getContractType() != null &&
                    "TEACHER".equalsIgnoreCase(contract.getContractType());

            // Nếu là giáo viên và có hourlySalary thì override contractSalary theo giờ × giờ công
            if (isTeacherHourly && contract.getHourlySalary() != null && contract.getHourlySalary() > 0) {
                // Ước lượng tổng giờ công chuẩn = totalWorkingDays * 8; giờ thực tế = actualWorkingDays * 8
                BigDecimal hourlyRate = new BigDecimal(contract.getHourlySalary());
                BigDecimal actualHours = new BigDecimal(actualWorkingDays).multiply(new BigDecimal(8));
                BigDecimal grossByHour = hourlyRate.multiply(actualHours);
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
                netSalary = calculationResult.getNetSalary();
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
            payrollResult.setContractStartDate(contract.getStartDate());
            payrollResult.setContractEndDate(contract.getEndDate());
            // Bổ sung đơn giá theo giờ nếu có trong hợp đồng (giúp FE hiển thị)
            if (contract.getHourlySalary() != null && contract.getHourlySalary() > 0) {
                payrollResult.setHourlySalary(new BigDecimal(contract.getHourlySalary()));
            }
            
            log.info("Successfully generated payroll for user {} - Net Salary: {}",
                    user.getFullName(), netSalary);
            
            return payrollResult;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo bảng lương cho người dùng {} và kỳ {}", userId, period, e);
            throw new RuntimeException("Lỗi khi tạo bảng lương: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<PayrollResult> generatePayrollForAllEmployees(YearMonth period) {
        log.info("Generating payroll for all employees for period {}", period);
        
        List<User> activeUsers = userRepository.findActiveUsers();
        List<PayrollResult> results = new ArrayList<>();
        
        for (User user : activeUsers) {
            try {
                // Only generate payroll for staff (not students)
                if (user.getRoleId() != null && user.getRoleId() != 1) { // 1 = STUDENT
                    PayrollResult result = generatePayrollForUser(user.getId(), period);
                    results.add(result);
                }
            } catch (Exception e) {
                log.error("Tạo bảng lương thất bại cho người dùng {}: {}", user.getFullName(), e.getMessage());
            }
        }
        
        log.info("Generated payroll for {} employees", results.size());
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
    
    /**
     * Calculate working days in a period (excluding weekends)
     */
    private int calculateWorkingDaysInPeriod(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            // Exclude Saturday (6) and Sunday (7)
            if (current.getDayOfWeek().getValue() < 6) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
}