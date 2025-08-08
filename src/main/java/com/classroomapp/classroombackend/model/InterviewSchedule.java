package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private RecruitmentApplication application;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", columnDefinition = "NVARCHAR(50)")
    private String status = "SCHEDULED"; // SCHEDULED, DONE, PENDING, ACCEPTED, REJECTED

    @Column(name = "result", columnDefinition = "NVARCHAR(MAX)")
    private String result;

    @Column(name = "offer", columnDefinition = "NVARCHAR(MAX)")
    private String offer;

    @Column(name = "evaluation", columnDefinition = "NVARCHAR(MAX)")
    private String evaluation;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;
} 