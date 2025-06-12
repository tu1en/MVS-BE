package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grading_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @NotNull
    @Column(name = "rubric_id", nullable = false)
    private Long rubricId;

    @NotNull
    @Column(name = "points_awarded", nullable = false, precision = 10, scale = 2)
    private BigDecimal pointsAwarded;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @NotNull
    @Column(name = "graded_by", nullable = false)
    private Long gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt = LocalDateTime.now();

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_id", insertable = false, updatable = false)
    private GradingRubric rubric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by", insertable = false, updatable = false)
    private User grader;
}
