package com.classroomapp.classroombackend.controller.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.Payroll;
import com.classroomapp.classroombackend.service.hrmanagement.SalaryCalculationService;
import com.classroomapp.classroombackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * REST Controller for salary and payroll operations
 */
@RestController
@RequestMapping("/api/hr/salary")
@RequiredArgsConstructor
@Slf4j
public class SalaryController {
    
    private final SalaryCalculationService salaryCalculationService;
    private final SecurityUtils securityUtils;
    
    /**
     * Calculate payroll for specific user and period
     * POST /api/hr/salary/calculate
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Payroll> calculatePayroll(
            @RequestParam Long userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Calculating payroll for user {} for period {}/{}", userId, month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            Payroll payroll = salaryCalculationService.calculatePayrollForUser(userId, period);
            
            return ResponseEntity.ok(payroll);
            
        } catch (Exception e) {
            log.error("Error calculating payroll for user {} and period {}/{}", userId, month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Calculate payroll for all users in period
     * POST /api/hr/salary/calculate-all
     */
    @PostMapping("/calculate-all")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Payroll>> calculatePayrollForPeriod(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Calculating payroll for all users for period {}/{}", month, year);
        
        try {
            YearMonth period = YearMonth.of(year, month);
            List<Payroll> payrolls = salaryCalculationService.calculatePayrollForPeriod(period);
            
            return ResponseEntity.ok(payrolls);
            
        } catch (Exception e) {
            log.error("Error calculating payroll for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Recalculate existing payroll
     * PUT /api/hr/salary/recalculate/{payrollId}
     */
    @PutMapping("/recalculate/{payrollId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Payroll> recalculatePayroll(@PathVariable Long payrollId) {
        
        log.info("Recalculating payroll: {}", payrollId);
        
        try {
            Payroll payroll = salaryCalculationService.recalculatePayroll(payrollId);
            return ResponseEntity.ok(payroll);
            
        } catch (Exception e) {
            log.error("Error recalculating payroll: {}", payrollId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payroll by ID
     * GET /api/hr/salary/payroll/{payrollId}
     */
    @GetMapping("/payroll/{payrollId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Payroll> getPayrollById(@PathVariable Long payrollId) {
        
        log.info("Getting payroll: {}", payrollId);
        
        try {
            Payroll payroll = salaryCalculationService.getPayrollById(payrollId);
            
            // Check if user can access this payroll
            Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
            String currentUserRole = securityUtils.getCurrentUserRole();
            
            if (!canAccessPayroll(payroll, currentUserId, currentUserRole)) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(payroll);
            
        } catch (Exception e) {
            log.error("Error getting payroll: {}", payrollId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payroll for user and period
     * GET /api/hr/salary/payroll/user/{userId}
     */
    @GetMapping("/payroll/user/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Payroll> getPayrollForUserAndPeriod(
            @PathVariable Long userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Getting payroll for user {} and period {}/{}", userId, month, year);
        
        try {
            // Check if user can access this data
            Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
            String currentUserRole = securityUtils.getCurrentUserRole();
            
            if (!canAccessUserPayroll(userId, currentUserId, currentUserRole)) {
                return ResponseEntity.status(403).build();
            }
            
            YearMonth period = YearMonth.of(year, month);
            Payroll payroll = salaryCalculationService.getPayrollForUserAndPeriod(userId, period);
            
            if (payroll == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(payroll);
            
        } catch (Exception e) {
            log.error("Error getting payroll for user {} and period {}/{}", userId, month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payrolls for current user
     * GET /api/hr/salary/my-payrolls
     */
    @GetMapping("/my-payrolls")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Payroll>> getMyPayrolls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "payrollYear") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting payrolls for current user: {}", currentUserId);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Payroll> payrolls = salaryCalculationService.getPayrollsForUser(currentUserId, pageable);
            
            return ResponseEntity.ok(payrolls);
            
        } catch (Exception e) {
            log.error("Error getting payrolls for user: {}", currentUserId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payrolls for period
     * GET /api/hr/salary/payrolls/period
     */
    @GetMapping("/payrolls/period")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Payroll>> getPayrollsForPeriod(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "user.fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Getting payrolls for period {}/{}", month, year);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            YearMonth period = YearMonth.of(year, month);
            Page<Payroll> payrolls = salaryCalculationService.getPayrollsForPeriod(period, pageable);
            
            return ResponseEntity.ok(payrolls);
            
        } catch (Exception e) {
            log.error("Error getting payrolls for period {}/{}", month, year, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payrolls by status
     * GET /api/hr/salary/payrolls/status/{status}
     */
    @GetMapping("/payrolls/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Payroll>> getPayrollsByStatus(
            @PathVariable Payroll.PayrollStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "payrollYear") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting payrolls by status: {}", status);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Payroll> payrolls = salaryCalculationService.getPayrollsByStatus(status, pageable);
            
            return ResponseEntity.ok(payrolls);
            
        } catch (Exception e) {
            log.error("Error getting payrolls by status: {}", status, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Approve payroll
     * PUT /api/hr/salary/payroll/{payrollId}/approve
     */
    @PutMapping("/payroll/{payrollId}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Payroll> approvePayroll(@PathVariable Long payrollId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Approving payroll {} by user {}", payrollId, currentUserId);
        
        try {
            Payroll payroll = salaryCalculationService.approvePayroll(payrollId, currentUserId);
            return ResponseEntity.ok(payroll);
            
        } catch (Exception e) {
            log.error("Error approving payroll: {}", payrollId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Approve multiple payrolls
     * PUT /api/hr/salary/payrolls/approve
     */
    @PutMapping("/payrolls/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Payroll>> approvePayrolls(@RequestBody List<Long> payrollIds) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Bulk approving {} payrolls by user {}", payrollIds.size(), currentUserId);
        
        try {
            List<Payroll> payrolls = salaryCalculationService.approvePayrolls(payrollIds, currentUserId);
            return ResponseEntity.ok(payrolls);
            
        } catch (Exception e) {
            log.error("Error bulk approving payrolls", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Mark payroll as paid
     * PUT /api/hr/salary/payroll/{payrollId}/paid
     */
    @PutMapping("/payroll/{payrollId}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payroll> markPayrollAsPaid(@PathVariable Long payrollId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Marking payroll {} as paid by user {}", payrollId, currentUserId);
        
        try {
            Payroll payroll = salaryCalculationService.markPayrollAsPaid(payrollId, currentUserId);
            return ResponseEntity.ok(payroll);
            
        } catch (Exception e) {
            log.error("Error marking payroll as paid: {}", payrollId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cancel payroll
     * DELETE /api/hr/salary/payroll/{payrollId}
     */
    @DeleteMapping("/payroll/{payrollId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelPayroll(@PathVariable Long payrollId) {
        
        log.info("Cancelling payroll: {}", payrollId);
        
        try {
            salaryCalculationService.cancelPayroll(payrollId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error cancelling payroll: {}", payrollId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Helper methods
    
    private boolean canAccessPayroll(Payroll payroll, Long currentUserId, String currentUserRole) {
        // Admin and Manager can access all payrolls
        if ("ADMIN".equals(currentUserRole) || "MANAGER".equals(currentUserRole)) {
            return true;
        }
        
        // Employee can only access their own payroll
        return payroll.getUser().getId().equals(currentUserId);
    }
    
    private boolean canAccessUserPayroll(Long userId, Long currentUserId, String currentUserRole) {
        // Admin and Manager can access all user payrolls
        if ("ADMIN".equals(currentUserRole) || "MANAGER".equals(currentUserRole)) {
            return true;
        }
        
        // Employee can only access their own payroll
        return userId.equals(currentUserId);
    }
}
