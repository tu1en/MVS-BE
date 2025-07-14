package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comprehensive report of data verification results
 */
public class DataVerificationReport {
    
    private final List<DataIntegrityIssue> issues;
    private final LocalDateTime reportTimestamp;
    private final Map<IssueSeverity, Long> issueCounts;
    
    public DataVerificationReport(List<DataIntegrityIssue> issues) {
        this.issues = issues;
        this.reportTimestamp = LocalDateTime.now();
        this.issueCounts = issues.stream()
            .collect(Collectors.groupingBy(DataIntegrityIssue::getSeverity, Collectors.counting()));
    }
    
    public List<DataIntegrityIssue> getIssues() {
        return issues;
    }
    
    public LocalDateTime getReportTimestamp() {
        return reportTimestamp;
    }
    
    public int getTotalIssues() {
        return issues.size();
    }
    
    public long getCriticalIssues() {
        return issueCounts.getOrDefault(IssueSeverity.CRITICAL, 0L);
    }
    
    public long getWarningIssues() {
        return issueCounts.getOrDefault(IssueSeverity.WARNING, 0L);
    }
    
    public long getInfoIssues() {
        return issueCounts.getOrDefault(IssueSeverity.INFO, 0L);
    }
    
    public List<DataIntegrityIssue> getCriticalIssuesList() {
        return issues.stream()
            .filter(issue -> issue.getSeverity() == IssueSeverity.CRITICAL)
            .collect(Collectors.toList());
    }
    
    public List<DataIntegrityIssue> getWarningIssuesList() {
        return issues.stream()
            .filter(issue -> issue.getSeverity() == IssueSeverity.WARNING)
            .collect(Collectors.toList());
    }
    
    public List<DataIntegrityIssue> getInfoIssuesList() {
        return issues.stream()
            .filter(issue -> issue.getSeverity() == IssueSeverity.INFO)
            .collect(Collectors.toList());
    }
    
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    public boolean hasCriticalIssues() {
        return getCriticalIssues() > 0;
    }
    
    public boolean hasWarningIssues() {
        return getWarningIssues() > 0;
    }
    
    /**
     * Tạo summary text của report
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Verification Report Summary\n");
        sb.append("Generated: ").append(reportTimestamp).append("\n");
        sb.append("Total Issues: ").append(getTotalIssues()).append("\n");
        sb.append("  - Critical: ").append(getCriticalIssues()).append("\n");
        sb.append("  - Warning: ").append(getWarningIssues()).append("\n");
        sb.append("  - Info: ").append(getInfoIssues()).append("\n");
        
        if (hasCriticalIssues()) {
            sb.append("\n⚠️ CRITICAL ISSUES FOUND - IMMEDIATE ACTION REQUIRED\n");
        } else if (hasWarningIssues()) {
            sb.append("\n⚠️ Warning issues found - should be addressed\n");
        } else {
            sb.append("\n✅ No critical issues found\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Tạo detailed report text
     */
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        
        if (hasCriticalIssues()) {
            sb.append("\n=== CRITICAL ISSUES ===\n");
            getCriticalIssuesList().forEach(issue -> sb.append(issue.toString()).append("\n"));
        }
        
        if (hasWarningIssues()) {
            sb.append("\n=== WARNING ISSUES ===\n");
            getWarningIssuesList().forEach(issue -> sb.append(issue.toString()).append("\n"));
        }
        
        if (getInfoIssues() > 0) {
            sb.append("\n=== INFORMATIONAL ===\n");
            getInfoIssuesList().forEach(issue -> sb.append(issue.toString()).append("\n"));
        }
        
        return sb.toString();
    }
    
    /**
     * Kiểm tra xem có nên fail application startup không
     */
    public boolean shouldFailStartup() {
        // Fail nếu có critical issues
        return hasCriticalIssues();
    }
    
    /**
     * Tạo JSON representation (đơn giản)
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(reportTimestamp).append("\",\n");
        json.append("  \"totalIssues\": ").append(getTotalIssues()).append(",\n");
        json.append("  \"criticalIssues\": ").append(getCriticalIssues()).append(",\n");
        json.append("  \"warningIssues\": ").append(getWarningIssues()).append(",\n");
        json.append("  \"infoIssues\": ").append(getInfoIssues()).append(",\n");
        json.append("  \"hasCriticalIssues\": ").append(hasCriticalIssues()).append("\n");
        json.append("}");
        return json.toString();
    }
}
