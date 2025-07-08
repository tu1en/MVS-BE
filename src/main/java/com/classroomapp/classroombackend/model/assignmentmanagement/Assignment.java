package com.classroomapp.classroombackend.model.assignmentmanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(columnDefinition = "NVARCHAR(255)")
    private String title;

    @Lob
    @Column(length = 2000, columnDefinition = "NTEXT")
    private String description;

    @NotNull
    @Future // Must be a future date
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Min(0) // Points must be positive
    private Integer points;

    @OneToMany(
            mappedBy = "assignment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AssignmentAttachment> attachments = new ArrayList<>();

    // The classroom this assignment belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    public void addAttachment(AssignmentAttachment attachment) {
        attachments.add(attachment);
        attachment.setAssignment(this);
    }

    public void removeAttachment(AssignmentAttachment attachment) {
        attachments.remove(attachment);
        attachment.setAssignment(null);
    }
}
