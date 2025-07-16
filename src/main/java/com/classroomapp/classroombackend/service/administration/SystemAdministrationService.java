package com.classroomapp.classroombackend.service.administration;

import com.classroomapp.classroombackend.model.administration.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for system administration operations
 */
public interface SystemAdministrationService {
    
    // Role Management
    
    /**
     * Create new system role
     */
    SystemRole createRole(SystemRole role, Long createdBy);
    
    /**
     * Update existing role
     */
    SystemRole updateRole(Long roleId, SystemRole role, Long updatedBy);
    
    /**
     * Delete role
     */
    void deleteRole(Long roleId, Long deletedBy);
    
    /**
     * Get role by ID
     */
    SystemRole getRoleById(Long roleId);
    
    /**
     * Get role by code
     */
    SystemRole getRoleByCode(String roleCode);
    
    /**
     * Get all active roles
     */
    List<SystemRole> getAllActiveRoles();
    
    /**
     * Search roles
     */
    Page<SystemRole> searchRoles(String searchTerm, Pageable pageable);
    
    /**
     * Assign role to user
     */
    void assignRoleToUser(Long userId, Long roleId, Long assignedBy);
    
    /**
     * Remove role from user
     */
    void removeRoleFromUser(Long userId, Long roleId, Long removedBy);
    
    // Permission Management
    
    /**
     * Create new permission
     */
    SystemPermission createPermission(SystemPermission permission, Long createdBy);
    
    /**
     * Update permission
     */
    SystemPermission updatePermission(Long permissionId, SystemPermission permission, Long updatedBy);
    
    /**
     * Delete permission
     */
    void deletePermission(Long permissionId, Long deletedBy);
    
    /**
     * Get permission by ID
     */
    SystemPermission getPermissionById(Long permissionId);
    
    /**
     * Get all active permissions
     */
    List<SystemPermission> getAllActivePermissions();
    
    /**
     * Grant permission to role
     */
    RolePermission grantPermissionToRole(Long roleId, Long permissionId, Long grantedBy);
    
    /**
     * Revoke permission from role
     */
    void revokePermissionFromRole(Long roleId, Long permissionId, Long revokedBy);
    
    /**
     * Get role permissions
     */
    List<RolePermission> getRolePermissions(Long roleId);
    
    // Configuration Management
    
    /**
     * Create configuration
     */
    SystemConfiguration createConfiguration(SystemConfiguration config, Long createdBy);
    
    /**
     * Update configuration
     */
    SystemConfiguration updateConfiguration(Long configId, SystemConfiguration config, Long updatedBy);
    
    /**
     * Delete configuration
     */
    void deleteConfiguration(Long configId, Long deletedBy);
    
    /**
     * Get configuration by key
     */
    SystemConfiguration getConfigurationByKey(String configKey);
    
    /**
     * Get configuration value
     */
    Object getConfigurationValue(String configKey);
    
    /**
     * Set configuration value
     */
    void setConfigurationValue(String configKey, String value, Long updatedBy);
    
    /**
     * Get configurations by category
     */
    List<SystemConfiguration> getConfigurationsByCategory(SystemConfiguration.ConfigCategory category);
    
    /**
     * Reset configuration to default
     */
    void resetConfigurationToDefault(String configKey, Long updatedBy);
    
    // Audit & Logging
    
    /**
     * Create audit log
     */
    AuditLog createAuditLog(AuditLog auditLog);
    
    /**
     * Get audit logs
     */
    Page<AuditLog> getAuditLogs(Pageable pageable);
    
