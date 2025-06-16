package com.classroomapp.classroombackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grading_rubrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradingRubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @NotBlank
    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "max_points", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPoints;

    @Column(name = "weight_percentage", precision = 5, scale = 2)
    private BigDecimal weightPercentage = BigDecimal.valueOf(100.00);

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;
}
