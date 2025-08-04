package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "course_templates")
@Data
public class CourseTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    private String subject;
    
    @Column(name = "total_weeks")
    private Integer totalWeeks = 0;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private TemplateStatus status = TemplateStatus.DRAFT;
    
    @OneToMany(mappedBy = "courseTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LectureTemplate> lectureTemplates = new ArrayList<>();
    
    @OneToMany(mappedBy = "courseTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<CourseMaterialTemplate> materials = new ArrayList<>();
}