    /**
     * Get audit logs by user
     */
    Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable);
    
    /**
     * Get audit logs by action
     */
    Page<AuditLog> getAuditLogsByAction(AuditLog.AuditAction action, Pageable pageable);
    
    /**
     * Get security logs
     */
    Page<AuditLog> getSecurityLogs(Pageable pageable);
    
    /**
     * Search audit logs
     */
    Page<AuditLog> searchAuditLogs(String searchTerm, Pageable pageable);
    
    /**
     * Get audit statistics
     */
    AuditStatistics getAuditStatistics(LocalDateTime since);
    
    // System Monitoring
    
    /**
     * Record monitoring metric
     */
    SystemMonitoring recordMetric(SystemMonitoring metric);
    
    /**
     * Get system metrics
     */
    List<SystemMonitoring> getSystemMetrics(SystemMonitoring.MonitoringCategory category);
    
    /**
     * Get latest metrics
     */
    List<SystemMonitoring> getLatestMetrics();
    
    /**
     * Get critical metrics
     */
    List<SystemMonitoring> getCriticalMetrics();
    
    /**
     * Get monitoring statistics
     */
    MonitoringStatistics getMonitoringStatistics();
    
    // System Health
    
    /**
     * Get system health status
     */
    SystemHealthStatus getSystemHealthStatus();
    
    /**
     * Perform system health check
     */
    HealthCheckResult performHealthCheck();
    
    /**
     * Get system information
     */
    SystemInformation getSystemInformation();
    
    // Maintenance Operations
    
    /**
     * Cleanup old audit logs
     */
    CleanupResult cleanupOldAuditLogs(int daysToKeep);
    
    /**
     * Cleanup old monitoring data
     */
    CleanupResult cleanupOldMonitoringData(int daysToKeep);
    
    /**
     * Optimize database
     */
    void optimizeDatabase();
    
    /**
     * Generate system backup
     */
    BackupResult generateSystemBackup();
    
    /**
     * Get backup history
     */
    List<BackupInfo> getBackupHistory();
    
    // User Activity
    
    /**
     * Get active users count
     */
    Long getActiveUsersCount();
    
    /**
     * Get user activity statistics
     */
    UserActivityStatistics getUserActivityStatistics(LocalDateTime since);
    
    /**
     * Get most active users
     */
    List<UserActivity> getMostActiveUsers(int limit);
    
    // Inner classes for DTOs
    
    class AuditStatistics {
        private Long totalLogs;
        private Long successfulLogs;
        private Long failedLogs;
        private Long uniqueUsers;
        private Long uniqueIPs;
        private Map<String, Long> actionCounts;
        private Map<String, Long> categoryCounts;
        
        // Constructors, getters, setters
        public AuditStatistics() {}
        
        public Long getTotalLogs() { return totalLogs; }
        public void setTotalLogs(Long totalLogs) { this.totalLogs = totalLogs; }
        
        public Long getSuccessfulLogs() { return successfulLogs; }
        public void setSuccessfulLogs(Long successfulLogs) { this.successfulLogs = successfulLogs; }
        
        public Long getFailedLogs() { return failedLogs; }
        public void setFailedLogs(Long failedLogs) { this.failedLogs = failedLogs; }
        
        public Long getUniqueUsers() { return uniqueUsers; }
        public void setUniqueUsers(Long uniqueUsers) { this.uniqueUsers = uniqueUsers; }
        
        public Long getUniqueIPs() { return uniqueIPs; }
        public void setUniqueIPs(Long uniqueIPs) { this.uniqueIPs = uniqueIPs; }
        
        public Map<String, Long> getActionCounts() { return actionCounts; }
        public void setActionCounts(Map<String, Long> actionCounts) { this.actionCounts = actionCounts; }
        
        public Map<String, Long> getCategoryCounts() { return categoryCounts; }
        public void setCategoryCounts(Map<String, Long> categoryCounts) { this.categoryCounts = categoryCounts; }
    }
    
    class MonitoringStatistics {
        private Long totalMetrics;
        private Long normalMetrics;
        private Long warningMetrics;
        private Long criticalMetrics;
        private Map<String, Long> categoryCounts;
        
        public MonitoringStatistics() {}
        
        public Long getTotalMetrics() { return totalMetrics; }
        public void setTotalMetrics(Long totalMetrics) { this.totalMetrics = totalMetrics; }
        
        public Long getNormalMetrics() { return normalMetrics; }
        public void setNormalMetrics(Long normalMetrics) { this.normalMetrics = normalMetrics; }
        
        public Long getWarningMetrics() { return warningMetrics; }
        public void setWarningMetrics(Long warningMetrics) { this.warningMetrics = warningMetrics; }
        
        public Long getCriticalMetrics() { return criticalMetrics; }
        public void setCriticalMetrics(Long criticalMetrics) { this.criticalMetrics = criticalMetrics; }
        
        public Map<String, Long> getCategoryCounts() { return categoryCounts; }
        public void setCategoryCounts(Map<String, Long> categoryCounts) { this.categoryCounts = categoryCounts; }
    }
    
    class SystemHealthStatus {
        private String overallStatus;
        private Map<String, String> componentStatuses;
        private List<String> issues;
        private LocalDateTime lastChecked;
        
        public SystemHealthStatus() {}
        
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
        
        public Map<String, String> getComponentStatuses() { return componentStatuses; }
        public void setComponentStatuses(Map<String, String> componentStatuses) { this.componentStatuses = componentStatuses; }
        
        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
        
        public LocalDateTime getLastChecked() { return lastChecked; }
        public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
    }
    
    class HealthCheckResult {
        private boolean healthy;
        private Map<String, Boolean> checks;
        private List<String> errors;
        private LocalDateTime timestamp;
        
        public HealthCheckResult() {}
        
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        
        public Map<String, Boolean> getChecks() { return checks; }
        public void setChecks(Map<String, Boolean> checks) { this.checks = checks; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    class SystemInformation {
        private String applicationName;
        private String version;
        private String buildTime;
        private String javaVersion;
        private String osName;
        private String osVersion;
        private Long totalMemory;
        private Long freeMemory;
        private Long usedMemory;
        private Integer availableProcessors;
        private LocalDateTime startTime;
        private Long uptime;
        
        public SystemInformation() {}
        
        // Getters and setters
        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getBuildTime() { return buildTime; }
        public void setBuildTime(String buildTime) { this.buildTime = buildTime; }
        
        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
        
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        
        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
        
        public Long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(Long totalMemory) { this.totalMemory = totalMemory; }
        
        public Long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(Long freeMemory) { this.freeMemory = freeMemory; }
        
        public Long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(Long usedMemory) { this.usedMemory = usedMemory; }
        
        public Integer getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(Integer availableProcessors) { this.availableProcessors = availableProcessors; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public Long getUptime() { return uptime; }
        public void setUptime(Long uptime) { this.uptime = uptime; }
    }
    
    class CleanupResult {
        private Integer deletedRecords;
        private Long freedSpace;
        private List<String> errors;
        
        public CleanupResult() {
            this.errors = new java.util.ArrayList<>();
        }
        
        public Integer getDeletedRecords() { return deletedRecords; }
        public void setDeletedRecords(Integer deletedRecords) { this.deletedRecords = deletedRecords; }
        
        public Long getFreedSpace() { return freedSpace; }
        public void setFreedSpace(Long freedSpace) { this.freedSpace = freedSpace; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public void addError(String error) { this.errors.add(error); }
    }
    
    class BackupResult {
        private String backupId;
        private String backupPath;
        private Long backupSize;
        private LocalDateTime backupTime;
        private boolean success;
        private String errorMessage;
        
        public BackupResult() {}
        
        public String getBackupId() { return backupId; }
        public void setBackupId(String backupId) { this.backupId = backupId; }
        
        public String getBackupPath() { return backupPath; }
        public void setBackupPath(String backupPath) { this.backupPath = backupPath; }
        
        public Long getBackupSize() { return backupSize; }
        public void setBackupSize(Long backupSize) { this.backupSize = backupSize; }
        
        public LocalDateTime getBackupTime() { return backupTime; }
        public void setBackupTime(LocalDateTime backupTime) { this.backupTime = backupTime; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    class BackupInfo {
        private String backupId;
        private String backupName;
        private Long backupSize;
        private LocalDateTime backupTime;
        private String status;
        
        public BackupInfo() {}
        
        public String getBackupId() { return backupId; }
        public void setBackupId(String backupId) { this.backupId = backupId; }
        
        public String getBackupName() { return backupName; }
        public void setBackupName(String backupName) { this.backupName = backupName; }
        
        public Long getBackupSize() { return backupSize; }
        public void setBackupSize(Long backupSize) { this.backupSize = backupSize; }
        
        public LocalDateTime getBackupTime() { return backupTime; }
        public void setBackupTime(LocalDateTime backupTime) { this.backupTime = backupTime; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    class UserActivityStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long newUsers;
        private Map<String, Long> activityByHour;
        private List<UserActivity> topUsers;
        
        public UserActivityStatistics() {}
        
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        
        public Long getNewUsers() { return newUsers; }
        public void setNewUsers(Long newUsers) { this.newUsers = newUsers; }
        
        public Map<String, Long> getActivityByHour() { return activityByHour; }
        public void setActivityByHour(Map<String, Long> activityByHour) { this.activityByHour = activityByHour; }
        
        public List<UserActivity> getTopUsers() { return topUsers; }
        public void setTopUsers(List<UserActivity> topUsers) { this.topUsers = topUsers; }
    }
    
    class UserActivity {
        private String username;
        private String fullName;
        private Long activityCount;
        private LocalDateTime lastActivity;
        
        public UserActivity() {}
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public Long getActivityCount() { return activityCount; }
        public void setActivityCount(Long activityCount) { this.activityCount = activityCount; }
        
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }
}
