package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.GeneratedReport;
import com.classroomapp.classroombackend.model.hrmanagement.ReportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for reporting operations
 */
public interface ReportingService {
    
    // Template management
    
    /**
     * Create new report template
     */
    ReportTemplate createTemplate(ReportTemplate template, Long createdBy);
    
    /**
     * Update existing template
     */
    ReportTemplate updateTemplate(Long templateId, ReportTemplate template, Long updatedBy);
    
    /**
     * Delete template
     */
    void deleteTemplate(Long templateId, Long deletedBy);
    
    /**
     * Get template by ID
     */
    ReportTemplate getTemplateById(Long templateId);
    
    /**
     * Get template by code
     */
    ReportTemplate getTemplateByCode(String templateCode);
    
    /**
     * Get all active templates
     */
    Page<ReportTemplate> getActiveTemplates(Pageable pageable);
    
    /**
     * Get templates by category
     */
    Page<ReportTemplate> getTemplatesByCategory(ReportTemplate.ReportCategory category, Pageable pageable);
    
    /**
     * Get accessible templates for user
     */
    Page<ReportTemplate> getAccessibleTemplates(Long userId, String userRole, Pageable pageable);
    
    /**
     * Search templates
     */
    Page<ReportTemplate> searchTemplates(String searchTerm, Pageable pageable);
    
    /**
     * Clone template for customization
     */
    ReportTemplate cloneTemplate(Long templateId, String newName, String newCode, Long userId);
    
    // Report generation
    
    /**
     * Generate report from template
     */
    GeneratedReport generateReport(Long templateId, Map<String, Object> parameters, 
                                 GeneratedReport.ReportFormat format, Long userId);
    
    /**
     * Generate report asynchronously
     */
    GeneratedReport generateReportAsync(Long templateId, Map<String, Object> parameters, 
                                      GeneratedReport.ReportFormat format, Long userId);
    
    /**
     * Get generated report by ID
     */
    GeneratedReport getGeneratedReportById(Long reportId);
    
    /**
     * Get reports by user
     */
    Page<GeneratedReport> getReportsByUser(Long userId, Pageable pageable);
    
    /**
     * Get reports by template
     */
    Page<GeneratedReport> getReportsByTemplate(Long templateId, Pageable pageable);
    
    /**
     * Get reports by status
     */
    Page<GeneratedReport> getReportsByStatus(GeneratedReport.ReportStatus status, Pageable pageable);
    
    /**
     * Get completed reports
     */
    Page<GeneratedReport> getCompletedReports(Pageable pageable);
    
    /**
     * Cancel report generation
     */
    void cancelReportGeneration(Long reportId, Long userId);
    
    /**
     * Delete generated report
     */
    void deleteGeneratedReport(Long reportId, Long userId);
    
    // Report data and download
    
    /**
     * Get report data
     */
    ReportData getReportData(Long reportId, Long userId);
    
    /**
     * Generate download URL for report
     */
    String generateDownloadUrl(Long reportId, Long userId);
    
    /**
     * Download report file
     */
    byte[] downloadReportFile(Long reportId, Long userId);
    
    /**
     * Get report preview data
     */
    ReportPreview getReportPreview(Long templateId, Map<String, Object> parameters, Long userId);
    
    // Statistics and analytics
    
    /**
     * Get report statistics
     */
    ReportStatistics getReportStatistics();
    
    /**
     * Get template usage statistics
     */
    List<TemplateUsageStats> getTemplateUsageStatistics();
    
    /**
     * Get user report activity
     */
    List<UserReportActivity> getUserReportActivity(Long userId);
    
    /**
     * Get daily report trends
     */
    List<DailyReportTrend> getDailyReportTrends(int days);
    
    // Maintenance and cleanup
    
    /**
     * Cleanup expired reports
     */
    CleanupResult cleanupExpiredReports();
    
    /**
     * Get storage usage
     */
    StorageUsage getStorageUsage();
    
    /**
     * Validate template configuration
     */
    TemplateValidationResult validateTemplate(ReportTemplate template);
    
    /**
     * Test template SQL query
     */
    QueryTestResult testTemplateQuery(String sqlQuery, Map<String, Object> parameters);
    
    // Scheduled reports
    
    /**
     * Schedule report generation
     */
    GeneratedReport scheduleReport(Long templateId, String cronExpression, 
                                 Map<String, Object> parameters, 
                                 GeneratedReport.ReportFormat format, Long userId);
    
    /**
     * Update scheduled report
     */
    GeneratedReport updateScheduledReport(Long reportId, String cronExpression, 
                                        Map<String, Object> parameters, Long userId);
    
    /**
     * Cancel scheduled report
     */
    void cancelScheduledReport(Long reportId, Long userId);
    
    /**
     * Get scheduled reports
     */
    List<GeneratedReport> getScheduledReports();
    
    /**
     * Process scheduled reports
     */
    void processScheduledReports();
    
    // Inner classes for DTOs
    
    class ReportData {
        private Long reportId;
        private String reportName;
        private List<Map<String, Object>> data;
        private Map<String, Object> chartData;
        private Map<String, Object> summaryStats;
        private Integer recordCount;
        
        // Constructors, getters, setters
        public ReportData() {}
        
        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        
        public String getReportName() { return reportName; }
        public void setReportName(String reportName) { this.reportName = reportName; }
        
        public List<Map<String, Object>> getData() { return data; }
        public void setData(List<Map<String, Object>> data) { this.data = data; }
        
        public Map<String, Object> getChartData() { return chartData; }
        public void setChartData(Map<String, Object> chartData) { this.chartData = chartData; }
        
        public Map<String, Object> getSummaryStats() { return summaryStats; }
        public void setSummaryStats(Map<String, Object> summaryStats) { this.summaryStats = summaryStats; }
        
