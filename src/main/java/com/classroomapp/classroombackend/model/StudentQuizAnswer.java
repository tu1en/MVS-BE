package com.classroomapp.classroombackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_quiz_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @NotNull
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_options", columnDefinition = "JSON")
    private String selectedOptions; // JSON array for multiple choice

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "points_earned", precision = 10, scale = 2)
    private BigDecimal pointsEarned = BigDecimal.ZERO;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt = LocalDateTime.now();

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QuizQuestion question;
}
