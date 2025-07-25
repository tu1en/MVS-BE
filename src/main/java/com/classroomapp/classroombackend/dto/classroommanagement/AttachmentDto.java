package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Attachment entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {

    private Long id;

    private String fileName;

    private String originalFileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Long sessionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;

    private String uploadedBy;

    /**
     * Get formatted file size for display
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Check if file is an image
     */
    public boolean isImage() {
        if (fileType == null) return false;
        return fileType.toLowerCase().startsWith("image/");
    }

    /**
     * Check if file is a document
     */
    public boolean isDocument() {
        if (fileType == null) return false;
        String type = fileType.toLowerCase();
        return type.contains("pdf") || type.contains("doc") || 
               type.contains("txt") || type.contains("rtf");
    }

    /**
     * Get file extension from filename
     */
    public String getFileExtension() {
        if (originalFileName == null) return "";
        int lastDot = originalFileName.lastIndexOf('.');
        return lastDot > 0 ? originalFileName.substring(lastDot + 1).toLowerCase() : "";
    }
}
