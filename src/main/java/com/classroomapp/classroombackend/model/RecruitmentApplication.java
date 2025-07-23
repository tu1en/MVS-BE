package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_position_id", nullable = false)
    private JobPosition jobPosition;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "cv_url", columnDefinition = "TEXT")
    private String cvUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status", columnDefinition = "NVARCHAR(50)")
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, INTERVIEW, ...

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;
} 