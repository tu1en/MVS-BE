package com.classroomapp.classroombackend.model.classroommanagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.model.classroommanagement.TemplateStatus;
import com.classroomapp.classroombackend.entity.LessonTemplate;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "NTEXT") // Using NTEXT for SQL Server compatibility
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
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // New fields for public enrollment
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "enrollment_fee", precision = 10, scale = 2)
    private BigDecimal enrollmentFee = BigDecimal.ZERO;
    
    @Column(name = "max_students_per_template")
    private Integer maxStudentsPerTemplate;
    
    // Include both relationship types - you can remove the ones you don't need
    @OneToMany(mappedBy = "courseTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LectureTemplate> lectureTemplates = new ArrayList<>();
    
    @OneToMany(mappedBy = "courseTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseMaterialTemplate> materials = new ArrayList<>();
    
    @OneToMany(mappedBy = "courseTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LessonTemplate> lessonTemplates = new ArrayList<>();
    
    @OneToMany(mappedBy = "courseTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassEntity> classes = new ArrayList<>();
}