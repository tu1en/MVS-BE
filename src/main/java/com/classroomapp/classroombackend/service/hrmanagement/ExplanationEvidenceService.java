package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateExplanationDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ExplanationEvidenceDto;
import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for ExplanationEvidence management
 */
public interface ExplanationEvidenceService {
    
    /**
     * Upload evidence files for an explanation
     * @param explanationId explanation ID
     * @param createDto explanation data with files
     * @return list of uploaded evidence DTOs
     */
    List<ExplanationEvidenceDto> uploadEvidenceFiles(Long explanationId, CreateExplanationDto createDto);
    
    /**
     * Upload single evidence file
     * @param explanationId explanation ID
     * @param file multipart file
     * @param description file description
     * @param evidenceType evidence type
     * @param uploadIp upload IP address
     * @return uploaded evidence DTO
     */
    ExplanationEvidenceDto uploadSingleEvidence(Long explanationId, MultipartFile file, 
                                               String description, String evidenceType, String uploadIp);
    
    /**
     * Get evidence files by explanation ID
     * @param explanationId explanation ID
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceByExplanation(Long explanationId);
    
    /**
     * Get evidence file by ID
     * @param id evidence ID
     * @return evidence DTO
     */
    ExplanationEvidenceDto getEvidenceById(Long id);
    
    /**
     * Delete evidence file
     * @param id evidence ID
     * @param deletedBy user ID who deletes
     */
    void deleteEvidence(Long id, Long deletedBy);
    
    /**
     * Delete all evidence files for an explanation
     * @param explanationId explanation ID
     */
    void deleteEvidenceByExplanation(Long explanationId);
    
    /**
     * Verify evidence file
     * @param id evidence ID
     * @param verifiedBy user ID who verifies
     * @return updated evidence DTO
     */
    ExplanationEvidenceDto verifyEvidence(Long id, Long verifiedBy);
    
    /**
     * Get evidence files by type
     * @param evidenceType evidence type
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceByType(ExplanationEvidence.EvidenceType evidenceType);
    
    /**
     * Get unverified evidence files
     * @return list of unverified evidence DTOs
     */
    List<ExplanationEvidenceDto> getUnverifiedEvidence();
    
    /**
     * Get evidence files by date range
     * @param startDate start date (ISO format)
     * @param endDate end date (ISO format)
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceByDateRange(String startDate, String endDate);
    
    /**
     * Get file statistics by type
     * @return file statistics
     */
    Object getFileStatisticsByType();
    
    /**
     * Get total file size for an explanation
     * @param explanationId explanation ID
     * @return total file size in bytes
     */
    Long getTotalFileSizeByExplanation(Long explanationId);
    
    /**
     * Validate file before upload
     * @param file multipart file
     * @return validation result
     */
    boolean validateFile(MultipartFile file);
    
    /**
     * Get validation errors for file
     * @param file multipart file
     * @return list of validation errors
     */
    List<String> getFileValidationErrors(MultipartFile file);
    
    /**
     * Generate secure download URL for evidence file
     * @param id evidence ID
     * @param userId user requesting download
     * @return secure download URL
     */
    String generateDownloadUrl(Long id, Long userId);
    
    /**
     * Check if user can access evidence file
     * @param evidenceId evidence ID
     * @param userId user ID
     * @return true if user can access
     */
    boolean canAccessEvidence(Long evidenceId, Long userId);
    
    /**
     * Get monthly upload statistics
     * @param year year
     * @param month month
     * @return monthly statistics
     */
    Object getMonthlyUploadStatistics(int year, int month);
    
    /**
     * Clean up orphaned files (files without explanation)
     * @return number of files cleaned up
     */
    int cleanupOrphanedFiles();
    
    /**
     * Get large files (above certain size)
     * @param minSizeMB minimum size in MB
     * @return list of large files
     */
    List<ExplanationEvidenceDto> getLargeFiles(int minSizeMB);
}
