package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "lecture_templates")
@Data
public class LectureTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_template_id", nullable = false)
    private CourseTemplate courseTemplate;
    
    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 100)
    private String type;
    
    @Column(columnDefinition = "TEXT")
    private String purpose;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    @Column(columnDefinition = "TEXT")
    private String preparation;
    
    @Column(name = "order_index")
    private Integer orderIndex = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