        public Integer getRecordCount() { return recordCount; }
        public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }
    }
    
    class ReportPreview {
        private List<Map<String, Object>> sampleData;
        private List<String> columns;
        private Integer totalRecords;
        private String query;
        
        public ReportPreview() {}
        
        public List<Map<String, Object>> getSampleData() { return sampleData; }
        public void setSampleData(List<Map<String, Object>> sampleData) { this.sampleData = sampleData; }
        
        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }
        
        public Integer getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }
        
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }
    
    class ReportStatistics {
        private Long totalTemplates;
        private Long totalReports;
        private Long completedReports;
        private Long failedReports;
        private Double averageGenerationTime;
        private Long totalStorageUsed;
        
        public ReportStatistics() {}
        
        public Long getTotalTemplates() { return totalTemplates; }
        public void setTotalTemplates(Long totalTemplates) { this.totalTemplates = totalTemplates; }
        
        public Long getTotalReports() { return totalReports; }
        public void setTotalReports(Long totalReports) { this.totalReports = totalReports; }
        
        public Long getCompletedReports() { return completedReports; }
        public void setCompletedReports(Long completedReports) { this.completedReports = completedReports; }
        
        public Long getFailedReports() { return failedReports; }
        public void setFailedReports(Long failedReports) { this.failedReports = failedReports; }
        
        public Double getAverageGenerationTime() { return averageGenerationTime; }
        public void setAverageGenerationTime(Double averageGenerationTime) { this.averageGenerationTime = averageGenerationTime; }
        
        public Long getTotalStorageUsed() { return totalStorageUsed; }
        public void setTotalStorageUsed(Long totalStorageUsed) { this.totalStorageUsed = totalStorageUsed; }
    }
    
    class TemplateUsageStats {
        private Long templateId;
        private String templateName;
        private Long usageCount;
        private Double averageGenerationTime;
        private Long totalDownloads;
        
        public TemplateUsageStats() {}
        
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public Long getUsageCount() { return usageCount; }
        public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
        
        public Double getAverageGenerationTime() { return averageGenerationTime; }
        public void setAverageGenerationTime(Double averageGenerationTime) { this.averageGenerationTime = averageGenerationTime; }
        
        public Long getTotalDownloads() { return totalDownloads; }
        public void setTotalDownloads(Long totalDownloads) { this.totalDownloads = totalDownloads; }
    }
    
    class UserReportActivity {
        private String userName;
        private Long reportCount;
        private Long downloadCount;
        private LocalDateTime lastActivity;
        
        public UserReportActivity() {}
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public Long getReportCount() { return reportCount; }
        public void setReportCount(Long reportCount) { this.reportCount = reportCount; }
        
        public Long getDownloadCount() { return downloadCount; }
        public void setDownloadCount(Long downloadCount) { this.downloadCount = downloadCount; }
        
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }
    
    class DailyReportTrend {
        private LocalDateTime date;
        private Long reportCount;
        private Long successCount;
        private Long failureCount;
        
        public DailyReportTrend() {}
        
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        
        public Long getReportCount() { return reportCount; }
        public void setReportCount(Long reportCount) { this.reportCount = reportCount; }
        
        public Long getSuccessCount() { return successCount; }
        public void setSuccessCount(Long successCount) { this.successCount = successCount; }
        
        public Long getFailureCount() { return failureCount; }
        public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
    }
    
    class CleanupResult {
        private Integer deletedReports;
        private Long freedSpace;
        private List<String> errors;
        
        public CleanupResult() {
            this.errors = new java.util.ArrayList<>();
        }
        
        public Integer getDeletedReports() { return deletedReports; }
        public void setDeletedReports(Integer deletedReports) { this.deletedReports = deletedReports; }
        
        public Long getFreedSpace() { return freedSpace; }
        public void setFreedSpace(Long freedSpace) { this.freedSpace = freedSpace; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public void addError(String error) { this.errors.add(error); }
    }
    
    class StorageUsage {
        private Long totalReports;
        private Long totalSize;
        private Long averageSize;
        private Long maxSize;
        private Map<String, Long> sizeByFormat;
        
        public StorageUsage() {}
        
        public Long getTotalReports() { return totalReports; }
        public void setTotalReports(Long totalReports) { this.totalReports = totalReports; }
        
        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
        
        public Long getAverageSize() { return averageSize; }
        public void setAverageSize(Long averageSize) { this.averageSize = averageSize; }
        
        public Long getMaxSize() { return maxSize; }
        public void setMaxSize(Long maxSize) { this.maxSize = maxSize; }
        
        public Map<String, Long> getSizeByFormat() { return sizeByFormat; }
        public void setSizeByFormat(Map<String, Long> sizeByFormat) { this.sizeByFormat = sizeByFormat; }
    }
    
    class TemplateValidationResult {
        private boolean isValid;
        private List<String> errors;
        private List<String> warnings;
        
        public TemplateValidationResult() {
            this.errors = new java.util.ArrayList<>();
            this.warnings = new java.util.ArrayList<>();
        }
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public void addError(String error) { this.errors.add(error); }
        public void addWarning(String warning) { this.warnings.add(warning); }
    }
    
    class QueryTestResult {
        private boolean isValid;
        private List<Map<String, Object>> sampleData;
        private List<String> columns;
        private Integer recordCount;
        private Long executionTime;
        private String error;
        
        public QueryTestResult() {}
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public List<Map<String, Object>> getSampleData() { return sampleData; }
        public void setSampleData(List<Map<String, Object>> sampleData) { this.sampleData = sampleData; }
        
        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }
        
        public Integer getRecordCount() { return recordCount; }
        public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }
        
        public Long getExecutionTime() { return executionTime; }
        public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
