package com.classroomapp.classroombackend.service.hrmanagement.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.hrmanagement.PayrollRecordDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.PayrollSummaryDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.ViolationImpactDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.PayrollReportDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.DeductionDetailDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.WorkingHoursDetailDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.PayrollPreviewDto;
// import com.classroomapp.classroombackend.dto.hrmanagement.MonthlyPayrollStatsDto;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.hrmanagement.PayrollRecord;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.PayrollRecordRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.PayrollService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of PayrollService for payroll management and calculation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayrollServiceImpl implements PayrollService {
    
    private final PayrollRecordRepository payrollRecordRepository;
    private final UserRepository userRepository;
    private final StaffAttendanceLogRepository attendanceLogRepository;
    private final AttendanceViolationRepository violationRepository;
    // private final SalaryCalculationService salaryCalculationService;
    
    // Standard deduction rates
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.08"); // 8% insurance
    private static final BigDecimal VIOLATION_DEDUCTION_RATE = new BigDecimal("50.00"); // 50k VND per violation
    
    @Override
    public PayrollRecordDto generatePayroll(Long staffId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating payroll for staff {} from {} to {}", staffId, startDate, endDate);
        
        User staff = userRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
        
        // Check if payroll already exists
        Optional<PayrollRecord> existing = payrollRecordRepository
            .findByStaffIdAndPayPeriodStartAndPayPeriodEnd(staffId, startDate, endDate);
        
        if (existing.isPresent()) {
            throw new RuntimeException("Payroll already exists for this staff and period");
        }
        
        PayrollRecord payrollRecord = createPayrollRecord(staff, startDate, endDate);
        payrollRecord = payrollRecordRepository.save(payrollRecord);
        
        return convertToDto(payrollRecord);
    }
    
    @Override
    public List<PayrollRecordDto> generateBulkPayroll(LocalDate startDate, LocalDate endDate) {
        log.info("Generating bulk payroll from {} to {}", startDate, endDate);
        
        List<User> allStaff = userRepository.findAll().stream()
            .filter(user -> user.getRole() != null && 
                   (user.getRole().equals("TEACHER") || user.getRole().equals("STAFF")))
            .collect(Collectors.toList());
        
        return allStaff.stream()
            .map(staff -> {
                try {
                    return generatePayroll(staff.getId(), startDate, endDate);
                } catch (RuntimeException e) {
                    log.warn("Failed to generate payroll for staff {}: {}", staff.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }
    
    @Override
    public BigDecimal calculateWorkingHours(Long staffId, LocalDate startDate, LocalDate endDate) {
        List<StaffAttendanceLog> attendanceLogs = attendanceLogRepository
            .findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(staffId, startDate, endDate);
        
        return attendanceLogs.stream()
            .filter(log -> log.getCheckInTime() != null && log.getCheckOutTime() != null)
            .map(log -> {
                long minutes = java.time.Duration.between(log.getCheckInTime(), log.getCheckOutTime()).toMinutes();
                return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal calculateTeachingHours(Long staffId, LocalDate startDate, LocalDate endDate) {
        // For now, assume all working hours are teaching hours for teachers
        // This could be enhanced to differentiate between teaching and non-teaching hours
        User staff = userRepository.findById(staffId).orElse(null);
        if (staff != null && "TEACHER".equals(staff.getRole())) {
            return calculateWorkingHours(staffId, startDate, endDate);
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal calculateDeductions(Long staffId, LocalDate startDate, LocalDate endDate) {
        // Get violations in the period
        List<AttendanceViolation> violations = violationRepository
            .findByUserIdAndViolationDateBetweenOrderByViolationDateDesc(staffId, startDate, endDate);
        
        // Calculate violation-based deductions
        BigDecimal violationDeductions = violations.stream()
            .filter(v -> !v.isResolved()) // Only unresolved violations
            .map(v -> getViolationDeductionAmount(v))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return violationDeductions;
    }
    
    private BigDecimal getViolationDeductionAmount(AttendanceViolation violation) {
        // Base deduction amount varies by violation type and severity
        BigDecimal baseAmount = VIOLATION_DEDUCTION_RATE;
        
        // Adjust based on severity
        switch (violation.getSeverity()) {
            case MINOR:
                return baseAmount;
            case MODERATE:
                return baseAmount.multiply(BigDecimal.valueOf(2));
            case MAJOR:
                return baseAmount.multiply(BigDecimal.valueOf(3));
            case CRITICAL:
                return baseAmount.multiply(BigDecimal.valueOf(5));
            default:
                return baseAmount;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollRecordDto getPayrollById(Long id) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        return convertToDto(payrollRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PayrollRecordDto> getPayrollByStaff(Long staffId, Pageable pageable) {
        Page<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByStaffIdOrderByPayPeriodStartDesc(staffId, pageable);
        return payrollRecords.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PayrollRecordDto> getPayrollByPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByPayPeriodStartGreaterThanEqualAndPayPeriodEndLessThanEqualOrderByGeneratedAtDesc(
                startDate, endDate, pageable);
        return payrollRecords.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PayrollRecordDto> getPayrollByStatus(PayrollRecord.PayrollStatus status, Pageable pageable) {
        Page<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByStatusOrderByGeneratedAtDesc(status, pageable);
        return payrollRecords.map(this::convertToDto);
    }
    
    @Override
    public PayrollRecordDto updatePayrollStatus(Long id, PayrollRecord.PayrollStatus status) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        
        if (!payrollRecord.isEditable() && status != PayrollRecord.PayrollStatus.CANCELLED) {
            throw new RuntimeException("Payroll record cannot be modified in current status");
        }
        
        payrollRecord.setStatus(status);
        payrollRecord = payrollRecordRepository.save(payrollRecord);
        
        log.info("Updated payroll {} status to {}", id, status);
        return convertToDto(payrollRecord);
    }
    
    @Override
    public PayrollRecordDto processPayroll(Long id, Long processedBy) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        
        if (payrollRecord.getStatus() != PayrollRecord.PayrollStatus.DRAFT) {
            throw new RuntimeException("Only draft payroll records can be processed");
        }
        
        payrollRecord.setStatus(PayrollRecord.PayrollStatus.PROCESSED);
        User processedByUser = userRepository.findById(processedBy).orElse(null);
        if (processedByUser != null) {
            payrollRecord.setGeneratedBy(processedByUser);
        }
        
        payrollRecord = payrollRecordRepository.save(payrollRecord);
        
        log.info("Processed payroll {} by user {}", id, processedBy);
        return convertToDto(payrollRecord);
    }
    
    @Override
    public PayrollRecordDto markPayrollAsPaid(Long id, Long paidBy) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        
        if (payrollRecord.getStatus() != PayrollRecord.PayrollStatus.PROCESSED &&
            payrollRecord.getStatus() != PayrollRecord.PayrollStatus.APPROVED) {
            throw new RuntimeException("Only processed or approved payroll records can be marked as paid");
        }
        
        payrollRecord.setStatus(PayrollRecord.PayrollStatus.PAID);
        payrollRecord.setPaidAt(LocalDateTime.now());
        
        payrollRecord = payrollRecordRepository.save(payrollRecord);
        
        log.info("Marked payroll {} as paid by user {}", id, paidBy);
        return convertToDto(payrollRecord);
    }
    
    @Override
    public List<PayrollRecordDto> bulkProcessPayroll(List<Long> payrollIds, Long processedBy) {
        return payrollIds.stream()
            .map(id -> processPayroll(id, processedBy))
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteDraftPayroll(Long id) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        
        if (payrollRecord.getStatus() != PayrollRecord.PayrollStatus.DRAFT) {
            throw new RuntimeException("Only draft payroll records can be deleted");
        }
        
        payrollRecordRepository.delete(payrollRecord);
        log.info("Deleted draft payroll record {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollSummaryDto getPayrollSummary(LocalDate startDate, LocalDate endDate) {
        Object[] summaryData = payrollRecordRepository.getPayrollSummary(startDate, endDate);
        List<Object[]> statusStats = payrollRecordRepository.getPayrollStatusStatistics(startDate, endDate);
        
        // Parse summary data
        Long totalRecords = ((Number) summaryData[0]).longValue();
        BigDecimal totalGrossPay = (BigDecimal) summaryData[1];
        BigDecimal totalNetPay = (BigDecimal) summaryData[2];
        BigDecimal totalDeductions = (BigDecimal) summaryData[3];
        BigDecimal totalWorkingHours = (BigDecimal) summaryData[4];
        
        // Parse status statistics
        Map<PayrollRecord.PayrollStatus, Long> statusCounts = statusStats.stream()
            .collect(Collectors.toMap(
                row -> (PayrollRecord.PayrollStatus) row[0],
                row -> ((Number) row[1]).longValue()
            ));
        
        Long draftRecords = statusCounts.getOrDefault(PayrollRecord.PayrollStatus.DRAFT, 0L);
        Long processedRecords = statusCounts.getOrDefault(PayrollRecord.PayrollStatus.PROCESSED, 0L);
        Long paidRecords = statusCounts.getOrDefault(PayrollRecord.PayrollStatus.PAID, 0L);
        
        return new PayrollSummaryDto(
            totalRecords,
            totalGrossPay,
            totalNetPay,
            totalDeductions,
            totalWorkingHours,
            draftRecords,
            processedRecords,
            paidRecords
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PayrollRecordDto> getStaffPayrollHistory(Long staffId, int months) {
        LocalDate since = LocalDate.now().minusMonths(months);
        List<PayrollRecord> payrollRecords = payrollRecordRepository
            .findStaffPayrollHistory(staffId, since);
        return payrollRecords.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] exportPayrollToExcel(LocalDate startDate, LocalDate endDate) {
        List<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByPayPeriodStartGreaterThanEqualAndPayPeriodEndLessThanEqualOrderByGeneratedAtDesc(
                startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Payroll Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Staff ID", "Staff Name", "Period Start", "Period End", 
                              "Working Hours", "Gross Pay", "Deductions", "Net Pay", "Status"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data rows
            int rowNum = 1;
            for (PayrollRecord record : payrollRecords) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getStaff().getId());
                row.createCell(1).setCellValue(record.getStaff().getFullName());
                row.createCell(2).setCellValue(record.getPayPeriodStart().toString());
                row.createCell(3).setCellValue(record.getPayPeriodEnd().toString());
                row.createCell(4).setCellValue(record.getTotalWorkingHours().doubleValue());
                row.createCell(5).setCellValue(record.getGrossPay().doubleValue());
                row.createCell(6).setCellValue(record.getTotalDeductions().doubleValue());
                row.createCell(7).setCellValue(record.getNetPay().doubleValue());
                row.createCell(8).setCellValue(record.getStatus().getDisplayName());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollReportDto generatePayrollReport(LocalDate startDate, LocalDate endDate) {
        List<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByPayPeriodStartGreaterThanEqualAndPayPeriodEndLessThanEqualOrderByGeneratedAtDesc(
                startDate, endDate);
        
        PayrollSummaryDto summary = getPayrollSummary(startDate, endDate);
        
        // Calculate violation impacts
        List<ViolationImpactDto> violationImpacts = calculateViolationImpacts(startDate, endDate);
        
        return new PayrollReportDto(
            startDate,
            endDate,
            payrollRecords.stream().map(this::convertToDto).collect(Collectors.toList()),
            summary,
            violationImpacts,
            LocalDate.now()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollPreviewDto previewPayroll(Long staffId, LocalDate startDate, LocalDate endDate) {
        User staff = userRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
        
        BigDecimal workingHours = calculateWorkingHours(staffId, startDate, endDate);
        BigDecimal teachingHours = calculateTeachingHours(staffId, startDate, endDate);
        BigDecimal deductions = calculateDeductions(staffId, startDate, endDate);
        
        // Get base salary and hourly rate (simplified calculation)
        BigDecimal baseSalary = getStaffBaseSalary(staff);
        BigDecimal hourlyRate = getStaffHourlyRate(staff);
        
        BigDecimal grossPay = baseSalary.add(hourlyRate.multiply(workingHours));
        BigDecimal netPay = grossPay.subtract(deductions);
        
        // Generate deduction details
        List<DeductionDetailDto> deductionDetails = generateDeductionDetails(staffId, startDate, endDate);
        
        // Generate working hours details
        List<WorkingHoursDetailDto> workingHoursDetails = generateWorkingHoursDetails(staffId, startDate, endDate);
        
        return new PayrollPreviewDto(
            staffId,
            staff.getFullName(),
            startDate,
            endDate,
            baseSalary,
            hourlyRate,
            workingHours,
            teachingHours,
            grossPay,
            deductions,
            netPay,
            deductionDetails,
            workingHoursDetails
        );
    }
    
    @Override
    public PayrollRecordDto recalculatePayroll(Long id) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        
        if (!payrollRecord.isEditable()) {
            throw new RuntimeException("Payroll record cannot be recalculated in current status");
        }
        
        // Recalculate all values
        PayrollRecord updatedRecord = createPayrollRecord(
            payrollRecord.getStaff(),
            payrollRecord.getPayPeriodStart(),
            payrollRecord.getPayPeriodEnd()
        );
        
        // Keep the original ID and metadata
        updatedRecord.setId(payrollRecord.getId());
        updatedRecord.setGeneratedBy(payrollRecord.getGeneratedBy());
        updatedRecord.setGeneratedAt(payrollRecord.getGeneratedAt());
        
        updatedRecord = payrollRecordRepository.save(updatedRecord);
        
        log.info("Recalculated payroll record {}", id);
        return convertToDto(updatedRecord);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MonthlyPayrollStatsDto getMonthlyPayrollStats(int year, int month) {
        Object[] statsData = payrollRecordRepository.getMonthlyPayrollStatistics(year, month);
        
        Long totalStaff = ((Number) statsData[0]).longValue();
        BigDecimal totalGrossPay = (BigDecimal) statsData[1];
        BigDecimal totalNetPay = (BigDecimal) statsData[2];
        BigDecimal totalDeductions = (BigDecimal) statsData[3];
        BigDecimal averageGrossPay = (BigDecimal) statsData[4];
        BigDecimal averageNetPay = (BigDecimal) statsData[5];
        BigDecimal totalWorkingHours = (BigDecimal) statsData[6];
        BigDecimal averageWorkingHours = (BigDecimal) statsData[7];
        
        return new MonthlyPayrollStatsDto(
            year,
            month,
            totalStaff,
            totalGrossPay,
            totalNetPay,
            totalDeductions,
            averageGrossPay,
            averageNetPay,
            totalWorkingHours,
            averageWorkingHours
        );
    }
    
    // Private helper methods
    
    private PayrollRecord createPayrollRecord(User staff, LocalDate startDate, LocalDate endDate) {
        PayrollRecord payrollRecord = new PayrollRecord();
        payrollRecord.setStaff(staff);
        payrollRecord.setPayPeriodStart(startDate);
        payrollRecord.setPayPeriodEnd(endDate);
        
        // Calculate values
        BigDecimal workingHours = calculateWorkingHours(staff.getId(), startDate, endDate);
        BigDecimal teachingHours = calculateTeachingHours(staff.getId(), startDate, endDate);
        BigDecimal baseSalary = getStaffBaseSalary(staff);
        BigDecimal hourlyRate = getStaffHourlyRate(staff);
        BigDecimal totalDeductions = calculateDeductions(staff.getId(), startDate, endDate);
        
        // Add tax and insurance deductions
        BigDecimal grossPay = baseSalary.add(hourlyRate.multiply(workingHours));
        BigDecimal taxDeduction = grossPay.multiply(TAX_RATE);
        BigDecimal insuranceDeduction = grossPay.multiply(INSURANCE_RATE);
        totalDeductions = totalDeductions.add(taxDeduction).add(insuranceDeduction);
        
        payrollRecord.setTotalWorkingHours(workingHours);
        payrollRecord.setTotalTeachingHours(teachingHours);
        payrollRecord.setBaseSalary(baseSalary);
        payrollRecord.setHourlyRate(hourlyRate);
        payrollRecord.setGrossPay(grossPay);
        payrollRecord.setTaxDeduction(taxDeduction);
        payrollRecord.setInsuranceDeduction(insuranceDeduction);
        payrollRecord.setTotalDeductions(totalDeductions);
        payrollRecord.setNetPay(grossPay.subtract(totalDeductions));
        payrollRecord.setStatus(PayrollRecord.PayrollStatus.DRAFT);
        
        return payrollRecord;
    }
    
    private BigDecimal getStaffBaseSalary(User staff) {
        // Simplified - in real implementation, this would come from staff profile or contract
        return new BigDecimal("5000000"); // 5 million VND base salary
    }
    
    private BigDecimal getStaffHourlyRate(User staff) {
        // Simplified - in real implementation, this would come from staff profile or contract
        if ("TEACHER".equals(staff.getRole())) {
            return new BigDecimal("100000"); // 100k VND per hour for teachers
        }
        return new BigDecimal("50000"); // 50k VND per hour for staff
    }
    
    private List<ViolationImpactDto> calculateViolationImpacts(LocalDate startDate, LocalDate endDate) {
        // Get all violations in the period
        List<AttendanceViolation> violations = violationRepository
            .findByViolationDateBetweenOrderByViolationDateDesc(startDate, endDate);
        
        // Group by staff and calculate impact
        Map<Long, List<AttendanceViolation>> violationsByStaff = violations.stream()
            .collect(Collectors.groupingBy(v -> v.getUser().getId()));
        
        return violationsByStaff.entrySet().stream()
            .map(entry -> {
                Long staffId = entry.getKey();
                List<AttendanceViolation> staffViolations = entry.getValue();
                User staff = staffViolations.get(0).getUser();
                
                BigDecimal totalDeduction = staffViolations.stream()
                    .filter(v -> !v.isResolved())
                    .map(this::getViolationDeductionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                String violationTypes = staffViolations.stream()
                    .map(v -> v.getViolationType().getDescription())
                    .distinct()
                    .collect(Collectors.joining(", "));
                
                return new ViolationImpactDto(
                    staffId,
                    staff.getFullName(),
                    staffViolations.size(),
                    totalDeduction,
                    violationTypes
                );
            })
            .collect(Collectors.toList());
    }
    
    private List<DeductionDetailDto> generateDeductionDetails(Long staffId, LocalDate startDate, LocalDate endDate) {
        List<DeductionDetailDto> details = new java.util.ArrayList<>();
        
        // Add violation deductions
        List<AttendanceViolation> violations = violationRepository
            .findByUserIdAndViolationDateBetweenOrderByViolationDateDesc(staffId, startDate, endDate);
        
        Map<AttendanceViolation.ViolationType, List<AttendanceViolation>> violationsByType = violations.stream()
            .filter(v -> !v.isResolved())
            .collect(Collectors.groupingBy(AttendanceViolation::getViolationType));
        
        violationsByType.forEach((type, typeViolations) -> {
            BigDecimal totalAmount = typeViolations.stream()
                .map(this::getViolationDeductionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            details.add(new DeductionDetailDto(
                type.getDescription(),
                typeViolations.size(),
                totalAmount,
                "Khấu trừ do vi phạm " + type.getDescription().toLowerCase()
            ));
        });
        
        return details;
    }
    
    private List<WorkingHoursDetailDto> generateWorkingHoursDetails(Long staffId, LocalDate startDate, LocalDate endDate) {
        List<StaffAttendanceLog> attendanceLogs = attendanceLogRepository
            .findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(staffId, startDate, endDate);
        
        return attendanceLogs.stream()
            .map(log -> {
                BigDecimal actualHours = BigDecimal.ZERO;
                if (log.getCheckInTime() != null && log.getCheckOutTime() != null) {
                    long minutes = java.time.Duration.between(log.getCheckInTime(), log.getCheckOutTime()).toMinutes();
                    actualHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                }
                
                return new WorkingHoursDetailDto(
                    log.getDate(),
                    log.getShiftAssignment() != null ? log.getShiftAssignment().getShift().getName() : "N/A",
                    log.getShiftAssignment() != null ? 
                        BigDecimal.valueOf(log.getShiftAssignment().getShift().getDurationHours()) : BigDecimal.ZERO,
                    actualHours,
                    log.getAttendanceStatus() != null ? log.getAttendanceStatus().toString() : "Unknown"
                );
            })
            .collect(Collectors.toList());
    }
    
    private PayrollRecordDto convertToDto(PayrollRecord payrollRecord) {
        return PayrollRecordDto.builder()
            .id(payrollRecord.getId())
            .staffId(payrollRecord.getStaff().getId())
            .staffName(payrollRecord.getStaff().getFullName())
            .payPeriodStart(payrollRecord.getPayPeriodStart())
            .payPeriodEnd(payrollRecord.getPayPeriodEnd())
            .totalWorkingHours(payrollRecord.getTotalWorkingHours())
            .totalTeachingHours(payrollRecord.getTotalTeachingHours())
            .baseSalary(payrollRecord.getBaseSalary())
            .hourlyRate(payrollRecord.getHourlyRate())
            .grossPay(payrollRecord.getGrossPay())
            .totalDeductions(payrollRecord.getTotalDeductions())
            .taxDeduction(payrollRecord.getTaxDeduction())
            .insuranceDeduction(payrollRecord.getInsuranceDeduction())
            .netPay(payrollRecord.getNetPay())
            .status(payrollRecord.getStatus())
            .generatedBy(payrollRecord.getGeneratedBy() != null ? payrollRecord.getGeneratedBy().getId() : null)
            .generatedAt(payrollRecord.getGeneratedAt())
            .paidAt(payrollRecord.getPaidAt())
            .build();
    }
}