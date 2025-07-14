package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;

/**
 * Represents a data integrity issue found during verification
 */
public class DataIntegrityIssue {
    
    private final IssueSeverity severity;
    private final String code;
    private final String message;
    private final String details;
    private final LocalDateTime timestamp;
    
    public DataIntegrityIssue(IssueSeverity severity, String code, String message, String details) {
        this.severity = severity;
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    public IssueSeverity getSeverity() {
        return severity;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDetails() {
        return details;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ");
        sb.append(code).append(": ").append(message);
        if (details != null && !details.trim().isEmpty()) {
            sb.append(" - ").append(details);
        }
        return sb.toString();
    }
    
    /**
     * Tạo issue với severity CRITICAL
     */
    public static DataIntegrityIssue critical(String code, String message, String details) {
        return new DataIntegrityIssue(IssueSeverity.CRITICAL, code, message, details);
    }
    
    /**
     * Tạo issue với severity WARNING
     */
    public static DataIntegrityIssue warning(String code, String message, String details) {
        return new DataIntegrityIssue(IssueSeverity.WARNING, code, message, details);
    }
    
    /**
     * Tạo issue với severity INFO
     */
    public static DataIntegrityIssue info(String code, String message, String details) {
        return new DataIntegrityIssue(IssueSeverity.INFO, code, message, details);
    }
}
