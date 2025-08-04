package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.service.hrmanagement.AttendanceViolationService;
import com.classroomapp.classroombackend.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Attendance Violation management
 */
@RestController
@RequestMapping("/api/hr/violations")
@RequiredArgsConstructor
@Slf4j
public class AttendanceViolationController {

    private final AttendanceViolationService violationService;
    private final SecurityUtils securityUtils;
    
    /**
     * Get violation by ID
     * GET /api/hr/violations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceViolationDto> getViolationById(@PathVariable Long id) {
        log.info("Getting violation by ID: {}", id);
        
        // Check if current user can view this violation
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId != null && !violationService.canViewViolation(id, currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        AttendanceViolationDto violation = violationService.getViolationById(id);
        return ResponseEntity.ok(violation);
    }
    
    /**
     * Get violations by current user
     * GET /api/hr/violations/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceViolationDto>> getMyViolations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "violationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting violations for current user - page: {}, size: {}", page, size);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AttendanceViolationDto> violations = violationService.getViolationsByUser(currentUserId, pageable);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get violations by user (for managers/admins)
     * GET /api/hr/violations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceViolationDto>> getViolationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "violationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting violations for user: {} - page: {}, size: {}", userId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AttendanceViolationDto> violations = violationService.getViolationsByUser(userId, pageable);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get violations by status
     * GET /api/hr/violations/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceViolationDto>> getViolationsByStatus(
            @PathVariable AttendanceViolation.ViolationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "violationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting violations by status: {} - page: {}, size: {}", status, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AttendanceViolationDto> violations = violationService.getViolationsByStatus(status, pageable);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get violations needing explanation
     * GET /api/hr/violations/pending-explanation
     */
    @GetMapping("/pending-explanation")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceViolationDto>> getViolationsNeedingExplanation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting violations needing explanation - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("violationDate").descending());
        Page<AttendanceViolationDto> violations = violationService.getViolationsNeedingExplanation(pageable);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get violations pending review
     * GET /api/hr/violations/pending-review
     */
    @GetMapping("/pending-review")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceViolationDto>> getViolationsPendingReview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting violations pending review - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("violationDate").descending());
        Page<AttendanceViolationDto> violations = violationService.getViolationsPendingReview(pageable);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get overdue violations
     * GET /api/hr/violations/overdue
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceViolationDto>> getOverdueViolations(
            @RequestParam(defaultValue = "3") int daysSince) {
        
        log.info("Getting overdue violations - days since: {}", daysSince);
        
        List<AttendanceViolationDto> violations = violationService.getOverdueViolations(daysSince);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get violations for specific date
     * GET /api/hr/violations/date/{date}
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceViolationDto>> getViolationsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting violations for date: {}", date);
        
        List<AttendanceViolationDto> violations = violationService.getViolationsForDate(date);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Get violations by date range
     * GET /api/hr/violations/date-range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceViolationDto>> getViolationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "violationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting violations by date range: {} to {} - page: {}, size: {}", 
                startDate, endDate, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AttendanceViolationDto> violations = violationService.getViolationsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(violations);
    }
    
    /**
     * Resolve violation manually
     * PATCH /api/hr/violations/{id}/resolve
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceViolationDto> resolveViolation(
            @PathVariable Long id,
            @RequestBody(required = false) String resolutionNotes) {
        
        log.info("Resolving violation: {} with notes: {}", id, resolutionNotes);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        AttendanceViolationDto resolvedViolation = violationService.resolveViolation(id, currentUserId, resolutionNotes);
        return ResponseEntity.ok(resolvedViolation);
    }
    
    /**
     * Escalate violation
     * PATCH /api/hr/violations/{id}/escalate
     */
    @PatchMapping("/{id}/escalate")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceViolationDto> escalateViolation(
            @PathVariable Long id,
            @RequestBody(required = false) String escalationNotes) {
        
        log.info("Escalating violation: {} with notes: {}", id, escalationNotes);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        AttendanceViolationDto escalatedViolation = violationService.escalateViolation(id, currentUserId, escalationNotes);
        return ResponseEntity.ok(escalatedViolation);
    }
    
    /**
     * Get violation statistics by type
     * GET /api/hr/violations/statistics/by-type
     */
    @GetMapping("/statistics/by-type")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getViolationStatisticsByType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting violation statistics by type from {} to {}", startDate, endDate);
        
        Object statistics = violationService.getViolationStatisticsByType(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get violation statistics by user
     * GET /api/hr/violations/statistics/by-user
     */
    @GetMapping("/statistics/by-user")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getViolationStatisticsByUser(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting violation statistics by user from {} to {}", startDate, endDate);
        
        Object statistics = violationService.getViolationStatisticsByUser(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get monthly violation summary
     * GET /api/hr/violations/statistics/monthly
     */
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getMonthlyViolationSummary(
            @RequestParam int year,
            @RequestParam int month) {
        
        log.info("Getting monthly violation summary for {}/{}", year, month);
        
        Object summary = violationService.getMonthlyViolationSummary(year, month);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get violation dashboard data for current user
     * GET /api/hr/violations/dashboard/my
     */
    @GetMapping("/dashboard/my")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getMyViolationDashboard() {
        log.info("Getting violation dashboard for current user");
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        Object dashboardData = violationService.getViolationDashboardData(currentUserId);
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get violation dashboard data for managers
     * GET /api/hr/violations/dashboard/manager
     */
    @GetMapping("/dashboard/manager")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getManagerViolationDashboard() {
        log.info("Getting violation dashboard for managers");
        
        Object dashboardData = violationService.getManagerViolationDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}
