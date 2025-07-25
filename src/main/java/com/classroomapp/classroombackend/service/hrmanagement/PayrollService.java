package com.classroomapp.classroombackend.service.hrmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.hrmanagement.PayrollRecordDto;
import com.classroomapp.classroombackend.model.hrmanagement.PayrollRecord;

/**
 * Service interface for payroll management and calculation
 */
public interface PayrollService {
    
    /**
     * Generate payroll for a single staff member for a period
     * @param staffId the staff member ID
     * @param startDate the pay period start date
     * @param endDate the pay period end date
     * @return generated payroll record
     */
    PayrollRecordDto generatePayroll(Long staffId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate payroll for all staff for a period
     * @param startDate the pay period start date
     * @param endDate the pay period end date
     * @return list of generated payroll records
     */
    List<PayrollRecordDto> generateBulkPayroll(LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate working hours for a staff member in a period
     * @param staffId the staff member ID
     * @param startDate the period start date
     * @param endDate the period end date
     * @return total working hours
     */
    BigDecimal calculateWorkingHours(Long staffId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate teaching hours for a staff member in a period
     * @param staffId the staff member ID
     * @param startDate the period start date
     * @param endDate the period end date
     * @return total teaching hours
     */
    BigDecimal calculateTeachingHours(Long staffId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate deductions based on violations and absences
     * @param staffId the staff member ID
     * @param startDate the period start date
     * @param endDate the period end date
     * @return total deductions
     */
    BigDecimal calculateDeductions(Long staffId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get payroll record by ID
     * @param id payroll record ID
     * @return payroll record
     */
    PayrollRecordDto getPayrollById(Long id);
    
    /**
     * Get payroll records by staff ID
     * @param staffId the staff member ID
     * @param pageable pagination parameters
     * @return page of payroll records
     */
    Page<PayrollRecordDto> getPayrollByStaff(Long staffId, Pageable pageable);
    
    /**
     * Get payroll records for a period
     * @param startDate the period start date
     * @param endDate the period end date
     * @param pageable pagination parameters
     * @return page of payroll records
     */
    Page<PayrollRecordDto> getPayrollByPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Get payroll records by status
     * @param status the payroll status
     * @param pageable pagination parameters
     * @return page of payroll records
     */
    Page<PayrollRecordDto> getPayrollByStatus(PayrollRecord.PayrollStatus status, Pageable pageable);
    
    /**
     * Update payroll status
     * @param id payroll record ID
     * @param status new status
     * @return updated payroll record
     */
    PayrollRecordDto updatePayrollStatus(Long id, PayrollRecord.PayrollStatus status);
    
    /**
     * Process payroll (mark as processed)
     * @param id payroll record ID
     * @param processedBy user ID who processed
     * @return updated payroll record
     */
    PayrollRecordDto processPayroll(Long id, Long processedBy);
    
    /**
     * Mark payroll as paid
     * @param id payroll record ID
     * @param paidBy user ID who marked as paid
     * @return updated payroll record
     */
    PayrollRecordDto markPayrollAsPaid(Long id, Long paidBy);
    
    /**
     * Bulk process payroll records
     * @param payrollIds list of payroll record IDs
     * @param processedBy user ID who processed
     * @return list of updated payroll records
     */
    List<PayrollRecordDto> bulkProcessPayroll(List<Long> payrollIds, Long processedBy);
    
    /**
     * Delete draft payroll record
     * @param id payroll record ID
     */
    void deleteDraftPayroll(Long id);
    
    /**
     * Get payroll summary for a period
     * @param startDate the period start date
     * @param endDate the period end date
     * @return payroll summary
     */
    PayrollSummaryDto getPayrollSummary(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get staff payroll history
     * @param staffId the staff member ID
     * @param months number of months to look back
     * @return list of payroll records
     */
    List<PayrollRecordDto> getStaffPayrollHistory(Long staffId, int months);
    
    /**
     * Export payroll data to Excel
     * @param startDate the period start date
     * @param endDate the period end date
     * @return Excel file as byte array
     */
    byte[] exportPayrollToExcel(LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate payroll report for management
     * @param startDate the period start date
     * @param endDate the period end date
     * @return detailed payroll report
     */
    PayrollReportDto generatePayrollReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Preview payroll calculation before generation
     * @param staffId the staff member ID
     * @param startDate the pay period start date
     * @param endDate the pay period end date
     * @return payroll preview
     */
    PayrollPreviewDto previewPayroll(Long staffId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Recalculate existing payroll record
     * @param id payroll record ID
     * @return recalculated payroll record
     */
    PayrollRecordDto recalculatePayroll(Long id);
    
    /**
     * Get monthly payroll statistics
     * @param year the year
     * @param month the month
     * @return monthly statistics
     */
    MonthlyPayrollStatsDto getMonthlyPayrollStats(int year, int month);
    
    /**
     * DTO for payroll summary
     */
    record PayrollSummaryDto(
        Long totalRecords,
        BigDecimal totalGrossPay,
        BigDecimal totalNetPay,
        BigDecimal totalDeductions,
        BigDecimal totalWorkingHours,
        Long draftRecords,
        Long processedRecords,
        Long paidRecords
    ) {}
    
    /**
     * DTO for payroll report
     */
    record PayrollReportDto(
        LocalDate periodStart,
        LocalDate periodEnd,
        List<PayrollRecordDto> payrollRecords,
        PayrollSummaryDto summary,
        List<ViolationImpactDto> violationImpacts,
        LocalDate generatedAt
    ) {}
    
    /**
     * DTO for violation impact on payroll
     */
    record ViolationImpactDto(
        Long staffId,
        String staffName,
        Integer violationCount,
        BigDecimal totalDeduction,
        String violationTypes
    ) {}
    
    /**
     * DTO for payroll preview
     */
    record PayrollPreviewDto(
        Long staffId,
        String staffName,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal baseSalary,
        BigDecimal hourlyRate,
        BigDecimal totalWorkingHours,
        BigDecimal totalTeachingHours,
        BigDecimal grossPay,
        BigDecimal totalDeductions,
        BigDecimal netPay,
        List<DeductionDetailDto> deductionDetails,
        List<WorkingHoursDetailDto> workingHoursDetails
    ) {}
    
    /**
     * DTO for deduction details
     */
    record DeductionDetailDto(
        String type,
        Integer count,
        BigDecimal amount,
        String description
    ) {}
    
    /**
     * DTO for working hours details
     */
    record WorkingHoursDetailDto(
        LocalDate date,
        String shiftName,
        BigDecimal scheduledHours,
        BigDecimal actualHours,
        String status
    ) {}
    
    /**
     * DTO for monthly payroll statistics
     */
    record MonthlyPayrollStatsDto(
        int year,
        int month,
        Long totalStaff,
        BigDecimal totalGrossPay,
        BigDecimal totalNetPay,
        BigDecimal totalDeductions,
        BigDecimal averageGrossPay,
        BigDecimal averageNetPay,
        BigDecimal totalWorkingHours,
        BigDecimal averageWorkingHours
    ) {}
}