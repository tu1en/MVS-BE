package com.classroomapp.classroombackend.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.administration.AuditLog;
import com.classroomapp.classroombackend.model.administration.RolePermission;
import com.classroomapp.classroombackend.model.administration.SystemConfiguration;
import com.classroomapp.classroombackend.model.administration.SystemMonitoring;
import com.classroomapp.classroombackend.model.administration.SystemPermission;
import com.classroomapp.classroombackend.model.administration.SystemRole;
import com.classroomapp.classroombackend.service.administration.SystemAdministrationService;
import com.classroomapp.classroombackend.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SystemAdminController {

    private final SystemAdministrationService adminService;
    private final SecurityUtils securityUtils;

    // System Health & Information
    @GetMapping("/health")
    public ResponseEntity<SystemAdministrationService.SystemHealthStatus> getSystemHealth() {
        log.info("Getting system health status");
        try {
            SystemAdministrationService.SystemHealthStatus health = adminService.getSystemHealthStatus();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting system health", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/health/check")
    public ResponseEntity<SystemAdministrationService.HealthCheckResult> performHealthCheck() {
        log.info("Performing system health check");
        try {
            SystemAdministrationService.HealthCheckResult result = adminService.performHealthCheck();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error performing health check", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/system-info")
    public ResponseEntity<SystemAdministrationService.SystemInformation> getSystemInformation() {
        log.info("Getting system information");
        try {
            SystemAdministrationService.SystemInformation info = adminService.getSystemInformation();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Error getting system information", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Role Management
    @GetMapping("/roles")
    public ResponseEntity<List<SystemRole>> getAllRoles() {
        log.info("Getting all active roles");
        try {
            List<SystemRole> roles = adminService.getAllActiveRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Error getting roles", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<SystemRole> createRole(@RequestBody SystemRole role) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Creating new role: {} by user {}", role.getRoleName(), currentUserId);
        try {
            SystemRole createdRole = adminService.createRole(role, currentUserId);
            return ResponseEntity.ok(createdRole);
        } catch (Exception e) {
            log.error("Error creating role", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/roles/{roleId}")
    public ResponseEntity<SystemRole> updateRole(@PathVariable Long roleId, @RequestBody SystemRole role) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Updating role: {} by user {}", roleId, currentUserId);
        try {
            SystemRole updatedRole = adminService.updateRole(roleId, role, currentUserId);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            log.error("Error updating role: {}", roleId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Deleting role: {} by user {}", roleId, currentUserId);
        try {
            adminService.deleteRole(roleId, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting role: {}", roleId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<List<RolePermission>> getRolePermissions(@PathVariable Long roleId) {
        log.info("Getting permissions for role: {}", roleId);
        try {
            List<RolePermission> permissions = adminService.getRolePermissions(roleId);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Error getting role permissions: {}", roleId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RolePermission> grantPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Granting permission {} to role {} by user {}", permissionId, roleId, currentUserId);
        try {
            RolePermission rolePermission = adminService.grantPermissionToRole(roleId, permissionId, currentUserId);
            return ResponseEntity.ok(rolePermission);
        } catch (Exception e) {
            log.error("Error granting permission {} to role {}", permissionId, roleId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> revokePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Revoking permission {} from role {} by user {}", permissionId, roleId, currentUserId);
        try {
            adminService.revokePermissionFromRole(roleId, permissionId, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error revoking permission {} from role {}", permissionId, roleId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Permission Management
    @GetMapping("/permissions")
    public ResponseEntity<List<SystemPermission>> getAllPermissions() {
        log.info("Getting all active permissions");
        try {
            List<SystemPermission> permissions = adminService.getAllActivePermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Error getting permissions", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/permissions")
    public ResponseEntity<SystemPermission> createPermission(@RequestBody SystemPermission permission) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Creating new permission: {} by user {}", permission.getPermissionName(), currentUserId);
        try {
            SystemPermission createdPermission = adminService.createPermission(permission, currentUserId);
            return ResponseEntity.ok(createdPermission);
        } catch (Exception e) {
            log.error("Error creating permission", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Configuration Management
    @GetMapping("/configurations")
    public ResponseEntity<List<SystemConfiguration>> getConfigurations(
            @RequestParam(required = false) SystemConfiguration.ConfigCategory category) {
        log.info("Getting configurations for category: {}", category);
        try {
            List<SystemConfiguration> configs = category != null ? 
                adminService.getConfigurationsByCategory(category) :
                adminService.getConfigurationsByCategory(null);
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("Error getting configurations", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/configurations/{configKey}")
    public ResponseEntity<Void> updateConfiguration(@PathVariable String configKey, @RequestBody String value) {
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Updating configuration {} by user {}", configKey, currentUserId);
        try {
            adminService.setConfigurationValue(configKey, value, currentUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating configuration: {}", configKey, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Audit Logs
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Getting audit logs");
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<AuditLog> auditLogs = adminService.getAuditLogs(pageable);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            log.error("Error getting audit logs", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audit-logs/security")
    public ResponseEntity<Page<AuditLog>> getSecurityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting security logs");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<AuditLog> securityLogs = adminService.getSecurityLogs(pageable);
            return ResponseEntity.ok(securityLogs);
        } catch (Exception e) {
            log.error("Error getting security logs", e);
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

    // System Monitoring
    @GetMapping("/monitoring/metrics")
    public ResponseEntity<List<SystemMonitoring>> getLatestMetrics() {
        log.info("Getting latest system metrics");
        try {
            List<SystemMonitoring> metrics = adminService.getLatestMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting system metrics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/monitoring/critical")
    public ResponseEntity<List<SystemMonitoring>> getCriticalMetrics() {
        log.info("Getting critical system metrics");
        try {
            List<SystemMonitoring> criticalMetrics = adminService.getCriticalMetrics();
            return ResponseEntity.ok(criticalMetrics);
        } catch (Exception e) {
            log.error("Error getting critical metrics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/monitoring/statistics")
    public ResponseEntity<SystemAdministrationService.MonitoringStatistics> getMonitoringStatistics() {
        log.info("Getting monitoring statistics");
        try {
            SystemAdministrationService.MonitoringStatistics stats = adminService.getMonitoringStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting monitoring statistics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Maintenance Operations
    @PostMapping("/maintenance/cleanup-audit-logs")
    public ResponseEntity<SystemAdministrationService.CleanupResult> cleanupAuditLogs(
            @RequestParam(defaultValue = "90") int daysToKeep) {
        log.info("Cleaning up audit logs older than {} days", daysToKeep);
        try {
            SystemAdministrationService.CleanupResult result = adminService.cleanupOldAuditLogs(daysToKeep);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error cleaning up audit logs", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/maintenance/backup")
    public ResponseEntity<SystemAdministrationService.BackupResult> generateBackup() {
        log.info("Generating system backup");
        try {
            SystemAdministrationService.BackupResult result = adminService.generateSystemBackup();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating backup", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/maintenance/backups")
    public ResponseEntity<List<SystemAdministrationService.BackupInfo>> getBackupHistory() {
        log.info("Getting backup history");
        try {
            List<SystemAdministrationService.BackupInfo> backups = adminService.getBackupHistory();
            return ResponseEntity.ok(backups);
        } catch (Exception e) {
            log.error("Error getting backup history", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // User Activity
    @GetMapping("/users/active-count")
    public ResponseEntity<Long> getActiveUsersCount() {
        log.info("Getting active users count");
        try {
            Long count = adminService.getActiveUsersCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting active users count", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/activity-statistics")
    public ResponseEntity<SystemAdministrationService.UserActivityStatistics> getUserActivityStatistics(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting user activity statistics for last {} days", days);
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            SystemAdministrationService.UserActivityStatistics stats = adminService.getUserActivityStatistics(since);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting user activity statistics", e);
            return ResponseEntity.badRequest().build();
        }
    }
}