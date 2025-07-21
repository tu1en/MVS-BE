package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shift_swap_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSwapController {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Các field khác
    private String reason;
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    private SwapStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private com.classroomapp.classroombackend.model.usermanagement.User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_employee_id")
    private com.classroomapp.classroombackend.model.usermanagement.User targetEmployee;

    public enum SwapStatus {
        PENDING_TARGET,
        PENDING_MANAGER,
        APPROVED,
        REJECTED,
        CANCELLED,
        EXPIRED
    }

    public enum Priority {
        NORMAL,
        HIGH
    }

    public enum TargetResponse {
        ACCEPT,
        REJECT
    }

    public enum ManagerResponse {
        APPROVE,
        DECLINE
    }
}
