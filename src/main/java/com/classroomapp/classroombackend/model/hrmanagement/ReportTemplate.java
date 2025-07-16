package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing report templates for HR system
 */
@Entity
@Table(name = "report_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_name", length = 200, nullable = false)
    private String templateName;
    
    @Column(name = "template_code", length = 50, nullable = false, unique = true)
    private String templateCode;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_category", length = 30, nullable = false)
    private ReportCategory reportCategory;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 20, nullable = false)
    private ReportType reportType;
    
    @Column(name = "sql_query", columnDefinition = "NVARCHAR(MAX)")
    private String sqlQuery;

    @Column(name = "parameters", columnDefinition = "NVARCHAR(MAX)")
    private String parameters; // JSON string for report parameters

    @Column(name = "columns_config", columnDefinition = "NVARCHAR(MAX)")
    private String columnsConfig; // JSON string for column configuration

    @Column(name = "chart_config", columnDefinition = "NVARCHAR(MAX)")
    private String chartConfig; // JSON string for chart configuration

    @Column(name = "filters_config", columnDefinition = "NVARCHAR(MAX)")
    private String filtersConfig; // JSON string for filter configuration
    
    @Column(name = "is_system_template", columnDefinition = "BIT DEFAULT 0")
    private Boolean isSystemTemplate = false;
    
    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;
    
    @Column(name = "is_public", columnDefinition = "BIT DEFAULT 0")
    private Boolean isPublic = false;
    
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * Report category enumeration
     */
    public enum ReportCategory {
        ATTENDANCE("Chấm công"),
        PAYROLL("Bảng lương"),
        VIOLATION("Vi phạm"),
        PERFORMANCE("Hiệu suất"),
        SUMMARY("Tổng hợp"),
        ANALYTICS("Phân tích");
        
        private final String description;
        
        ReportCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Report type enumeration
     */
    public enum ReportType {
        TABLE("Bảng"),
        CHART("Biểu đồ"),
        DASHBOARD("Dashboard"),
        EXPORT("Xuất file");
        
        private final String description;
        
        ReportType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business logic methods
    
    /**
     * Check if template can be edited
     */
    public boolean canBeEdited() {
        return !Boolean.TRUE.equals(isSystemTemplate);
    }
    
    /**
     * Check if template can be deleted
     */
    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(isSystemTemplate);
    }
    
    /**
     * Check if template is accessible by user
     */
    public boolean isAccessibleBy(Long userId, String userRole) {
        // Public templates are accessible by everyone
        if (Boolean.TRUE.equals(isPublic)) {
            return true;
        }
        
        // System templates are accessible by managers and admins
        if (Boolean.TRUE.equals(isSystemTemplate)) {
            return "MANAGER".equals(userRole) || "ADMIN".equals(userRole);
        }
        
        // Private templates are accessible by creator and admins
        if (createdBy != null && createdBy.equals(userId)) {
            return true;
        }
        
        return "ADMIN".equals(userRole);
    }
    
    /**
     * Get template display name
     */
    public String getDisplayName() {
        return templateName + " (" + reportCategory.getDescription() + ")";
    }
    
    /**
     * Check if template has chart configuration
     */
    public boolean hasChartConfig() {
        return chartConfig != null && !chartConfig.trim().isEmpty();
    }
    
    /**
     * Check if template has filters
     */
    public boolean hasFilters() {
        return filtersConfig != null && !filtersConfig.trim().isEmpty();
    }
    
    /**
     * Check if template has parameters
     */
    public boolean hasParameters() {
        return parameters != null && !parameters.trim().isEmpty();
    }
    
    /**
     * Get template complexity score (for performance optimization)
     */
    public int getComplexityScore() {
        int score = 0;
        
        // Base score for SQL query length
        if (sqlQuery != null) {
            score += sqlQuery.length() / 100;
        }
        
        // Add score for features
        if (hasChartConfig()) score += 2;
        if (hasFilters()) score += 1;
        if (hasParameters()) score += 1;
        
        // Add score for report type
        switch (reportType) {
            case DASHBOARD:
                score += 3;
                break;
            case CHART:
                score += 2;
                break;
            case EXPORT:
                score += 1;
                break;
            default:
                break;
        }
        
        return score;
    }
    
    /**
     * Validate template configuration
     */
    public boolean isValidConfiguration() {
        // Must have name and code
        if (templateName == null || templateName.trim().isEmpty()) {
            return false;
        }
        
        if (templateCode == null || templateCode.trim().isEmpty()) {
            return false;
        }
        
        // Must have SQL query for data reports
        if (reportType != ReportType.DASHBOARD && 
            (sqlQuery == null || sqlQuery.trim().isEmpty())) {
            return false;
        }
        
        // Chart reports must have chart config
        if (reportType == ReportType.CHART && !hasChartConfig()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Clone template for customization
     */
    public ReportTemplate cloneForCustomization(String newName, String newCode, Long userId) {
        ReportTemplate clone = new ReportTemplate();
        clone.setTemplateName(newName);
        clone.setTemplateCode(newCode);
        clone.setDescription("Sao chép từ: " + this.templateName);
        clone.setReportCategory(this.reportCategory);
        clone.setReportType(this.reportType);
        clone.setSqlQuery(this.sqlQuery);
        clone.setParameters(this.parameters);
        clone.setColumnsConfig(this.columnsConfig);
        clone.setChartConfig(this.chartConfig);
        clone.setFiltersConfig(this.filtersConfig);
        clone.setIsSystemTemplate(false);
        clone.setIsActive(true);
        clone.setIsPublic(false);
        clone.setSortOrder(this.sortOrder);
        clone.setCreatedBy(userId);
        
        return clone;
    }
}
