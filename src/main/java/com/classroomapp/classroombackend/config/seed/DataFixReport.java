package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report cho data fix operations
 */
public class DataFixReport {
    
    private final LocalDateTime timestamp;
    private final Map<String, Integer> fixResults;
    private final List<String> errors;
    private final List<String> warnings;
    
    public DataFixReport() {
        this.timestamp = LocalDateTime.now();
        this.fixResults = new HashMap<>();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public void addResult(String operation, int count) {
        fixResults.put(operation, count);
    }
    
    public void addError(String operation, String error) {
        errors.add(operation + ": " + error);
    }
    
    public void addWarning(String operation, String warning) {
        warnings.add(operation + ": " + warning);
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Integer> getFixResults() {
        return fixResults;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public int getTotalFixedItems() {
        return fixResults.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Fix Report Summary\n");
        sb.append("Generated: ").append(timestamp).append("\n");
        sb.append("Total Fixed Items: ").append(getTotalFixedItems()).append("\n");
        
        if (!fixResults.isEmpty()) {
            sb.append("Fix Results:\n");
            fixResults.forEach((operation, count) -> 
                sb.append("  - ").append(operation).append(": ").append(count).append(" items\n"));
        }
        
        if (hasErrors()) {
            sb.append("Errors: ").append(errors.size()).append("\n");
        }
        
        if (hasWarnings()) {
            sb.append("Warnings: ").append(warnings.size()).append("\n");
        }
        
        return sb.toString();
    }
    
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        
        if (hasErrors()) {
            sb.append("\n=== ERRORS ===\n");
            errors.forEach(error -> sb.append("❌ ").append(error).append("\n"));
        }
        
        if (hasWarnings()) {
            sb.append("\n=== WARNINGS ===\n");
            warnings.forEach(warning -> sb.append("⚠️ ").append(warning).append("\n"));
        }
        
        return sb.toString();
    }
    
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(timestamp).append("\",\n");
        json.append("  \"totalFixedItems\": ").append(getTotalFixedItems()).append(",\n");
        json.append("  \"hasErrors\": ").append(hasErrors()).append(",\n");
        json.append("  \"hasWarnings\": ").append(hasWarnings()).append(",\n");
        json.append("  \"fixResults\": {\n");
        
        boolean first = true;
        for (Map.Entry<String, Integer> entry : fixResults.entrySet()) {
            if (!first) json.append(",\n");
            json.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        
        json.append("\n  }\n");
        json.append("}");
        return json.toString();
    }
}
