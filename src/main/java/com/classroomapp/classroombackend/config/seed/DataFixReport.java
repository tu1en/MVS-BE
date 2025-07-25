package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataFixReport {
    private String status;
    private LocalDateTime timestamp;
    private List<String> appliedFixes;
    private List<String> errors;
    private List<String> warnings;
    private int totalFixesApplied;
    private boolean successful;
    
    public DataFixReport() {
        this.timestamp = LocalDateTime.now();
        this.appliedFixes = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.totalFixesApplied = 0;
        this.successful = true;
        this.status = "SUCCESS";
    }
    
    public void addFix(String fix) {
        this.appliedFixes.add(fix);
        this.totalFixesApplied++;
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.successful = false;
        this.status = "ERROR";
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
        if (!"ERROR".equals(this.status)) {
            this.status = "WARNING";
        }
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
    
    public List<String> getAppliedFixes() {
        return appliedFixes;
    }
    
    public void setAppliedFixes(List<String> appliedFixes) {
        this.appliedFixes = appliedFixes;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    public int getTotalFixesApplied() {
        return totalFixesApplied;
    }
    
    public void setTotalFixesApplied(int totalFixesApplied) {
        this.totalFixesApplied = totalFixesApplied;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getSummary() {
        return String.format("Status: %s, Fixes Applied: %d, Errors: %d, Warnings: %d", 
            status, totalFixesApplied, errors.size(), warnings.size());
    }
}