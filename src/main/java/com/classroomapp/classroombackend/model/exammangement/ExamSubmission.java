package com.classroomapp.classroombackend.model.exammangement;

import java.time.Instant;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_submissions")
@Data
@NoArgsConstructor
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Instant startedAt; // Time when the student started the exam

    private Instant submittedAt; // Time when the student submitted the exam

    @Column(length = 2000)
    private String content; // Could be answers, comments, etc.

    // Similar to SubmissionAttachment, you might want a separate entity for attachments
    // @OneToMany(mappedBy = "examSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ExamSubmissionAttachment> attachments = new ArrayList<>();

    // Grading information
    private Integer score;
    private String feedback;
    private Instant gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_id")
    private User gradedBy;

    public ExamSubmission(Exam exam, User student, Instant startedAt) {
        this.exam = exam;
        this.student = student;
        this.startedAt = startedAt;
    }
} 