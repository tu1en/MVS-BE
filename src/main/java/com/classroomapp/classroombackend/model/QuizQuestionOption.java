package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_question_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @NotBlank
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QuizQuestion question;
}
