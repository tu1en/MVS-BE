package com.classroomapp.classroombackend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;

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
@Table(name = "classes")
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_template_id", nullable = false)
    private CourseTemplate courseTemplate;
    
    @Column(name = "class_name", nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
    private String className;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "schedule_json")
    private String scheduleJson;
    
    @Column(name = "max_students")
    private Integer maxStudents = 30;
    
    @Column(name = "current_students")
    private Integer currentStudents = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status = ClassStatus.PLANNING;
    
    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassLesson> classLessons = new ArrayList<>();
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Public visibility and tuition fee for the class
    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "tuition_fee", precision = 10, scale = 2)
    private BigDecimal tuitionFee = BigDecimal.ZERO;

    public enum ClassStatus {
        PLANNING, ACTIVE, COMPLETED, CANCELLED
    }
}