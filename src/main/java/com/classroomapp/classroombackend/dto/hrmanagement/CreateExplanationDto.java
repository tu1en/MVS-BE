package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO for creating violation explanations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExplanationDto {
    
    @NotNull(message = "ID vi phạm không được để trống")
    private Long violationId;
    
    @NotBlank(message = "Lý do giải trình không được để trống")
    @Size(min = 10, max = 2000, message = "Lý do giải trình phải từ 10-2000 ký tự")
    private String explanationText;
    
    // Evidence files (optional but recommended)
    private List<MultipartFile> evidenceFiles;
    
    // Evidence descriptions (optional)
    private List<String> evidenceDescriptions;
    
    // Evidence types (optional)
    private List<String> evidenceTypes;
    
    /**
     * Validate evidence files
     */
    public boolean hasEvidenceFiles() {
        return evidenceFiles != null && !evidenceFiles.isEmpty();
    }
    
    /**
     * Get evidence file count
     */
    public int getEvidenceFileCount() {
        return evidenceFiles != null ? evidenceFiles.size() : 0;
    }
    
    /**
     * Validate evidence data consistency
     */
    public boolean isEvidenceDataConsistent() {
        int fileCount = getEvidenceFileCount();
        
        if (fileCount == 0) {
            return true; // No evidence is valid
        }
        
        // If descriptions are provided, count should match
        if (evidenceDescriptions != null && evidenceDescriptions.size() != fileCount) {
            return false;
        }
        
        // If types are provided, count should match
        if (evidenceTypes != null && evidenceTypes.size() != fileCount) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get total file size in bytes
     */
    public long getTotalFileSize() {
        if (evidenceFiles == null) return 0;
        
        return evidenceFiles.stream()
                .mapToLong(MultipartFile::getSize)
                .sum();
    }
    
    /**
     * Check if total file size is within limit (5MB)
     */
    public boolean isWithinSizeLimit() {
        long maxSizeBytes = 5 * 1024 * 1024; // 5MB
        return getTotalFileSize() <= maxSizeBytes;
    }
    
    /**
     * Get formatted total file size
     */
    public String getFormattedTotalFileSize() {
        long totalSize = getTotalFileSize();
        
        if (totalSize < 1024) {
            return totalSize + " B";
        } else if (totalSize < 1024 * 1024) {
            return String.format("%.1f KB", totalSize / 1024.0);
        } else {
            return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Validate file types
     */
    public boolean hasValidFileTypes() {
        if (evidenceFiles == null) return true;
        
        String[] allowedTypes = {
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        };
        
        for (MultipartFile file : evidenceFiles) {
            String contentType = file.getContentType();
            boolean isAllowed = false;
            
            for (String allowedType : allowedTypes) {
                if (allowedType.equals(contentType)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (!isAllowed) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get invalid file types
     */
    public List<String> getInvalidFileTypes() {
        if (evidenceFiles == null) return List.of();
        
        String[] allowedTypes = {
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        };
        
        return evidenceFiles.stream()
                .map(MultipartFile::getContentType)
                .filter(contentType -> {
                    for (String allowedType : allowedTypes) {
                        if (allowedType.equals(contentType)) {
                            return false;
                        }
                    }
                    return true;
                })
                .distinct()
                .toList();
    }
    
    /**
     * Check if any file is too large (individual file limit: 2MB)
     */
    public boolean hasOversizedFiles() {
        if (evidenceFiles == null) return false;
        
        long maxFileSize = 2 * 1024 * 1024; // 2MB per file
        
        return evidenceFiles.stream()
                .anyMatch(file -> file.getSize() > maxFileSize);
    }
    
    /**
     * Get oversized file names
     */
    public List<String> getOversizedFileNames() {
        if (evidenceFiles == null) return List.of();
        
        long maxFileSize = 2 * 1024 * 1024; // 2MB per file
        
        return evidenceFiles.stream()
                .filter(file -> file.getSize() > maxFileSize)
                .map(MultipartFile::getOriginalFilename)
                .toList();
    }
    
    /**
     * Validate all constraints
     */
    public boolean isValid() {
        return isEvidenceDataConsistent() && 
               isWithinSizeLimit() && 
               hasValidFileTypes() && 
               !hasOversizedFiles();
    }
    
    /**
     * Get validation error messages
     */
    public List<String> getValidationErrors() {
        List<String> errors = new java.util.ArrayList<>();
        
        if (!isEvidenceDataConsistent()) {
            errors.add("Số lượng file và mô tả không khớp");
        }
        
        if (!isWithinSizeLimit()) {
            errors.add("Tổng dung lượng file vượt quá 5MB");
        }
        
        if (!hasValidFileTypes()) {
            errors.add("Có file không đúng định dạng cho phép: " + String.join(", ", getInvalidFileTypes()));
        }
        
        if (hasOversizedFiles()) {
            errors.add("Có file vượt quá 2MB: " + String.join(", ", getOversizedFileNames()));
        }
        
        return errors;
    }
}
