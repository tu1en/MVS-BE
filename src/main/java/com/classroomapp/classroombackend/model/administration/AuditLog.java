package com.classroomapp.classroombackend.model.administration;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing system audit logs
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_ip", columnList = "ip_address")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "username", length = 100)
    private String username;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 30, nullable = false)
    private AuditAction action;
    
    @Column(name = "entity_type", length = 100)
    private String entityType;
    
    @Column(name = "entity_id", length = 100)
    private String entityId;
    
    @Column(name = "entity_name", length = 200)
    private String entityName;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON string
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON string
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "request_url", length = 500)
    private String requestUrl;
    
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    private AuditSeverity severity = AuditSeverity.INFO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private AuditCategory category = AuditCategory.GENERAL;
    
    @Column(name = "module", length = 50)
    private String module;
    
    @Column(name = "success", columnDefinition = "BIT DEFAULT 1")
    private Boolean success = true;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "additional_data", columnDefinition = "NVARCHAR(MAX)")
    private String additionalData; // JSON string for extra data
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Audit action enumeration
     */
    public enum AuditAction {
        CREATE("Tạo mới"),
        READ("Xem"),
        UPDATE("Cập nhật"),
        DELETE("Xóa"),
        LOGIN("Đăng nhập"),
        LOGOUT("Đăng xuất"),
        LOGIN_FAILED("Đăng nhập thất bại"),
        PASSWORD_CHANGE("Đổi mật khẩu"),
        PASSWORD_RESET("Reset mật khẩu"),
        PERMISSION_GRANT("Cấp quyền"),
        PERMISSION_REVOKE("Thu hồi quyền"),
        ROLE_ASSIGN("Gán role"),
        ROLE_REMOVE("Gỡ role"),
        CONFIG_CHANGE("Thay đổi cấu hình"),
        EXPORT("Xuất dữ liệu"),
        IMPORT("Nhập dữ liệu"),
        BACKUP("Sao lưu"),
        RESTORE("Khôi phục"),
        SYSTEM_START("Khởi động hệ thống"),
        SYSTEM_STOP("Dừng hệ thống"),
        ERROR("Lỗi hệ thống"),
        SECURITY_VIOLATION("Vi phạm bảo mật");
        
        private final String description;
        
        AuditAction(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Audit severity enumeration
     */
    public enum AuditSeverity {
        TRACE("Trace"),
        DEBUG("Debug"),
        INFO("Info"),
        WARN("Warning"),
        ERROR("Error"),
        FATAL("Fatal");
        
        private final String description;
        
        AuditSeverity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return ordinal();
        }
        
        public boolean isHigherThan(AuditSeverity other) {
            return this.getLevel() > other.getLevel();
        }
    }
    
    /**
     * Audit category enumeration
     */
    public enum AuditCategory {
        GENERAL("Tổng quát"),
        AUTHENTICATION("Xác thực"),
        AUTHORIZATION("Phân quyền"),
        DATA_ACCESS("Truy cập dữ liệu"),
        DATA_MODIFICATION("Thay đổi dữ liệu"),
        SYSTEM_CONFIG("Cấu hình hệ thống"),
        USER_MANAGEMENT("Quản lý người dùng"),
        SECURITY("Bảo mật"),
        PERFORMANCE("Hiệu suất"),
        ERROR("Lỗi"),
        INTEGRATION("Tích hợp"),
        BACKUP("Sao lưu"),
        MONITORING("Giám sát");
        
        private final String description;
        
        AuditCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business logic methods
    
    /**
     * Check if audit log indicates success
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(success);
    }
    
    /**
     * Check if audit log indicates failure
     */
    public boolean isFailure() {
        return !Boolean.TRUE.equals(success);
    }
    
    /**
     * Check if audit log is security-related
     */
    public boolean isSecurityRelated() {
        return category == AuditCategory.SECURITY || 
               category == AuditCategory.AUTHENTICATION || 
               category == AuditCategory.AUTHORIZATION ||
               action == AuditAction.SECURITY_VIOLATION ||
               action == AuditAction.LOGIN_FAILED;
    }
    
    /**
     * Check if audit log is critical
     */
    public boolean isCritical() {
        return severity == AuditSeverity.ERROR || 
               severity == AuditSeverity.FATAL ||
               isSecurityRelated();
    }
    
    /**
     * Get formatted timestamp
     */
    public String getFormattedTimestamp() {
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    /**
     * Get execution time in seconds
     */
    public Double getExecutionTimeSeconds() {
        if (executionTimeMs == null) {
            return null;
        }
        
        return executionTimeMs / 1000.0;
    }
    
    /**
     * Get audit summary
     */
    public String getAuditSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (username != null) {
            summary.append(username);
        } else {
            summary.append("System");
        }
        
        summary.append(" - ").append(action.getDescription());
        
        if (entityType != null) {
            summary.append(" ").append(entityType);
            if (entityName != null) {
                summary.append(" (").append(entityName).append(")");
            }
        }
        
        if (description != null) {
            summary.append(" - ").append(description);
        }
        
        return summary.toString();
    }
    
    /**
     * Check if audit log has changes
     */
    public boolean hasChanges() {
        return (oldValues != null && !oldValues.trim().isEmpty()) ||
               (newValues != null && !newValues.trim().isEmpty());
    }
    
    /**
     * Check if audit log has error
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }
    
    /**
     * Get severity color for UI
     */
    public String getSeverityColor() {
        switch (severity) {
            case FATAL:
                return "#ff0000";
            case ERROR:
                return "#ff4d4f";
            case WARN:
                return "#faad14";
            case INFO:
                return "#1890ff";
            case DEBUG:
                return "#52c41a";
            case TRACE:
                return "#d9d9d9";
            default:
                return "#000000";
        }
    }
    
    /**
     * Get action color for UI
     */
    public String getActionColor() {
        switch (action) {
            case CREATE:
                return "#52c41a";
            case UPDATE:
                return "#1890ff";
            case DELETE:
                return "#ff4d4f";
            case LOGIN:
                return "#722ed1";
            case LOGOUT:
                return "#fa8c16";
            case LOGIN_FAILED:
            case SECURITY_VIOLATION:
            case ERROR:
                return "#ff0000";
            default:
                return "#666666";
        }
    }
    
    /**
     * Create audit log builder
     */
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }
    
    /**
     * Builder class for AuditLog
     */
    public static class AuditLogBuilder {
        private AuditLog auditLog = new AuditLog();
        
        public AuditLogBuilder user(User user) {
            auditLog.setUser(user);
            if (user != null) {
                auditLog.setUsername(user.getUsername());
            }
            return this;
        }
        
        public AuditLogBuilder username(String username) {
            auditLog.setUsername(username);
            return this;
        }
        
        public AuditLogBuilder sessionId(String sessionId) {
            auditLog.setSessionId(sessionId);
            return this;
        }
        
        public AuditLogBuilder action(AuditAction action) {
            auditLog.setAction(action);
            return this;
        }
        
        public AuditLogBuilder entity(String entityType, String entityId, String entityName) {
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setEntityName(entityName);
            return this;
        }
        
        public AuditLogBuilder description(String description) {
            auditLog.setDescription(description);
            return this;
        }
        
        public AuditLogBuilder changes(String oldValues, String newValues) {
            auditLog.setOldValues(oldValues);
            auditLog.setNewValues(newValues);
            return this;
        }
        
        public AuditLogBuilder request(String ipAddress, String userAgent, String url, String method) {
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setRequestUrl(url);
            auditLog.setRequestMethod(method);
            return this;
        }
        
        public AuditLogBuilder response(Integer status, Long executionTime) {
            auditLog.setResponseStatus(status);
            auditLog.setExecutionTimeMs(executionTime);
            return this;
        }
        
        public AuditLogBuilder severity(AuditSeverity severity) {
            auditLog.setSeverity(severity);
            return this;
        }
        
        public AuditLogBuilder category(AuditCategory category) {
            auditLog.setCategory(category);
            return this;
        }
        
        public AuditLogBuilder module(String module) {
            auditLog.setModule(module);
            return this;
        }
        
        public AuditLogBuilder success(Boolean success) {
            auditLog.setSuccess(success);
            return this;
        }
        
        public AuditLogBuilder error(String errorMessage) {
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSuccess(false);
            auditLog.setSeverity(AuditSeverity.ERROR);
            return this;
        }
        
        public AuditLogBuilder additionalData(String additionalData) {
            auditLog.setAdditionalData(additionalData);
            return this;
        }
        
        public AuditLog build() {
            return auditLog;
        }
    }
}
