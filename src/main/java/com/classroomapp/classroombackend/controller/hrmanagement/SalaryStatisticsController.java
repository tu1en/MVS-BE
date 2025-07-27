package com.classroomapp.classroombackend.controller.hrmanagement;

import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService;
import com.classroomapp.classroombackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * REST Controller for salary statistics and analytics
 */
@RestController
@RequestMapping("/api/hr/salary/statistics")
@RequiredArgsConstructor
@Slf4j
public class SalaryStatisticsController {
    
    private final SalaryCalculationService salaryCalculationService;
    private final SecurityUtils securityUtils;
    
    /**
     * Get payroll statistics for period
     * GET /api/hr/salary/statistics/period
     */
    @GetMapping("/period")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SalaryCalculationService.PayrollStatistics> getPayrollStatisticsForPeriod(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Getting payroll statistics for period {}/{}", month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            SalaryCalculationService.PayrollStatistics statistics = 
                salaryCalculationService.getPayrollStatisticsForPeriod(period);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error getting payroll statistics for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get yearly payroll summary for user
     * GET /api/hr/salary/statistics/yearly/{userId}
     */
    @GetMapping("/yearly/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SalaryCalculationService.MonthlyPayrollSummary>> getYearlyPayrollSummary(
            @PathVariable Long userId,
            @RequestParam Integer year) {
        
        log.info("Getting yearly payroll summary for user {} and year {}", userId, year);
        
        try {
            // Check if user can access this data
            Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
            String currentUserRole = securityUtils.getCurrentUserRole();
            
            if (!canAccessUserData(userId, currentUserId, currentUserRole)) {
                return ResponseEntity.status(403).build();
            }
            
            List<SalaryCalculationService.MonthlyPayrollSummary> summary = 
                salaryCalculationService.getYearlyPayrollSummary(userId, year);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting yearly payroll summary for user {} and year {}", userId, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get yearly payroll summary for current user
     * GET /api/hr/salary/statistics/my-yearly
     */
    @GetMapping("/my-yearly")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SalaryCalculationService.MonthlyPayrollSummary>> getMyYearlyPayrollSummary(
            @RequestParam Integer year) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting yearly payroll summary for current user {} and year {}", currentUserId, year);
        
        try {
            List<SalaryCalculationService.MonthlyPayrollSummary> summary = 
                salaryCalculationService.getYearlyPayrollSummary(currentUserId, year);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting yearly payroll summary for user {} and year {}", currentUserId, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get department payroll summary
     * GET /api/hr/salary/statistics/department
     */
    @GetMapping("/department")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SalaryCalculationService.DepartmentPayrollSummary>> getDepartmentPayrollSummary(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Getting department payroll summary for period {}/{}", month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            List<SalaryCalculationService.DepartmentPayrollSummary> summary = 
                salaryCalculationService.getDepartmentPayrollSummary(period);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting department payroll summary for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get top earners for period
     * GET /api/hr/salary/statistics/top-earners
     */
    @GetMapping("/top-earners")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<com.classroomapp.classroombackend.model.hrmanagement.Payroll>> getTopEarners(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting top {} earners for period {}/{}", limit, month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            List<com.classroomapp.classroombackend.model.hrmanagement.Payroll> topEarners = 
                salaryCalculationService.getTopEarners(period, limit);
            
            return ResponseEntity.ok(topEarners);
            
        } catch (Exception e) {
            log.error("Error getting top earners for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payroll comparison between periods
     * GET /api/hr/salary/statistics/comparison
     */
    @GetMapping("/comparison")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SalaryCalculationService.PayrollComparison>> getPayrollComparison(
            @RequestParam Integer currentYear,
            @RequestParam Integer currentMonth,
            @RequestParam Integer previousYear,
            @RequestParam Integer previousMonth) {
        
        log.info("Getting payroll comparison between {}/{} and {}/{}", 
                currentMonth, currentYear, previousMonth, previousYear);
        
        try {
            YearMonth currentPeriod = YearMonth.of(currentYear, currentMonth);
            YearMonth previousPeriod = YearMonth.of(previousYear, previousMonth);
            
            List<SalaryCalculationService.PayrollComparison> comparison = 
                salaryCalculationService.getPayrollComparison(currentPeriod, previousPeriod);
            
            return ResponseEntity.ok(comparison);
            
        } catch (Exception e) {
            log.error("Error getting payroll comparison between {}/{} and {}/{}", 
                    currentMonth, currentYear, previousMonth, previousYear, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get monthly payroll trends
     * GET /api/hr/salary/statistics/trends
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SalaryCalculationService.MonthlyPayrollTrend>> getMonthlyPayrollTrends(
            @RequestParam(defaultValue = "12") int months) {
        
        log.info("Getting monthly payroll trends for last {} months", months);
        
        try {
            List<SalaryCalculationService.MonthlyPayrollTrend> trends = 
                salaryCalculationService.getMonthlyPayrollTrends(months);
            
            return ResponseEntity.ok(trends);
            
        } catch (Exception e) {
            log.error("Error getting monthly payroll trends for {} months", months, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Validate payroll calculation
     * GET /api/hr/salary/statistics/validate/{payrollId}
     */
    @GetMapping("/validate/{payrollId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SalaryCalculationService.PayrollValidationResult> validatePayroll(
            @PathVariable Long payrollId) {
        
        log.info("Validating payroll: {}", payrollId);
        
        try {
            SalaryCalculationService.PayrollValidationResult result = 
                salaryCalculationService.validatePayroll(payrollId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error validating payroll: {}", payrollId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payroll processing status for period
     * GET /api/hr/salary/statistics/processing-status
     */
    @GetMapping("/processing-status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SalaryCalculationService.PayrollProcessingStatus> getPayrollProcessingStatus(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Getting payroll processing status for period {}/{}", month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            SalaryCalculationService.PayrollProcessingStatus status = 
                salaryCalculationService.getPayrollProcessingStatus(period);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error getting payroll processing status for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Process bulk payroll calculation
     * POST /api/hr/salary/statistics/bulk-calculate
     */
    @PostMapping("/bulk-calculate")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SalaryCalculationService.BulkPayrollResult> processBulkPayrollCalculation(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestBody List<Long> userIds) {
        
        log.info("Processing bulk payroll calculation for {} users in period {}/{}", 
                userIds.size(), month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            SalaryCalculationService.BulkPayrollResult result = 
                salaryCalculationService.processBulkPayrollCalculation(period, userIds);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing bulk payroll calculation for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get attendance summary for payroll calculation
     * GET /api/hr/salary/statistics/attendance-summary/{userId}
     */
    @GetMapping("/attendance-summary/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SalaryCalculationService.AttendanceSummary> getAttendanceSummaryForPayroll(
            @PathVariable Long userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Getting attendance summary for user {} and period {}/{}", userId, month, year);
        
        try {
            // Check if user can access this data
            Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
            String currentUserRole = securityUtils.getCurrentUserRole();
            
            if (!canAccessUserData(userId, currentUserId, currentUserRole)) {
                return ResponseEntity.status(403).build();
            }
            
            YearMonth period = YearMonth.of(year, month);
            SalaryCalculationService.AttendanceSummary summary = 
                salaryCalculationService.getAttendanceSummaryForPayroll(userId, period);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting attendance summary for user {} and period {}/{}", userId, month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get attendance summary for current user
     * GET /api/hr/salary/statistics/my-attendance-summary
     */
    @GetMapping("/my-attendance-summary")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SalaryCalculationService.AttendanceSummary> getMyAttendanceSummaryForPayroll(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting attendance summary for current user {} and period {}/{}", currentUserId, month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            SalaryCalculationService.AttendanceSummary summary = 
                salaryCalculationService.getAttendanceSummaryForPayroll(currentUserId, period);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting attendance summary for user {} and period {}/{}", currentUserId, month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Helper methods
    
    private boolean canAccessUserData(Long userId, Long currentUserId, String currentUserRole) {
        // Admin and Manager can access all user data
        if ("ADMIN".equals(currentUserRole) || "MANAGER".equals(currentUserRole)) {
            return true;
        }
        
        // Employee can only access their own data
        return userId.equals(currentUserId);
    }
}
