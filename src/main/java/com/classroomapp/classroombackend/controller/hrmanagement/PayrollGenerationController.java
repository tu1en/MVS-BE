package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollResult;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.hrmanagement.PayrollGenerationService;

import lombok.extern.slf4j.Slf4j;
import com.classroomapp.classroombackend.util.TopCVCalculation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller for payroll generation using TopCV calculations
 * Only accessible by accountants and managers
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payroll Generation", description = "Generate payroll using TopCV calculations and contract data")
public class PayrollGenerationController {
    
    private final PayrollGenerationService payrollGenerationService;
    private final EmailService emailService;
    
    /**
     * Generate payroll for a specific user
     */
    @PostMapping("/generate/user/{userId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Generate payroll for specific user")
    public ResponseEntity<PayrollResult> generatePayrollForUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Payroll period (YYYY-MM)") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        
        log.info("Generating payroll for user {} for period {}", userId, period);
        
        try {
            PayrollResult result = payrollGenerationService.generatePayrollForUser(userId, period);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating payroll for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Send payroll confirmation email to a specific user for a period
     */
    @PostMapping("/send-confirmation/user/{userId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Send payroll confirmation email to user for period")
    public ResponseEntity<?> sendPayrollConfirmationToUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Payroll period (YYYY-MM)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        try {
            PayrollResult result = payrollGenerationService.generatePayrollForUser(userId, period);
            if (result.getUserEmail() == null || result.getUserEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "User has no email"));
            }
            emailService.sendPayrollConfirmationEmail(result.getUserEmail(), result.getUserName(), result);
            return ResponseEntity.ok(Map.of("status", "SENT"));
        } catch (Exception e) {
            log.error("Failed to send payroll confirmation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Send payroll confirmations to all employees for period
     */
    @PostMapping("/send-confirmation/all")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Send payroll confirmation emails to all employees for period")
    public ResponseEntity<Map<String, Object>> sendPayrollConfirmationToAll(
            @Parameter(description = "Payroll period (YYYY-MM)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        List<PayrollResult> results = payrollGenerationService.generatePayrollForAllEmployees(period);
        int sent = 0;
        for (PayrollResult r : results) {
            try {
                if (r.getUserEmail() != null && !r.getUserEmail().isEmpty()) {
                    emailService.sendPayrollConfirmationEmail(r.getUserEmail(), r.getUserName(), r);
                    sent++;
                }
            } catch (Exception ex) {
                log.warn("Could not send payroll confirmation to {}: {}", r.getUserEmail(), ex.getMessage());
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("period", period.toString());
        resp.put("totalEmployees", results.size());
        resp.put("emailsSent", sent);
        return ResponseEntity.ok(resp);
    }
    /**
     * Generate payroll for all employees
     */
    @PostMapping("/generate/all")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Generate payroll for all employees")
    public ResponseEntity<Map<String, Object>> generatePayrollForAllEmployees(
            @Parameter(description = "Payroll period (YYYY-MM)") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        
        log.info("Generating payroll for all employees for period {}", period);
        
        try {
            List<PayrollResult> results = payrollGenerationService.generatePayrollForAllEmployees(period);
            
            Map<String, Object> response = new HashMap<>();
            response.put("period", period.toString());
            response.put("totalEmployees", results.size());
            response.put("payrollResults", results);
            
            // Calculate summary statistics
            double totalGross = results.stream()
                .mapToDouble(r -> r.getProratedGrossSalary().doubleValue())
                .sum();
            double totalNet = results.stream()
                .mapToDouble(r -> r.getNetSalary().doubleValue())
                .sum();
            
            response.put("totalGrossSalary", totalGross);
            response.put("totalNetSalary", totalNet);
            response.put("totalTaxAndInsurance", totalGross - totalNet);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating payroll for all employees: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generate payroll by contract type (TEACHER, ACCOUNTANT, etc.)
     */
    @PostMapping("/generate/contract-type/{contractType}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Generate payroll by contract type")
    public ResponseEntity<Map<String, Object>> generatePayrollByContractType(
            @Parameter(description = "Contract type") @PathVariable String contractType,
            @Parameter(description = "Payroll period (YYYY-MM)") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        
        log.info("Generating payroll for contract type {} for period {}", contractType, period);
        
        try {
            List<PayrollResult> results = payrollGenerationService.generatePayrollByContractType(contractType, period);
            
            Map<String, Object> response = new HashMap<>();
            response.put("period", period.toString());
            response.put("contractType", contractType);
            response.put("totalEmployees", results.size());
            response.put("payrollResults", results);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating payroll for contract type {}: {}", contractType, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get payroll history for a user
     */
    @GetMapping("/history/user/{userId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payroll history for user")
    public ResponseEntity<List<PayrollResult>> getPayrollHistory(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "From period (YYYY-MM)") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth fromPeriod,
            @Parameter(description = "To period (YYYY-MM)") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth toPeriod) {
        
        log.info("Getting payroll history for user {} from {} to {}", userId, fromPeriod, toPeriod);
        
        try {
            List<PayrollResult> history = payrollGenerationService.getPayrollHistory(userId, fromPeriod, toPeriod);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting payroll history for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Preview salary calculation (without saving)
     */
    @GetMapping("/preview/user/{userId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Preview salary calculation for user")
    public ResponseEntity<PayrollResult> previewPayrollForUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Payroll period (YYYY-MM)") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        
        log.info("Previewing payroll for user {} for period {}", userId, period);
        
        try {
            PayrollResult result = payrollGenerationService.generatePayrollForUser(userId, period);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error previewing payroll for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get TopCV calculation example
     */
    @GetMapping("/topcv-example")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get TopCV salary calculation example")
    public ResponseEntity<TopCVCalculation.SalaryCalculationResult> getTopCVExample() {
        log.info("Getting TopCV calculation example");
        
        try {
            TopCVCalculation.SalaryCalculationResult example = TopCVCalculation.calculateExample();
            return ResponseEntity.ok(example);
        } catch (Exception e) {
            log.error("Error getting TopCV example: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}