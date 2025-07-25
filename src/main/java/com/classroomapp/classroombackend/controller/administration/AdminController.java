package com.classroomapp.classroombackend.controller.administration;

import com.classroomapp.classroombackend.model.administration.*;
import com.classroomapp.classroombackend.service.administration.SystemAdministrationService;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for system administration
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final SystemAdministrationService adminService;
    private final SecurityUtils securityUtils;
    
    // System Health & Information
    
    /**
     * Get system health status
     * GET /api/admin/health
     */
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
    
    /**
     * Perform health check
     * POST /api/admin/health/check
     */
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
    
    /**
     * Get system information
     * GET /api/admin/system-info
     */
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
    
    /**
     * Get all active roles
     * GET /api/admin/roles
     */
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
    
    /**
     * Create new role
     * POST /api/admin/roles
     */
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
    
    /**
     * Update role
     * PUT /api/admin/roles/{roleId}
     */
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
    
    /**
     * Delete role
     * DELETE /api/admin/roles/{roleId}
     */
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
    
    /**
     * Get role permissions
     * GET /api/admin/roles/{roleId}/permissions
     */
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
    
    /**
     * Grant permission to role
     * POST /api/admin/roles/{roleId}/permissions/{permissionId}
     */
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
    
    /**
     * Revoke permission from role
     * DELETE /api/admin/roles/{roleId}/permissions/{permissionId}
     */
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
    
    /**
     * Get all permissions
     * GET /api/admin/permissions
     */
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
    
    /**
     * Create new permission
     * POST /api/admin/permissions
     */
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
    
    /**
     * Get configurations by category
     * GET /api/admin/configurations
     */
    @GetMapping("/configurations")
    public ResponseEntity<List<SystemConfiguration>> getConfigurations(
            @RequestParam(required = false) SystemConfiguration.ConfigCategory category) {
        log.info("Getting configurations for category: {}", category);
        
        try {
            List<SystemConfiguration> configs;
            if (category != null) {
                configs = adminService.getConfigurationsByCategory(category);
            } else {
                // Get all configurations - implement this method
                configs = adminService.getConfigurationsByCategory(null);
            }
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("Error getting configurations", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update configuration value
     * PUT /api/admin/configurations/{configKey}
     */
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
    
    /**
     * Get audit logs
     * GET /api/admin/audit-logs
     */
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
    
    /**
     * Get security logs
     * GET /api/admin/audit-logs/security
     */
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
    
    /**
     * Get audit statistics
     * GET /api/admin/audit-logs/statistics
     */
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
    
    /**
     * Get audit log by ID
     * GET /api/admin/audit-logs/{id}
     */
    @GetMapping("/audit-logs/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        log.info("Getting audit log by ID: {}", id);
        
        try {
            // Use the repository directly since we don't have a service method for this yet
            // In a real implementation, we would add this method to the service interface
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
    
    /**
     * Get latest system metrics
     * GET /api/admin/monitoring/metrics
     */
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
    
    /**
     * Get critical metrics
     * GET /api/admin/monitoring/critical
     */
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
    
    /**
     * Get monitoring statistics
     * GET /api/admin/monitoring/statistics
     */
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
    
    /**
     * Cleanup old audit logs
     * POST /api/admin/maintenance/cleanup-audit-logs
     */
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
    
    /**
     * Generate system backup
     * POST /api/admin/maintenance/backup
     */
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
    
    /**
     * Get backup history
     * GET /api/admin/maintenance/backups
     */
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
    
    /**
     * Get active users count
     * GET /api/admin/users/active-count
     */
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
    
    /**
     * Get user activity statistics
     * GET /api/admin/users/activity-statistics
     */
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
