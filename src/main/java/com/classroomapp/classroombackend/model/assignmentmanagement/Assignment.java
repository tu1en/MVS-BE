package com.classroomapp.classroombackend.model.assignmentmanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    @Column(name = "due_date", nullable = false)
    // @FutureOrPresent(message = "Due date must be present or future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
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
