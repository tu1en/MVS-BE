package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateExplanationDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ViolationExplanationDto;
import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for ViolationExplanation management
 */
public interface ViolationExplanationService {
    
    /**
     * Submit explanation for a violation
     * @param createDto explanation data with evidence files
     * @param submittedBy user ID who submits the explanation
     * @return created explanation DTO
     */
    ViolationExplanationDto submitExplanation(CreateExplanationDto createDto, Long submittedBy);
    
    /**
     * Update an existing explanation
     * @param id explanation ID
     * @param updateDto updated explanation data
     * @param submittedBy user ID who updates
     * @return updated explanation DTO
     */
    ViolationExplanationDto updateExplanation(Long id, CreateExplanationDto updateDto, Long submittedBy);
    
    /**
     * Get explanation by ID
     * @param id explanation ID
     * @return explanation DTO
     */
    ViolationExplanationDto getExplanationById(Long id);
    
    /**
     * Get explanations by user (submitted by)
     * @param userId user ID
     * @param pageable pagination parameters
     * @return page of explanations
     */
    Page<ViolationExplanationDto> getExplanationsByUser(Long userId, Pageable pageable);
    
    /**
     * Get explanations by violation
     * @param violationId violation ID
     * @return list of explanations for the violation
     */
    List<ViolationExplanationDto> getExplanationsByViolation(Long violationId);
    
    /**
     * Get explanations pending review
     * @param pageable pagination parameters
     * @return page of explanations pending review
     */
    Page<ViolationExplanationDto> getPendingReviewExplanations(Pageable pageable);
    
    /**
     * Get explanations by status
     * @param status explanation status
     * @param pageable pagination parameters
     * @return page of explanations with the status
     */
    Page<ViolationExplanationDto> getExplanationsByStatus(ViolationExplanation.ExplanationStatus status, Pageable pageable);
    
    /**
     * Approve an explanation
     * @param id explanation ID
     * @param reviewedBy user ID who approves
     * @param reviewNotes optional review notes
     * @return updated explanation DTO
     */
    ViolationExplanationDto approveExplanation(Long id, Long reviewedBy, String reviewNotes);
    
    /**
     * Reject an explanation
     * @param id explanation ID
     * @param reviewedBy user ID who rejects
     * @param reviewNotes rejection reason
     * @return updated explanation DTO
     */
    ViolationExplanationDto rejectExplanation(Long id, Long reviewedBy, String reviewNotes);
    
    /**
     * Request more information for an explanation
     * @param id explanation ID
     * @param reviewedBy user ID who requests more info
     * @param reviewNotes what additional info is needed
     * @return updated explanation DTO
     */
    ViolationExplanationDto requestMoreInfo(Long id, Long reviewedBy, String reviewNotes);
    
    /**
     * Delete an explanation (soft delete)
     * @param id explanation ID
     * @param deletedBy user ID who deletes
     */
    void deleteExplanation(Long id, Long deletedBy);
    
    /**
     * Get overdue explanations (submitted but not reviewed)
     * @param daysSince days since submission
     * @return list of overdue explanations
     */
    List<ViolationExplanationDto> getOverdueExplanations(int daysSince);
    
    /**
     * Get explanation statistics by status
     * @param startDate start date
     * @param endDate end date
     * @return explanation statistics
     */
    Object getExplanationStatisticsByStatus(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get explanation statistics by user
     * @param startDate start date
     * @param endDate end date
     * @return explanation statistics by user
     */
    Object getExplanationStatisticsByUser(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Check if user can submit explanation for violation
     * @param violationId violation ID
     * @param userId user ID
     * @return true if user can submit explanation
     */
    boolean canSubmitExplanation(Long violationId, Long userId);
    
    /**
     * Check if user can edit explanation
     * @param explanationId explanation ID
     * @param userId user ID
     * @return true if user can edit explanation
     */
    boolean canEditExplanation(Long explanationId, Long userId);
    
    /**
     * Check if user can review explanation
     * @param explanationId explanation ID
     * @param userId user ID
     * @return true if user can review explanation
     */
    boolean canReviewExplanation(Long explanationId, Long userId);
    
    /**
     * Get latest explanation for a violation
     * @param violationId violation ID
     * @return latest explanation DTO or null
     */
    ViolationExplanationDto getLatestExplanationForViolation(Long violationId);
    
    /**
     * Count explanations by status
     * @param status explanation status
     * @return count of explanations
     */
    long countExplanationsByStatus(ViolationExplanation.ExplanationStatus status);
    
    /**
     * Count explanations by user
     * @param userId user ID
     * @return count of explanations submitted by user
     */
    long countExplanationsByUser(Long userId);
    
    /**
     * Get explanations with evidence files
     * @return list of explanations that have evidence files
     */
    List<ViolationExplanationDto> getExplanationsWithEvidence();
    
    /**
     * Get explanations without evidence files
     * @return list of explanations without evidence files
     */
    List<ViolationExplanationDto> getExplanationsWithoutEvidence();
    
    /**
     * Validate explanation data
     * @param createDto explanation data to validate
     * @return validation result
     */
    boolean validateExplanationData(CreateExplanationDto createDto);
    
    /**
     * Get monthly explanation summary
     * @param year year
     * @param month month
     * @return monthly summary
     */
    Object getMonthlyExplanationSummary(int year, int month);
}
