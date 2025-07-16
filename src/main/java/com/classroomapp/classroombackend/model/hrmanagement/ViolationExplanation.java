package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing explanations submitted by staff for attendance violations
 */
@Entity
@Table(name = "violation_explanations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViolationExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Vi phạm không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "violation_id", nullable = false)
    private AttendanceViolation violation;

    @NotNull(message = "Người gửi giải trình không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @NotBlank(message = "Lý do giải trình không được để trống")
    @Column(name = "explanation_text", columnDefinition = "NVARCHAR(2000)", nullable = false)
    private String explanationText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExplanationStatus status = ExplanationStatus.SUBMITTED;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "review_notes", columnDefinition = "NVARCHAR(1000)")
    private String reviewNotes;

    @Column(name = "is_valid", columnDefinition = "BIT")
    private Boolean isValid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-many relationship with evidence files
    @OneToMany(mappedBy = "explanation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExplanationEvidence> evidenceFiles;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum for explanation status
     */
    public enum ExplanationStatus {
        SUBMITTED("Đã gửi"),
        UNDER_REVIEW("Đang xem xét"),
        APPROVED("Đã duyệt"),
        REJECTED("Bị từ chối"),
        REQUIRES_MORE_INFO("Cần thêm thông tin");

        private final String description;

        ExplanationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Check if explanation is pending review
     * @return true if submitted but not reviewed
     */
    public boolean isPendingReview() {
        return status == ExplanationStatus.SUBMITTED || status == ExplanationStatus.UNDER_REVIEW;
    }

    /**
     * Check if explanation is approved
     * @return true if approved
     */
    public boolean isApproved() {
        return status == ExplanationStatus.APPROVED;
    }

    /**
     * Check if explanation is rejected
     * @return true if rejected
     */
    public boolean isRejected() {
        return status == ExplanationStatus.REJECTED;
    }

    /**
     * Check if explanation can be edited
     * @return true if can be edited
     */
    public boolean canBeEdited() {
        return status == ExplanationStatus.SUBMITTED || 
               status == ExplanationStatus.REQUIRES_MORE_INFO;
    }

    /**
     * Approve the explanation
     * @param reviewedBy user who approved
     * @param reviewNotes optional review notes
     */
    public void approve(User reviewedBy, String reviewNotes) {
        this.status = ExplanationStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
        this.isValid = true;
    }

    /**
     * Reject the explanation
     * @param reviewedBy user who rejected
     * @param reviewNotes rejection reason
     */
    public void reject(User reviewedBy, String reviewNotes) {
        this.status = ExplanationStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
        this.isValid = false;
    }

    /**
     * Request more information
     * @param reviewedBy user who requested more info
     * @param reviewNotes what additional info is needed
     */
    public void requestMoreInfo(User reviewedBy, String reviewNotes) {
        this.status = ExplanationStatus.REQUIRES_MORE_INFO;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
    }

    /**
     * Get days since submission
     * @return number of days
     */
    public long getDaysSinceSubmission() {
        if (submittedAt == null) return 0;
        return submittedAt.toLocalDate().until(LocalDateTime.now().toLocalDate()).getDays();
    }

    /**
     * Check if explanation is overdue for review
     * @param maxDays maximum days for review
     * @return true if overdue
     */
    public boolean isOverdueForReview(int maxDays) {
        return isPendingReview() && getDaysSinceSubmission() > maxDays;
    }

    /**
     * Get formatted submission info
     * @return formatted string with submitter and date
     */
    public String getSubmissionInfo() {
        StringBuilder info = new StringBuilder();
        
        if (submittedBy != null) {
            info.append("Gửi bởi: ").append(submittedBy.getFullName());
        }
        
        if (submittedAt != null) {
            info.append(" vào ").append(submittedAt.toLocalDate());
        }
        
        return info.toString();
    }

    /**
     * Get formatted review info
     * @return formatted string with reviewer and date
     */
    public String getReviewInfo() {
        if (reviewedBy == null || reviewedAt == null) {
            return "Chưa được xem xét";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Xem xét bởi: ").append(reviewedBy.getFullName())
            .append(" vào ").append(reviewedAt.toLocalDate())
            .append(" - ").append(status.getDescription());
        
        return info.toString();
    }

    /**
     * Check if explanation has evidence files
     * @return true if has evidence
     */
    public boolean hasEvidence() {
        return evidenceFiles != null && !evidenceFiles.isEmpty();
    }

    /**
     * Get count of evidence files
     * @return number of evidence files
     */
    public int getEvidenceCount() {
        return evidenceFiles != null ? evidenceFiles.size() : 0;
    }
}
