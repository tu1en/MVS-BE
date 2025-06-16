package com.classroomapp.classroombackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.StudentProgress.ProgressType;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull
    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "assignment_id")
    private Long assignmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "progress_type", nullable = false)
    private ProgressType progressType;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @Column(name = "points_earned", precision = 10, scale = 2)
    private BigDecimal pointsEarned = BigDecimal.ZERO;

    @Column(name = "max_points", precision = 10, scale = 2)
    private BigDecimal maxPoints = BigDecimal.ZERO;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed = LocalDateTime.now();

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", insertable = false, updatable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;

    public enum ProgressType {
        ASSIGNMENT, LECTURE, QUIZ, OVERALL
    }
}
