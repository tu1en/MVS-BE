package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing evidence file templates for accountants
 */
@Entity
@Table(name = "evidence_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_name", nullable = false)
    private String templateName;
    
    @Column(name = "template_code", unique = true)
    private String templateCode;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TemplateCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "download_url")
    private String downloadUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "usage_instructions", columnDefinition = "TEXT")
    private String usageInstructions;
    
    @Column(name = "required_fields")
    private String requiredFields; // JSON string of required fields
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * Template categories for evidence files
     */
    public enum TemplateCategory {
        ATTENDANCE("Chấm công"),
        PAYROLL("Lương bổng"),
        CONTRACT("Hợp đồng"),
        MEDICAL("Y tế"),
        VIOLATION("Vi phạm"),
        EXPLANATION("Giải trình"),
        REPORT("Báo cáo"),
        OTHER("Khác");
        
        private final String displayName;
        
        TemplateCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * File types for templates
     */
    public enum FileType {
        PDF("PDF"),
        DOCX("Word Document"),
        XLSX("Excel Spreadsheet"),
        DOC("Word Document (Legacy)"),
        XLS("Excel Spreadsheet (Legacy)"),
        RTF("Rich Text Format"),
        ODT("OpenDocument Text"),
        ODS("OpenDocument Spreadsheet");
        
        private final String displayName;
        
        FileType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (templateCode == null || templateCode.trim().isEmpty()) {
            templateCode = generateTemplateCode();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Generate template code based on category and name
     */
    private String generateTemplateCode() {
        String categoryPrefix = category.name().substring(0, Math.min(3, category.name().length()));
        String namePrefix = templateName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (namePrefix.length() > 5) {
            namePrefix = namePrefix.substring(0, 5);
        }
        return categoryPrefix + "_" + namePrefix + "_" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Get file extension from fileName
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Check if template is downloadable
     */
    public boolean isDownloadable() {
        return isActive && (filePath != null || downloadUrl != null);
    }
    
    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "Unknown";
        }
        
        double bytes = fileSize.doubleValue();
        if (bytes < 1024) {
            return String.format("%.0f B", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024 * 1024 * 1024));
        }
    }
    
    /**
     * Get display category name
     */
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "Không xác định";
    }
    
    /**
     * Get display file type name
     */
    public String getFileTypeDisplayName() {
        return fileType != null ? fileType.getDisplayName() : "Không xác định";
    }
}