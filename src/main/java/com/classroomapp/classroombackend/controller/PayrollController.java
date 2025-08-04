package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.hrmanagement.PayrollRecordDto;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.service.hrmanagement.PayrollService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for payroll management
 * Handles payroll generation, calculation, and reporting
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payroll Management", description = "APIs for payroll calculation and management")
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping("/period")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payroll records for a specific period")
    public ResponseEntity<Page<PayrollRecordDto>> getPayrollByPeriod(
            @Parameter(description = "Start date of the period (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of the period (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "payPeriodStart") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PayrollRecordDto> payrollRecords = payrollService.getPayrollByPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(payrollRecords);
    }

    @GetMapping("/month/{year}/{month}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payroll records for a specific month")
    public ResponseEntity<Page<PayrollRecordDto>> getPayrollByMonth(
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("payPeriodStart").descending());

        Page<PayrollRecordDto> payrollRecords = payrollService.getPayrollByPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(payrollRecords);
    }

    @PostMapping("/generate/bulk")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @Operation(summary = "Generate payroll for all eligible staff for a period")
    public ResponseEntity<List<PayrollRecordDto>> generateBulkPayroll(
            @Parameter(description = "Start date of the pay period")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of the pay period") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("User {} generating bulk payroll for period {} to {}", 
                userDetails.getUsername(), startDate, endDate);

        List<PayrollRecordDto> payrollRecords = payrollService.generateBulkPayroll(startDate, endDate);
        return ResponseEntity.ok(payrollRecords);
    }

    @PostMapping("/generate/{staffId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @Operation(summary = "Generate payroll for a specific staff member")
    public ResponseEntity<PayrollRecordDto> generatePayroll(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PayrollRecordDto payrollRecord = payrollService.generatePayroll(staffId, startDate, endDate);
        return ResponseEntity.ok(payrollRecord);
    }

    @GetMapping("/preview/{staffId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @Operation(summary = "Preview payroll calculation for a staff member")
    public ResponseEntity<PayrollService.PayrollPreviewDto> previewPayroll(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PayrollService.PayrollPreviewDto preview = payrollService.previewPayroll(staffId, startDate, endDate);
        return ResponseEntity.ok(preview);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payroll summary for a period")
    public ResponseEntity<PayrollService.PayrollSummaryDto> getPayrollSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PayrollService.PayrollSummaryDto summary = payrollService.getPayrollSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payroll records for a specific staff member")
    public ResponseEntity<Page<PayrollRecordDto>> getPayrollByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("payPeriodStart").descending());

        Page<PayrollRecordDto> payrollRecords = payrollService.getPayrollByStaff(staffId, pageable);
        return ResponseEntity.ok(payrollRecords);
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @Operation(summary = "Process a payroll record (mark as processed)")
    public ResponseEntity<PayrollRecordDto> processPayroll(
            @PathVariable Long id,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        PayrollRecordDto payrollRecord = payrollService.processPayroll(id, userDetails.getId());
        return ResponseEntity.ok(payrollRecord);
    }

    @PostMapping("/{id}/paid")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @Operation(summary = "Mark payroll record as paid")
    public ResponseEntity<PayrollRecordDto> markAsPaid(
            @PathVariable Long id,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        PayrollRecordDto payrollRecord = payrollService.markPayrollAsPaid(id, userDetails.getId());
        return ResponseEntity.ok(payrollRecord);
    }

    @GetMapping("/stats/{year}/{month}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get monthly payroll statistics")
    public ResponseEntity<PayrollService.MonthlyPayrollStatsDto> getMonthlyStats(
            @PathVariable int year,
            @PathVariable int month) {

        PayrollService.MonthlyPayrollStatsDto stats = payrollService.getMonthlyPayrollStats(year, month);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Generate detailed payroll report")
    public ResponseEntity<PayrollService.PayrollReportDto> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PayrollService.PayrollReportDto report = payrollService.generatePayrollReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Export payroll data to Excel")
    public ResponseEntity<byte[]> exportPayrollToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] excelData = payrollService.exportPayrollToExcel(startDate, endDate);
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=payroll_report.xlsx")
                .body(excelData);
    }
}