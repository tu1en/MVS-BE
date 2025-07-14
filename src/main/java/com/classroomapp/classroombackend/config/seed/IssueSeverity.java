package com.classroomapp.classroombackend.config.seed;

/**
 * Severity levels for data integrity issues
 */
public enum IssueSeverity {
    /**
     * Critical issues that must be fixed immediately
     * Examples: Missing required relationships, orphaned records, data corruption
     */
    CRITICAL,
    
    /**
     * Warning issues that should be addressed but don't break functionality
     * Examples: Missing optional data, inconsistent formatting, performance concerns
     */
    WARNING,
    
    /**
     * Informational issues for awareness
     * Examples: Statistics, recommendations, best practices
     */
    INFO
}
