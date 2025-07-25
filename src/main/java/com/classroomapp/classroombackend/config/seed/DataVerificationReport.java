package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataVerificationReport {
    private String status;
    private LocalDateTime timestamp;
    private List<String> issues;
    private List<String> warnings;
    private List<String> successes;
    private boolean hasCriticalIssues;
    
    public DataVerificationReport() {
        this.timestamp = LocalDateTime.now();
        this.issues = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.successes = new ArrayList<>();
        this.hasCriticalIssues = false;
        this.status = "HEALTHY";
    }
    
    public void addIssue(String issue) {
        this.issues.add(issue);
        this.hasCriticalIssues = true;
        this.status = "ISSUES_FOUND";
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
        if (!"ISSUES_FOUND".equals(this.status)) {
            this.status = "WARNINGS";
        }
    }
    
    public void addSuccess(String success) {
        this.successes.add(success);
    }
    
    // Getters and setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<String> getIssues() {
        return issues;
    }
    
    public void setIssues(List<String> issues) {
        this.issues = issues;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    public List<String> getSuccesses() {
        return successes;
    }
    
    public void setSuccesses(List<String> successes) {
        this.successes = successes;
    }
    
    public boolean isHasCriticalIssues() {
        return hasCriticalIssues;
    }
    
    public void setHasCriticalIssues(boolean hasCriticalIssues) {
        this.hasCriticalIssues = hasCriticalIssues;
    }
    
    public String getSummary() {
        return String.format("Status: %s, Issues: %d, Warnings: %d, Successes: %d", 
            status, issues.size(), warnings.size(), successes.size());
    }
}