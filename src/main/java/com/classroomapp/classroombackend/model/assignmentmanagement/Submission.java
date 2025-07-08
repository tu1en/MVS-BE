package com.classroomapp.classroombackend.model.assignmentmanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"assignment_id", "student_id"},
           name = "uk_submission_assignment_student"
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The assignment this submission is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    // The student who submitted
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(length = 2000, columnDefinition = "NVARCHAR(2000)")
    private String comment;
    
    private LocalDateTime submittedAt;

    @OneToMany(
            mappedBy = "submission",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SubmissionAttachment> attachments = new ArrayList<>();
    
    // Grading information
    @Min(0) // Score must be non-negative
    private Integer score;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String feedback;
    
    private LocalDateTime gradedAt;
    
    // The teacher who graded the submission
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_id")
    private User gradedBy;

    public Submission(Assignment assignment, User student) {
        this.assignment = assignment;
        this.student = student;
    }

    public void addAttachment(SubmissionAttachment attachment) {
        attachments.add(attachment);
        attachment.setSubmission(this);
    }

    public void removeAttachment(SubmissionAttachment attachment) {
        attachments.remove(attachment);
        attachment.setSubmission(null);
    }
}
