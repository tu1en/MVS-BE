package com.classroomapp.classroombackend.service.hrmanagement;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.hrmanagement.ExplanationEvidenceDto;

/**
 * Service interface for Accountant Evidence management
 * Extends base evidence functionality with accountant-specific features
 */
public interface AccountantEvidenceService {
    
    /**
     * Upload supporting evidence for attendance violations (Accountant specific)
     * @param violationId violation ID
     * @param file multipart file
     * @param description file description
     * @param evidenceType evidence type
     * @param category business category (PAYROLL, ATTENDANCE, CONTRACT, etc.)
     * @param accountantId accountant user ID
     * @param uploadIp upload IP address
     * @return uploaded evidence DTO
     */
    ExplanationEvidenceDto uploadSupportingEvidence(Long violationId, MultipartFile file, 
                                                   String description, String evidenceType, 
                                                   String category, Long accountantId, String uploadIp);
    
    /**
     * Get evidence files uploaded by specific accountant
     * @param accountantId accountant user ID
     * @param startDate start date filter (optional)
     * @param endDate end date filter (optional)
     * @param evidenceType evidence type filter (optional)
     * @param status status filter (optional)
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getAccountantUploads(Long accountantId, String startDate, 
                                                      String endDate, String evidenceType, String status);
    
    /**
     * Get evidence files by violation ID with accountant context
     * @param violationId violation ID
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceByViolation(Long violationId);
    
    /**
     * Get evidence files pending accountant review
     * @return list of evidence DTOs needing attention
     */
    List<ExplanationEvidenceDto> getPendingAccountantReview();
    
    /**
     * Add accountant notes to evidence file
     * @param evidenceId evidence ID
     * @param notes accountant notes
     * @param accountantId accountant user ID
     * @return updated evidence DTO
     */
    ExplanationEvidenceDto addAccountantNotes(Long evidenceId, String notes, Long accountantId);
    
    /**
     * Mark evidence as reviewed by accountant
     * @param evidenceId evidence ID
     * @param accountantId accountant user ID
     * @return updated evidence DTO
     */
    ExplanationEvidenceDto markAsAccountantReviewed(Long evidenceId, Long accountantId);
    
    /**
     * Get evidence statistics for accountant dashboard
     * @param accountantId accountant user ID
     * @param period time period (week, month, year)
     * @return statistics map
     */
    Map<String, Object> getAccountantEvidenceStatistics(Long accountantId, String period);
    
    /**
     * Export evidence report for accounting purposes
     * @param accountantId accountant user ID
     * @param startDate start date
     * @param endDate end date
     * @param format export format (pdf, excel)
     * @return report data as byte array
     */
    byte[] exportEvidenceReport(Long accountantId, String startDate, String endDate, String format);
    
    /**
     * Bulk upload multiple evidence files
     * @param violationId violation ID
     * @param files array of multipart files
     * @param descriptions array of descriptions (optional)
     * @param evidenceTypes array of evidence types (optional)
     * @param category business category
     * @param accountantId accountant user ID
     * @param uploadIp upload IP address
     * @return list of uploaded evidence DTOs
     */
    List<ExplanationEvidenceDto> bulkUploadEvidence(Long violationId, MultipartFile[] files,
                                                   String[] descriptions, String[] evidenceTypes,
                                                   String category, Long accountantId, String uploadIp);
    
    /**
     * Get evidence files by business category
     * @param category business category
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceByCategory(String category);
    
    /**
     * Delete evidence uploaded by accountant
     * @param evidenceId evidence ID
     * @param accountantId accountant user ID (for authorization)
     */
    void deleteAccountantEvidence(Long evidenceId, Long accountantId);
    
    /**
     * Get evidence file templates for accountants
     * @return list of template information
     */
    List<Map<String, Object>> getEvidenceTemplates();
    
    /**
     * Validate evidence file for accountant upload
     * @param file multipart file
     * @param category business category
     * @return validation result
     */
    boolean validateAccountantEvidence(MultipartFile file, String category);
    
    /**
     * Get evidence upload history for audit trail
     * @param accountantId accountant user ID
     * @param days number of days to look back
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceUploadHistory(Long accountantId, int days);
    
    /**
     * Get monthly evidence summary for accountant
     * @param accountantId accountant user ID
     * @param year year
     * @param month month
     * @return monthly summary data
     */
    Map<String, Object> getMonthlyEvidenceSummary(Long accountantId, int year, int month);
    
    /**
     * Archive old evidence files (older than specified days)
     * @param daysOld number of days
     * @return number of files archived
     */
    int archiveOldEvidence(int daysOld);
    
    /**
     * Get evidence files needing follow-up
     * @param accountantId accountant user ID
     * @return list of evidence DTOs needing attention
     */
    List<ExplanationEvidenceDto> getEvidenceNeedingFollowUp(Long accountantId);
    
    /**
     * Update evidence priority level
     * @param evidenceId evidence ID
     * @param priority priority level (HIGH, MEDIUM, LOW)
     * @param accountantId accountant user ID
     * @return updated evidence DTO
     */
    ExplanationEvidenceDto updateEvidencePriority(Long evidenceId, String priority, Long accountantId);
    
    /**
     * Get evidence files by priority level
     * @param priority priority level
     * @return list of evidence DTOs
     */
    List<ExplanationEvidenceDto> getEvidenceByPriority(String priority);
}