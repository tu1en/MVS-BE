package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shift_swap_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftSwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // NgÆ°á»i yÃªu cáº§u

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetEmployee; // NgÆ°á»i Ä‘Æ°á»£c yÃªu cáº§u Ä‘á»•i ca

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_assignment_id", nullable = false)
    private ShiftAssignment requesterAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_assignment_id", nullable = false)
    private ShiftAssignment targetAssignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SwapStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Column(name = "request_reason")
    private String requestReason;

    @Builder.Default
    @Column(name = "is_emergency")
    private Boolean isEmergency = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_response")
    private ManagerResponse managerResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_response")
    private TargetResponse targetResponse;

    @Column(name = "manager_reason")
    private String managerReason;

    @Column(name = "target_reason")
    private String targetReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    // âœ… ENUMS
    public enum SwapStatus {
        PENDING,    // Äang chá»
        ACCEPTED,   // Target Ä‘Ã£ cháº¥p nháº­n
        REJECTED,   // Target tá»« chá»‘i
        APPROVED,   // Manager Ä‘Ã£ duyá»‡t
        DECLINED,   // Manager tá»« chá»‘i
        CANCELLED,  // NgÆ°á»i yÃªu cáº§u há»§y
        EXPIRED     // Háº¿t háº¡n
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum TargetResponse {
        ACCEPTED, REJECTED
    }

    public enum ManagerResponse {
        APPROVED, DECLINED
    }

    // âœ… METHODS Ä‘Æ°á»£c gá»i trong ServiceImpl
    public ShiftAssignment getRequesterAssignment() {
        return requesterAssignment;
    }

    public ShiftAssignment getTargetAssignment() {
        return targetAssignment;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public Boolean getIsEmergency() {
        return isEmergency != null && isEmergency;
    }

    public void cancel() {
        this.status = SwapStatus.CANCELLED;
    }

    public void respondByTarget(TargetResponse response, String reason) {
        this.targetResponse = response;
        this.targetReason = reason;
        if (response == TargetResponse.ACCEPTED) {
            this.status = SwapStatus.ACCEPTED;
        } else {
            this.status = SwapStatus.REJECTED;
        }
    }

    public void approveByManager(User manager, ManagerResponse response, String reason) {
        this.managerResponse = response;
        this.managerReason = reason;
        this.approvedBy = manager;
        if (response == ManagerResponse.APPROVED) {
            this.status = SwapStatus.APPROVED;
        } else {
            this.status = SwapStatus.DECLINED;
        }
    }

    public boolean isValidRequest() {
        return requester != null && targetEmployee != null &&
                requesterAssignment != null && targetAssignment != null &&
                !requester.getId().equals(targetEmployee.getId());
    }

    // Missing methods for Firebase integration
    public String getRequestType() {
        return isEmergency != null && isEmergency ? "EMERGENCY" : "NORMAL";
    }

    public String getTargetResponseReason() {
        return targetReason;
    }

    public LocalDateTime getTargetRespondedAt() {
        return updatedAt;
    }

    public String getManagerResponseReason() {
        return managerReason;
    }

    public LocalDateTime getApprovedAt() {
        return updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiryTime;
    }
    
    // Additional setter methods for compatibility
    public void setReason(String reason) {
        this.requestReason = reason;
    }
    
    public void setRequestTime(LocalDateTime requestTime) {
        this.createdAt = requestTime;
    }
}
