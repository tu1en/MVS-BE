package com.classroomapp.classroombackend.controller.hrmanagement;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftAssignmentService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftScheduleService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftSwapService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller cho Shift Management Reports
 * Cung cáº¥p cÃ¡c APIs cho bÃ¡o cÃ¡o vÃ  thá»‘ng kÃª shift management
 */
@RestController
@RequestMapping("/api/hr/shift-reports")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Shift Reports", description = "APIs cho bÃ¡o cÃ¡o vÃ  thá»‘ng kÃª shift management")
@SecurityRequirement(name = "bearerAuth")
public class ShiftReportController {

    private final ShiftTemplateService shiftTemplateService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftSwapService shiftSwapService;
    private final ShiftScheduleService shiftScheduleService;

    @Operation(summary = "Dashboard tá»•ng quan", 
               description = "Láº¥y thá»‘ng kÃª tá»•ng quan cho dashboard")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        log.info("Láº¥y dashboard statistics");

        Map<String, Object> dashboard = new HashMap<>();
        
        // Template statistics
        ShiftTemplateService.TemplateStatistics templateStats = shiftTemplateService.getTemplateStatistics();
        dashboard.put("templateStats", templateStats);
        
        // Assignment statistics for current month
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        ShiftAssignmentService.AssignmentStatistics assignmentStats = 
            shiftAssignmentService.getAssignmentStatistics(startOfMonth, endOfMonth);
        dashboard.put("assignmentStats", assignmentStats);
        
        // Swap statistics for current month
        LocalDateTime startOfMonthTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthTime = endOfMonth.atTime(23, 59, 59);
        ShiftSwapService.SwapStatistics swapStats = 
            shiftSwapService.getSwapStatistics(startOfMonthTime, endOfMonthTime);
        dashboard.put("swapStats", swapStats);
        
        // Schedule statistics for current month
        ShiftScheduleService.ScheduleStatistics scheduleStats = 
            shiftScheduleService.getScheduleStatistics(startOfMonth, endOfMonth);
        dashboard.put("scheduleStats", scheduleStats);

