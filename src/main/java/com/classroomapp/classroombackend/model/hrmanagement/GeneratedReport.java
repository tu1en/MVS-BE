package com.classroomapp.classroombackend.model.hrmanagement;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing generated reports
 */
@Entity
@Table(name = "generated_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ReportTemplate template;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;
    
    @Column(name = "report_name", length = 200, nullable = false)
    private String reportName;
    
    @Column(name = "parameters", columnDefinition = "NVARCHAR(MAX)")
    private String parameters; // JSON string for report parameters used

    @Column(name = "filters", columnDefinition = "NVARCHAR(MAX)")
    private String filters; // JSON string for filters applied

    @Column(name = "data_snapshot", columnDefinition = "NVARCHAR(MAX)")
    private String dataSnapshot; // JSON string containing report data
    
    @Column(name = "chart_data", columnDefinition = "NVARCHAR(MAX)")
    private String chartData; // JSON string for chart data

    @Column(name = "summary_stats", columnDefinition = "NVARCHAR(MAX)")
    private String summaryStats; // JSON string for summary statistics
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReportStatus status = ReportStatus.GENERATING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20, nullable = false)
    private ReportFormat format = ReportFormat.JSON;
    
    @Column(name = "file_path", length = 500)
    private String filePath; // Path to generated file (PDF, Excel, etc.)
    
    @Column(name = "file_url", length = 1000)
    private String fileUrl; // URL to download generated file
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "record_count")
    private Integer recordCount;
    
    @Column(name = "generation_time_ms")
    private Long generationTimeMs;
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_scheduled", columnDefinition = "BIT DEFAULT 0")
    private Boolean isScheduled = false;
    
    @Column(name = "schedule_expression", length = 100)
    private String scheduleExpression; // Cron expression for scheduled reports
    
    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;
    
    @Column(name = "download_count", columnDefinition = "INT DEFAULT 0")
    private Integer downloadCount = 0;
    
    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Report status enumeration
     */
    public enum ReportStatus {
        GENERATING("Đang tạo"),
        COMPLETED("Hoàn thành"),
        FAILED("Thất bại"),
        EXPIRED("Hết hạn"),
        CANCELLED("Đã hủy");
        
        private final String description;
        
        ReportStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Report format enumeration
     */
    public enum ReportFormat {
        JSON("JSON"),
        PDF("PDF"),
        EXCEL("Excel"),
        CSV("CSV"),
        HTML("HTML");
        
        private final String description;
        
        ReportFormat(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getFileExtension() {
            switch (this) {
                case PDF:
                    return ".pdf";
                case EXCEL:
                    return ".xlsx";
                case CSV:
                    return ".csv";
                case HTML:
                    return ".html";
                default:
                    return ".json";
            }
        }
        
        public String getMimeType() {
            switch (this) {
                case PDF:
                    return "application/pdf";
                case EXCEL:
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case CSV:
                    return "text/csv";
                case HTML:
                    return "text/html";
                default:
                    return "application/json";
            }
        }
    }
    
    // Business logic methods
    
    /**
     * Mark report as completed
     */
    public void markAsCompleted() {
        this.status = ReportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        
        // Set expiration time (30 days from completion)
        this.expiresAt = completedAt.plusDays(30);
    }
    
    /**
     * Mark report as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Check if report is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if report can be downloaded
     */
    public boolean canBeDownloaded() {
        return status == ReportStatus.COMPLETED && !isExpired() && 
               (filePath != null || fileUrl != null);
    }
    
    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        this.downloadCount = (downloadCount != null ? downloadCount : 0) + 1;
        this.lastDownloadedAt = LocalDateTime.now();
    }
    
    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Get generation duration in seconds
     */
    public Double getGenerationDurationSeconds() {
        if (generationTimeMs == null) {
            return null;
        }
        
        return generationTimeMs / 1000.0;
    }
    
    /**
     * Check if report generation is in progress
     */
    public boolean isGenerating() {
        return status == ReportStatus.GENERATING;
    }
    
    /**
     * Check if report generation was successful
     */
    public boolean isSuccessful() {
        return status == ReportStatus.COMPLETED;
    }
    
    /**
     * Check if report generation failed
     */
    public boolean isFailed() {
        return status == ReportStatus.FAILED;
    }
    
    /**
     * Get display name for the report
     */
    public String getDisplayName() {
        return reportName + " (" + format.getDescription() + ")";
    }
    
    /**
     * Get suggested filename for download
     */
    public String getSuggestedFilename() {
        String baseName = reportName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        String timestamp = createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        return baseName + "_" + timestamp + format.getFileExtension();
    }
    
    /**
     * Check if report has data
     */
    public boolean hasData() {
        return recordCount != null && recordCount > 0;
    }
    
    /**
     * Check if report has chart data
     */
    public boolean hasChartData() {
        return chartData != null && !chartData.trim().isEmpty();
    }
    
    /**
     * Check if report has summary statistics
     */
    public boolean hasSummaryStats() {
        return summaryStats != null && !summaryStats.trim().isEmpty();
    }
    
    /**
     * Get time until expiration
     */
    public String getTimeUntilExpiration() {
        if (expiresAt == null) {
            return "Không hết hạn";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return "Đã hết hạn";
        }
        
        long hours = java.time.Duration.between(now, expiresAt).toHours();
        if (hours < 24) {
            return hours + " giờ";
        } else {
            long days = hours / 24;
            return days + " ngày";
        }
    }
    
    /**
     * Cancel report generation
     */
    public void cancel() {
        if (status == ReportStatus.GENERATING) {
            this.status = ReportStatus.CANCELLED;
            this.completedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Check if this is a scheduled report
     */
    public boolean isScheduledReport() {
        return Boolean.TRUE.equals(isScheduled) && 
               scheduleExpression != null && !scheduleExpression.trim().isEmpty();
    }
}
