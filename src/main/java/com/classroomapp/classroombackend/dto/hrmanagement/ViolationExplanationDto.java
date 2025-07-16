package com.classroomapp.classroombackend.dto.hrmanagement;

import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ViolationExplanation entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationExplanationDto {
    
    private Long id;
    
    private Long violationId;
    private AttendanceViolationDto violation;
    
    private Long submittedBy;
    private String submittedByName;
    private String submittedByEmail;
    
    private String explanationText;
    
    private ViolationExplanation.ExplanationStatus status;
    private String statusDescription;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String reviewedByName;
    private String reviewNotes;
    
    private Boolean isValid;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Evidence files
    private List<ExplanationEvidenceDto> evidenceFiles;
    private Integer evidenceCount;
    
    // Computed fields
    private Boolean isPendingReview;
    private Boolean isApproved;
    private Boolean isRejected;
    private Boolean canBeEdited;
    private Long daysSinceSubmission;
    private Boolean isOverdueForReview;
    
    /**
     * Get status description
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : null;
    }
    
    /**
     * Check if explanation is pending review
     */
    public Boolean getIsPendingReview() {
        return status == ViolationExplanation.ExplanationStatus.SUBMITTED || 
               status == ViolationExplanation.ExplanationStatus.UNDER_REVIEW;
    }
    
    /**
     * Check if explanation is approved
     */
    public Boolean getIsApproved() {
        return status == ViolationExplanation.ExplanationStatus.APPROVED;
    }
    
    /**
     * Check if explanation is rejected
     */
    public Boolean getIsRejected() {
        return status == ViolationExplanation.ExplanationStatus.REJECTED;
    }
    
    /**
     * Check if explanation can be edited
     */
    public Boolean getCanBeEdited() {
        return status == ViolationExplanation.ExplanationStatus.SUBMITTED || 
               status == ViolationExplanation.ExplanationStatus.REQUIRES_MORE_INFO;
    }
    
    /**
     * Get days since submission
     */
    public Long getDaysSinceSubmission() {
        if (submittedAt == null) return 0L;
        return (long) submittedAt.toLocalDate().until(LocalDateTime.now().toLocalDate()).getDays();
    }
    
    /**
     * Check if explanation is overdue for review (more than 2 days)
     */
    public Boolean getIsOverdueForReview() {
        return getIsPendingReview() && getDaysSinceSubmission() > 2;
    }
    
    /**
     * Get evidence count
     */
    public Integer getEvidenceCount() {
        return evidenceFiles != null ? evidenceFiles.size() : 0;
    }
    
    /**
     * Check if explanation has evidence files
     */
    public Boolean getHasEvidence() {
        return evidenceFiles != null && !evidenceFiles.isEmpty();
    }
    
    /**
     * Get formatted submission info
     */
    public String getSubmissionInfo() {
        StringBuilder info = new StringBuilder();
        
        if (submittedByName != null) {
            info.append("Gửi bởi: ").append(submittedByName);
        }
        
        if (submittedAt != null) {
            info.append(" vào ").append(submittedAt.toLocalDate());
        }
        
        return info.toString();
    }
    
    /**
     * Get formatted review info
     */
    public String getReviewInfo() {
        if (reviewedByName == null || reviewedAt == null) {
            return "Chưa được xem xét";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Xem xét bởi: ").append(reviewedByName)
            .append(" vào ").append(reviewedAt.toLocalDate())
            .append(" - ").append(getStatusDescription());
        
        return info.toString();
    }
    
    /**
     * Get status color for UI
     */
    public String getStatusColor() {
        if (status == null) return "default";
        
        switch (status) {
            case SUBMITTED:
                return "blue";
            case UNDER_REVIEW:
                return "orange";
            case APPROVED:
                return "green";
            case REJECTED:
                return "red";
            case REQUIRES_MORE_INFO:
                return "purple";
            default:
                return "default";
        }
    }
    
    /**
     * Get explanation summary for display
     */
    public String getExplanationSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (submittedByName != null) {
            summary.append(submittedByName).append(" - ");
        }
        
        if (status != null) {
            summary.append(status.getDescription());
        }
        
        if (submittedAt != null) {
            summary.append(" (").append(submittedAt.toLocalDate()).append(")");
        }
        
        return summary.toString();
    }
    
    /**
     * Get truncated explanation text for preview
     */
    public String getTruncatedExplanationText(int maxLength) {
        if (explanationText == null) return "";
        
        if (explanationText.length() <= maxLength) {
            return explanationText;
        }
        
        return explanationText.substring(0, maxLength) + "...";
    }
    
    /**
     * Get explanation text preview (100 characters)
     */
    public String getExplanationPreview() {
        return getTruncatedExplanationText(100);
    }
}
