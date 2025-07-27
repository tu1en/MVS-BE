package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Enhanced AttendanceExplanation entity connecting violations with explanations
 * Based on guild.md specifications
 */
@Entity
@Table(name = "attendance_explanations",
       indexes = {
           @Index(name = "idx_explanations_status", columnList = "status"),
           @Index(name = "idx_explanations_staff", columnList = "staff_id"),
           @Index(name = "idx_explanations_review", columnList = "reviewed_by, submitted_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceExplanationEnhanced {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "violation_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_explanations_violation"))
    private AttendanceViolation violation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_explanations_staff"))
    private User staff;
    
    @Column(name = "explanation_text", nullable = false, columnDefinition = "TEXT")
    private String explanationText;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExplanationStatus status = ExplanationStatus.PENDING;
    
    @Column(name = "submitted_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime submittedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", 
                foreignKey = @ForeignKey(name = "fk_explanations_reviewed_by"))
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum ExplanationStatus {
        PENDING("Pending Review"),
        APPROVED("Approved"),
        REJECTED("Rejected");
        
        private final String displayName;
        
        ExplanationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public boolean isPending() {
        return status == ExplanationStatus.PENDING;
    }
    
    public boolean isApproved() {
        return status == ExplanationStatus.APPROVED;
    }
    
    public long getDaysSinceSubmission() {
        return java.time.Duration.between(submittedAt, LocalDateTime.now()).toDays();
    }
    
    public String getDaysSinceSubmissionText() {
        long days = getDaysSinceSubmission();
        if (days == 0) return "Today";
        if (days == 1) return "1 day ago";
        return days + " days ago";
    }
}