        return ResponseEntity.ok(ApiResponse.success(dashboard, "Láº¥y dashboard statistics thÃ nh cÃ´ng"));
    }

    @Operation(summary = "BÃ¡o cÃ¡o template usage", 
               description = "BÃ¡o cÃ¡o sá»­ dá»¥ng shift templates")
    @GetMapping("/template-usage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTemplateUsageReport() {
        log.info("Láº¥y template usage report");

        Map<String, Object> report = new HashMap<>();
        
        // Template statistics
        ShiftTemplateService.TemplateStatistics stats = shiftTemplateService.getTemplateStatistics();
        report.put("statistics", stats);
        
        // Most used templates
        List<?> mostUsed = shiftTemplateService.getMostUsedTemplates(10);
        report.put("mostUsedTemplates", mostUsed);
        
        // Templates with break time
        List<?> withBreak = shiftTemplateService.findTemplatesWithBreak();
        report.put("templatesWithBreak", withBreak);
        
        // Overtime eligible templates
        List<?> overtimeEligible = shiftTemplateService.findOvertimeEligibleTemplates();
        report.put("overtimeEligibleTemplates", overtimeEligible);

        return ResponseEntity.ok(ApiResponse.success(report, "Láº¥y template usage report thÃ nh cÃ´ng"));
    }

    @Operation(summary = "BÃ¡o cÃ¡o attendance", 
               description = "BÃ¡o cÃ¡o cháº¥m cÃ´ng vÃ  attendance")
    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceReport(
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "NgÃ y káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "ID nhÃ¢n viÃªn (optional)") @RequestParam(required = false) Long employeeId) {
        
        log.info("Láº¥y attendance report tá»« {} Ä‘áº¿n {} cho employee {}", startDate, endDate, employeeId);

        Map<String, Object> report = new HashMap<>();
        
        // Assignment statistics
        ShiftAssignmentService.AssignmentStatistics assignmentStats = 
            shiftAssignmentService.getAssignmentStatistics(startDate, endDate);
        report.put("assignmentStatistics", assignmentStats);
        
        // Overtime assignments
        List<?> overtimeAssignments = shiftAssignmentService.findOvertimeAssignments(startDate, endDate);
        report.put("overtimeAssignments", overtimeAssignments);
        
        // Attendance issues
        List<?> attendanceIssues = shiftAssignmentService.findAttendanceIssues(startDate, endDate);
        report.put("attendanceIssues", attendanceIssues);
        
        // Working hours summary for specific employee
        if (employeeId != null) {
            ShiftAssignmentService.WorkingHoursSummary workingHours = 
                shiftAssignmentService.calculateWorkingHours(employeeId, startDate, endDate);
            report.put("workingHoursSummary", workingHours);
        }

        return ResponseEntity.ok(ApiResponse.success(report, "Láº¥y attendance report thÃ nh cÃ´ng"));
    }

    @Operation(summary = "BÃ¡o cÃ¡o swap requests", 
               description = "BÃ¡o cÃ¡o yÃªu cáº§u Ä‘á»•i ca")
    @GetMapping("/swap-requests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSwapRequestsReport(
            @Parameter(description = "Thá»i gian báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "Thá»i gian káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Láº¥y swap requests report tá»« {} Ä‘áº¿n {}", startTime, endTime);

        Map<String, Object> report = new HashMap<>();
        
        // Swap statistics
        ShiftSwapService.SwapStatistics swapStats = shiftSwapService.getSwapStatistics(startTime, endTime);
        report.put("swapStatistics", swapStats);
        
        // Top requesters
        List<Object[]> topRequesters = shiftSwapService.findTopRequesters(startTime, endTime, 10);
        report.put("topRequesters", topRequesters);
        
        // Emergency requests
        List<?> emergencyRequests = shiftSwapService.findEmergencyRequests();
        report.put("emergencyRequests", emergencyRequests);
        
        // Pending manager approval
        List<?> pendingApproval = shiftSwapService.findPendingManagerApproval();
        report.put("pendingManagerApproval", pendingApproval);

        return ResponseEntity.ok(ApiResponse.success(report, "Láº¥y swap requests report thÃ nh cÃ´ng"));
    }

    @Operation(summary = "BÃ¡o cÃ¡o schedule performance", 
               description = "BÃ¡o cÃ¡o hiá»‡u suáº¥t lá»‹ch lÃ m viá»‡c")
    @GetMapping("/schedule-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSchedulePerformanceReport(
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "NgÃ y káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Láº¥y schedule performance report tá»« {} Ä‘áº¿n {}", startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        
        // Schedule statistics
        ShiftScheduleService.ScheduleStatistics scheduleStats = 
            shiftScheduleService.getScheduleStatistics(startDate, endDate);
        report.put("scheduleStatistics", scheduleStats);
        
        // Schedules with most assignments
        List<?> mostAssignments = shiftScheduleService.findSchedulesWithMostAssignments(10);
        report.put("schedulesWithMostAssignments", mostAssignments);
        
        // Active schedules
        List<?> activeSchedules = shiftScheduleService.findActiveSchedules();
        report.put("activeSchedules", activeSchedules);
        
        // Upcoming schedules
        List<?> upcomingSchedules = shiftScheduleService.findUpcomingSchedules(7);
        report.put("upcomingSchedules", upcomingSchedules);

        return ResponseEntity.ok(ApiResponse.success(report, "Láº¥y schedule performance report thÃ nh cÃ´ng"));
    }

    @Operation(summary = "BÃ¡o cÃ¡o payroll", 
               description = "BÃ¡o cÃ¡o tÃ­nh lÆ°Æ¡ng dá»±a trÃªn shift assignments")
    @GetMapping("/payroll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollReport(
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "NgÃ y káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "ID nhÃ¢n viÃªn (optional)") @RequestParam(required = false) Long employeeId) {
        
        log.info("Láº¥y payroll report tá»« {} Ä‘áº¿n {} cho employee {}", startDate, endDate, employeeId);

        Map<String, Object> report = new HashMap<>();
        
        if (employeeId != null) {
            // Individual employee payroll
            ShiftAssignmentService.WorkingHoursSummary workingHours = 
                shiftAssignmentService.calculateWorkingHours(employeeId, startDate, endDate);
            report.put("workingHoursSummary", workingHours);
            
            // Employee assignments in period
            List<?> assignments = shiftAssignmentService.findByEmployeeAndDateRange(employeeId, startDate, endDate);
            report.put("assignments", assignments);
        } else {
            // All employees payroll summary
            ShiftAssignmentService.AssignmentStatistics assignmentStats = 
                shiftAssignmentService.getAssignmentStatistics(startDate, endDate);
            report.put("assignmentStatistics", assignmentStats);
            
            // Overtime assignments for payroll calculation
            List<?> overtimeAssignments = shiftAssignmentService.findOvertimeAssignments(startDate, endDate);
            report.put("overtimeAssignments", overtimeAssignments);
        }

        return ResponseEntity.ok(ApiResponse.success(report, "Láº¥y payroll report thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Export assignment report", 
               description = "Export bÃ¡o cÃ¡o assignments ra file Excel/CSV")
    @GetMapping("/export/assignments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<byte[]> exportAssignmentReport(
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "NgÃ y káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Format file (excel/csv)") @RequestParam(defaultValue = "excel") String format) {
        
        log.info("Export assignment report tá»« {} Ä‘áº¿n {} format: {}", startDate, endDate, format);

        try {
            byte[] reportData = shiftAssignmentService.exportAssignments(startDate, endDate, format);
            
            String filename = String.format("assignment-report-%s-to-%s.%s", 
                                           startDate, endDate, format.equals("excel") ? "xlsx" : "csv");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(format.equals("excel") ? 
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") :
                MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Lá»—i khi export assignment report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Export swap requests report", 
               description = "Export bÃ¡o cÃ¡o swap requests ra file Excel/CSV")
    @GetMapping("/export/swap-requests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<byte[]> exportSwapRequestsReport(
            @Parameter(description = "Thá»i gian báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "Thá»i gian káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "Format file (excel/csv)") @RequestParam(defaultValue = "excel") String format) {
        
        log.info("Export swap requests report tá»« {} Ä‘áº¿n {} format: {}", startTime, endTime, format);

        try {
            byte[] reportData = shiftSwapService.exportSwapRequests(startTime, endTime, format);
            
            String filename = String.format("swap-requests-report-%s-to-%s.%s", 
                                           startTime.toLocalDate(), endTime.toLocalDate(), 
                                           format.equals("excel") ? "xlsx" : "csv");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(format.equals("excel") ? 
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") :
                MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Lá»—i khi export swap requests report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Real-time metrics", 
               description = "Láº¥y metrics real-time cho monitoring")
    @GetMapping("/metrics/realtime")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealtimeMetrics() {
        log.info("Láº¥y realtime metrics");

        Map<String, Object> metrics = new HashMap<>();
        
        // Current day metrics
        LocalDate today = LocalDate.now();
        
        // Today's assignments
        List<?> todayAssignments = shiftAssignmentService.findByDate(today);
        metrics.put("todayAssignments", todayAssignments.size());
        
        // Pending check-ins
        List<?> pendingCheckIns = shiftAssignmentService.findPendingCheckIns();
        metrics.put("pendingCheckIns", pendingCheckIns.size());
        
        // Pending check-outs
        List<?> pendingCheckOuts = shiftAssignmentService.findPendingCheckOuts();
        metrics.put("pendingCheckOuts", pendingCheckOuts.size());
        
        // Pending manager approvals
        List<?> pendingApprovals = shiftSwapService.findPendingManagerApproval();
        metrics.put("pendingManagerApprovals", pendingApprovals.size());
        
        // Emergency swap requests
        List<?> emergencyRequests = shiftSwapService.findEmergencyRequests();
        metrics.put("emergencySwapRequests", emergencyRequests.size());
        
        // Active schedules
        List<?> activeSchedules = shiftScheduleService.findActiveSchedules();
        metrics.put("activeSchedules", activeSchedules.size());

        return ResponseEntity.ok(ApiResponse.success(metrics, "Láº¥y realtime metrics thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Health check cho shift management", 
               description = "Kiá»ƒm tra tÃ¬nh tráº¡ng hoáº¡t Ä‘á»™ng cá»§a shift management system")
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealthCheck() {
        log.info("Shift management health check");

        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check template service
            ShiftTemplateService.TemplateStatistics templateStats = shiftTemplateService.getTemplateStatistics();
            health.put("templateService", "OK");
            health.put("activeTemplates", templateStats.getActiveCount());
            
            // Check assignment service
            LocalDate today = LocalDate.now();
            List<?> todayAssignments = shiftAssignmentService.findByDate(today);
            health.put("assignmentService", "OK");
            health.put("todayAssignments", todayAssignments.size());
            
            // Check swap service
            List<?> pendingSwaps = shiftSwapService.findPendingManagerApproval();
            health.put("swapService", "OK");
            health.put("pendingSwaps", pendingSwaps.size());
            
            // Check schedule service
            List<?> activeSchedules = shiftScheduleService.findActiveSchedules();
            health.put("scheduleService", "OK");
            health.put("activeSchedules", activeSchedules.size());
            
            health.put("overallStatus", "HEALTHY");
            
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            health.put("overallStatus", "UNHEALTHY");
            health.put("error", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(health, "Health check completed"));
    }
}
