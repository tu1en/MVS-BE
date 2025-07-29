package com.classroomapp.classroombackend.controller.administration;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.administration.AuditLog;
import com.classroomapp.classroombackend.service.administration.SystemAdministrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SystemAdministrationService adminService;

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Getting audit logs - page: {}, size: {}", page, size);
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // For now, just return all audit logs - can add filtering later
            Page<AuditLog> auditLogs = adminService.getAuditLogs(pageable);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            log.error("Error getting audit logs", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audit-logs/statistics")
    public ResponseEntity<SystemAdministrationService.AuditStatistics> getAuditStatistics(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting audit statistics for last {} days", days);
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            SystemAdministrationService.AuditStatistics stats = adminService.getAuditStatistics(since);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting audit statistics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audit-logs/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        log.info("Getting audit log by ID: {}", id);
        try {
            AuditLog auditLog = adminService.findAuditLogById(id);
            if (auditLog != null) {
                return ResponseEntity.ok(auditLog);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting audit log by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 