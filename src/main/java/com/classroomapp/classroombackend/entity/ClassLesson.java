package com.classroomapp.classroombackend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "class_lessons")
@NoArgsConstructor
@AllArgsConstructor
public class ClassLesson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_template_id", nullable = false)
    private LessonTemplate lessonTemplate;
    
    @Column(name = "actual_date")
    private LocalDate actualDate;
    
    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;
    
    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status = LessonStatus.SCHEDULED;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String notes;
    
    @Column(name = "attendance_count")
    private Integer attendanceCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "classLesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Material> materials;
    
    public enum LessonStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}