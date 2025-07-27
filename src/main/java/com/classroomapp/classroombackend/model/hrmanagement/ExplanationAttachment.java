package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.classroomapp.classroombackend.model.AttendanceExplanation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing supporting evidence files for attendance explanations
 */
@Entity
@Table(name = "explanation_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_id", nullable = false)
    private AttendanceExplanation explanation;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type")
    private String mimeType;
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    // Helper methods for file operations
    
    /**
     * Get file extension from filename
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Check if file is an image
     */
    public boolean isImage() {
        String ext = getFileExtension();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("bmp");
    }
    
    /**
     * Check if file is a document
     */
    public boolean isDocument() {
        String ext = getFileExtension();
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
               ext.equals("xls") || ext.equals("xlsx") || ext.equals("txt");
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
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
}