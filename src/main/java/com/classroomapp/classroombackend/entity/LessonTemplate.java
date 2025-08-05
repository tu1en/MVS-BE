package com.classroomapp.classroombackend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "lesson_templates")
@NoArgsConstructor
@AllArgsConstructor
public class LessonTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_template_id", nullable = false)
    private CourseTemplate courseTemplate;
    
    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;
    
    @Column(name = "topic_name", nullable = false, length = 500)
    private String topicName;
    
    @Column(name = "lesson_type", length = 100)
    private String lessonType;
    
    @Column(columnDefinition = "NTEXT")
    private String objectives;
    
    @Column(columnDefinition = "NTEXT")
    private String requirements;
    
    @Column(columnDefinition = "NTEXT")
    private String preparations;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes = 120;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "lessonTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Material> materials = new ArrayList<>();
    
    @OneToMany(mappedBy = "lessonTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassLesson> classLessons = new ArrayList<>();
}