package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AttendanceViolation entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceViolationDto {
    
    private Long id;
    
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userDepartment;
    
    private Long shiftAssignmentId;
    private String shiftName;
    private String shiftTimeRange;
    
    private Long attendanceLogId;
    
    private LocalDate violationDate;
    
    private AttendanceViolation.ViolationType violationType;
    private String violationTypeDescription;
    
    private AttendanceViolation.ViolationSeverity severity;
    private String severityDescription;
    
    private LocalTime expectedTime;
    private LocalTime actualTime;
    private Integer deviationMinutes;
    
    private String systemDescription;
    
    private AttendanceViolation.ViolationStatus status;
    private String statusDescription;
    
    private Boolean autoDetected;
    private LocalDateTime detectionTime;
    
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
    private String resolvedByName;
    private String resolutionNotes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Boolean needsExplanation;
    private Boolean isResolved;
    private Boolean canBeExplained;
    private Long daysSinceViolation;
    private Boolean isOverdueForExplanation;
    
    // Explanation info
    private Boolean hasExplanation;
    private Integer explanationCount;
    private String latestExplanationStatus;
    
    /**
     * Get violation type description
     */
    public String getViolationTypeDescription() {
        return violationType != null ? violationType.getDescription() : null;
    }
    
    /**
     * Get severity description
     */
    public String getSeverityDescription() {
        return severity != null ? severity.getDescription() : null;
    }
    
    /**
     * Get status description
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : null;
    }
    
    /**
     * Get detailed violation description
     */
    public String getDetailedDescription() {
        StringBuilder desc = new StringBuilder();
        
        if (violationType != null) {
            desc.append(violationType.getDescription());
        }
        
        if (deviationMinutes != null && deviationMinutes > 0) {
            desc.append(" (").append(deviationMinutes).append(" phút)");
        }
        
        if (expectedTime != null && actualTime != null) {
            desc.append(" - Dự kiến: ").append(expectedTime)
                .append(", Thực tế: ").append(actualTime);
        }
        
        return desc.toString();
    }
    
    /**
     * Get formatted violation date
     */
    public String getFormattedViolationDate() {
        return violationDate != null ? violationDate.toString() : null;
    }
    
    /**
     * Get violation summary for display
     */
    public String getViolationSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (userFullName != null) {
            summary.append(userFullName).append(" - ");
        }
        
        if (violationType != null) {
            summary.append(violationType.getDescription());
        }
        
        if (violationDate != null) {
            summary.append(" (").append(violationDate).append(")");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if violation needs explanation
     */
    public Boolean getNeedsExplanation() {
        return status == AttendanceViolation.ViolationStatus.PENDING_EXPLANATION;
    }
    
    /**
     * Check if violation is resolved
     */
    public Boolean getIsResolved() {
        return status == AttendanceViolation.ViolationStatus.RESOLVED || 
               status == AttendanceViolation.ViolationStatus.APPROVED;
    }
    
    /**
     * Check if violation can be explained
     */
    public Boolean getCanBeExplained() {
        return status == AttendanceViolation.ViolationStatus.PENDING_EXPLANATION || 
               status == AttendanceViolation.ViolationStatus.REJECTED;
    }
    
    /**
     * Calculate days since violation occurred
     */
    public Long getDaysSinceViolation() {
        if (violationDate == null) return 0L;
        return (long) violationDate.until(LocalDate.now()).getDays();
    }
    
    /**
     * Check if violation is overdue for explanation (more than 3 days)
     */
    public Boolean getIsOverdueForExplanation() {
        return getNeedsExplanation() && getDaysSinceViolation() > 3;
    }
    
    /**
     * Get severity color for UI
     */
    public String getSeverityColor() {
        if (severity == null) return "default";
        
        switch (severity) {
            case MINOR:
                return "blue";
            case MODERATE:
                return "orange";
            case MAJOR:
                return "red";
            case CRITICAL:
                return "purple";
            default:
                return "default";
        }
    }
    
    /**
     * Get status color for UI
     */
    public String getStatusColor() {
        if (status == null) return "default";
        
        switch (status) {
            case PENDING_EXPLANATION:
                return "red";
            case EXPLANATION_SUBMITTED:
                return "blue";
            case UNDER_REVIEW:
                return "orange";
            case APPROVED:
                return "green";
            case REJECTED:
                return "red";
            case RESOLVED:
                return "green";
            case ESCALATED:
                return "purple";
            default:
                return "default";
        }
    }
}
