package com.classroomapp.classroombackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.entity.*;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Data
@Entity
@Table(name = "materials")
@NoArgsConstructor
@AllArgsConstructor
public class Material {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_template_id")
    private LessonTemplate lessonTemplate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_lesson_id")
    private ClassLesson classLesson;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType materialType;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "NTEXT")
    private String description;
    
    @Column(name = "file_path", length = 1000)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
    
    @Column(name = "is_required")
    private Boolean isRequired = false;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum MaterialType {
        DOCUMENT, VIDEO, EXERCISE, LINK
    }
}