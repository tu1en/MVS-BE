package com.classroomapp.classroombackend.model.exammangement;

import java.time.Instant;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

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

@Entity
@Table(name = "exams")
@Data
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(nullable = false)
    private Instant startTime; // The time when the exam becomes available

    @Column(nullable = false)
    private Instant endTime;   // The time when the exam is no longer available

    @Column(nullable = false)
    private Integer durationInMinutes; // The duration students have to complete the exam
} 