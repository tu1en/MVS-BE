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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.dto.hrmanagement.PayrollRecordDto;
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
            .filter(user -> user.getRoleId() != null && 
                    user.getRoleId() != RoleConstants.STUDENT && 
                    user.getRoleId() != RoleConstants.ADMIN)
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
        User staff = userRepository.findById(staffId).orElse(null);
        if (staff != null && "TEACHER".equals(staff.getRole())) {
            return calculateWorkingHours(staffId, startDate, endDate);
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal calculateDeductions(Long staffId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceViolation> violations = violationRepository
            .findByUserIdAndViolationDateBetweenOrderByViolationDateDesc(staffId, startDate, endDate);
        
        BigDecimal violationDeductions = violations.stream()
            .filter(v -> !v.isResolved())
            .map(v -> getViolationDeductionAmount(v))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return violationDeductions;
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
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date và end date không được null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date phải trước end date");
        }
        
        List<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByPayPeriodStartGreaterThanEqualAndPayPeriodEndLessThanEqualOrderByGeneratedAtDesc(
                startDate, endDate);
        
        if (payrollRecords.isEmpty()) {
            log.warn("Không tìm thấy dữ liệu payroll từ {} đến {}", startDate, endDate);
        }
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Payroll Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Staff ID", "Staff Name", "Period Start", "Period End", 
                              "Working Hours", "Gross Pay", "Deductions", "Net Pay", "Status"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            // Create data rows
            int rowNum = 1;
            for (PayrollRecord record : payrollRecords) {
                try {
                    Row row = sheet.createRow(rowNum++);
                    
                    Cell idCell = row.createCell(0);
                    idCell.setCellValue(record.getStaff() != null ? record.getStaff().getId() : 0);
                    idCell.setCellStyle(dataStyle);
                    
                    Cell nameCell = row.createCell(1);
                    nameCell.setCellValue(record.getStaff() != null ? 
                        (record.getStaff().getFullName() != null ? record.getStaff().getFullName() : "N/A") : "N/A");
                    nameCell.setCellStyle(dataStyle);
                    
                    Cell startCell = row.createCell(2);
                    startCell.setCellValue(record.getPayPeriodStart() != null ? record.getPayPeriodStart().toString() : "");
                    startCell.setCellStyle(dataStyle);
                    
                    Cell endCell = row.createCell(3);
                    endCell.setCellValue(record.getPayPeriodEnd() != null ? record.getPayPeriodEnd().toString() : "");
                    endCell.setCellStyle(dataStyle);
                    
                    Cell hoursCell = row.createCell(4);
                    hoursCell.setCellValue(record.getTotalWorkingHours() != null ? record.getTotalWorkingHours().doubleValue() : 0.0);
                    hoursCell.setCellStyle(dataStyle);
                    
                    Cell grossCell = row.createCell(5);
                    grossCell.setCellValue(record.getGrossPay() != null ? record.getGrossPay().doubleValue() : 0.0);
                    grossCell.setCellStyle(dataStyle);
                    
                    Cell deductionsCell = row.createCell(6);
                    deductionsCell.setCellValue(record.getTotalDeductions() != null ? record.getTotalDeductions().doubleValue() : 0.0);
                    deductionsCell.setCellStyle(dataStyle);
                    
                    Cell netCell = row.createCell(7);
                    netCell.setCellValue(record.getNetPay() != null ? record.getNetPay().doubleValue() : 0.0);
                    netCell.setCellStyle(dataStyle);
                    
                    Cell statusCell = row.createCell(8);
                    statusCell.setCellValue(record.getStatus() != null ? record.getStatus().getDisplayName() : "Unknown");
                    statusCell.setCellStyle(dataStyle);
                    
                } catch (Exception e) {
                    log.warn("Lỗi xử lý payroll record ID {}: {}", record.getId(), e.getMessage());
                }
            }
            
            // Auto-size columns with limits
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
                if (currentWidth < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }
            
            workbook.write(out);
            byte[] result = out.toByteArray();
            
            log.info("Thành công export {} payroll records, kích thước file: {} bytes", 
                       payrollRecords.size(), result.length);
            
            return result;
            
        } catch (IOException e) {
            log.error("Lỗi tạo file Excel export: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo file Excel: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi export payroll: {}", e.getMessage());
            throw new RuntimeException("Lỗi export payroll: " + e.getMessage(), e);
        }
    }

    // NEW MISSING METHODS FROM INTERFACE
    
    @Override
    public PayrollReportDto generatePayrollReport(LocalDate startDate, LocalDate endDate) {
        List<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByPayPeriodStartGreaterThanEqualAndPayPeriodEndLessThanEqualOrderByGeneratedAtDesc(
                startDate, endDate);
        
        List<PayrollRecordDto> recordDtos = payrollRecords.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        PayrollSummaryDto summary = getPayrollSummary(startDate, endDate);
        
        // Mock violation impacts - replace with actual logic
        List<ViolationImpactDto> violationImpacts = List.of();
        
        return new PayrollReportDto(
            startDate,
            endDate,
            recordDtos,
            summary,
            violationImpacts,
            LocalDate.now()
        );
    }
    
    @Override
    public PayrollPreviewDto previewPayroll(Long staffId, LocalDate startDate, LocalDate endDate) {
        User staff = userRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
        
        BigDecimal workingHours = calculateWorkingHours(staffId, startDate, endDate);
        BigDecimal teachingHours = calculateTeachingHours(staffId, startDate, endDate);
        BigDecimal baseSalary = getStaffBaseSalary(staff);
        BigDecimal hourlyRate = getStaffHourlyRate(staff);
        BigDecimal totalDeductions = calculateDeductions(staffId, startDate, endDate);
        
        BigDecimal grossPay = baseSalary.add(hourlyRate.multiply(workingHours));
        BigDecimal taxDeduction = grossPay.multiply(TAX_RATE);
        BigDecimal insuranceDeduction = grossPay.multiply(INSURANCE_RATE);
        totalDeductions = totalDeductions.add(taxDeduction).add(insuranceDeduction);
        BigDecimal netPay = grossPay.subtract(totalDeductions);
        
        // Mock deduction and working hours details - replace with actual logic
        List<DeductionDetailDto> deductionDetails = List.of(
            new DeductionDetailDto("Tax", 1, taxDeduction, "Income tax"),
            new DeductionDetailDto("Insurance", 1, insuranceDeduction, "Social insurance")
        );
        
        List<WorkingHoursDetailDto> workingHoursDetails = List.of();
        
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
            totalDeductions,
            netPay,
            deductionDetails,
            workingHoursDetails
        );
    }
    
    @Override
    public PayrollRecordDto recalculatePayroll(Long id) {
        PayrollRecord payrollRecord = payrollRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll record not found with ID: " + id));
        
        if (payrollRecord.getStatus() != PayrollRecord.PayrollStatus.DRAFT) {
            throw new RuntimeException("Only draft payroll records can be recalculated");
        }
        
        // Recalculate values
        User staff = payrollRecord.getStaff();
        LocalDate startDate = payrollRecord.getPayPeriodStart();
        LocalDate endDate = payrollRecord.getPayPeriodEnd();
        
        BigDecimal workingHours = calculateWorkingHours(staff.getId(), startDate, endDate);
        BigDecimal teachingHours = calculateTeachingHours(staff.getId(), startDate, endDate);
        BigDecimal baseSalary = getStaffBaseSalary(staff);
        BigDecimal hourlyRate = getStaffHourlyRate(staff);
        BigDecimal totalDeductions = calculateDeductions(staff.getId(), startDate, endDate);
        
        BigDecimal grossPay = baseSalary.add(hourlyRate.multiply(workingHours));
        BigDecimal taxDeduction = grossPay.multiply(TAX_RATE);
        BigDecimal insuranceDeduction = grossPay.multiply(INSURANCE_RATE);
        totalDeductions = totalDeductions.add(taxDeduction).add(insuranceDeduction);
        
        // Update payroll record
        payrollRecord.setTotalWorkingHours(workingHours);
        payrollRecord.setTotalTeachingHours(teachingHours);
        payrollRecord.setBaseSalary(baseSalary);
        payrollRecord.setHourlyRate(hourlyRate);
        payrollRecord.setGrossPay(grossPay);
        payrollRecord.setTaxDeduction(taxDeduction);
        payrollRecord.setInsuranceDeduction(insuranceDeduction);
        payrollRecord.setTotalDeductions(totalDeductions);
        payrollRecord.setNetPay(grossPay.subtract(totalDeductions));
        
        payrollRecord = payrollRecordRepository.save(payrollRecord);
        
        log.info("Recalculated payroll record {}", id);
        return convertToDto(payrollRecord);
    }
    
    @Override
    public MonthlyPayrollStatsDto getMonthlyPayrollStats(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<PayrollRecord> payrollRecords = payrollRecordRepository
            .findByPayPeriodStartGreaterThanEqualAndPayPeriodEndLessThanEqualOrderByGeneratedAtDesc(
                startDate, endDate);
        
        Long totalStaff = (long) payrollRecords.size();
        BigDecimal totalGrossPay = payrollRecords.stream()
            .map(PayrollRecord::getGrossPay)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNetPay = payrollRecords.stream()
            .map(PayrollRecord::getNetPay)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeductions = payrollRecords.stream()
            .map(PayrollRecord::getTotalDeductions)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWorkingHours = payrollRecords.stream()
            .map(PayrollRecord::getTotalWorkingHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageGrossPay = totalStaff > 0 ? 
            totalGrossPay.divide(BigDecimal.valueOf(totalStaff), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal averageNetPay = totalStaff > 0 ? 
            totalNetPay.divide(BigDecimal.valueOf(totalStaff), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal averageWorkingHours = totalStaff > 0 ? 
            totalWorkingHours.divide(BigDecimal.valueOf(totalStaff), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
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
    
    private BigDecimal getViolationDeductionAmount(AttendanceViolation violation) {
        BigDecimal baseAmount = VIOLATION_DEDUCTION_RATE;
        
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
        return new BigDecimal("5000000"); // 5 million VND base salary
    }
    
    private BigDecimal getStaffHourlyRate(User staff) {
        if (staff.getRoleId() != null && staff.getRoleId() == RoleConstants.TEACHER) {
            return new BigDecimal("100000"); // 100k VND per hour for teachers
        }
        return new BigDecimal("50000"); // 50k VND per hour for other roles
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