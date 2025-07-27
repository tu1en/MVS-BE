package com.classroomapp.classroombackend.model.classroommanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.entity.enumeration.CourseStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;

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
import jakarta.persistence.Table;
import lombok.Data; // Import Lombok's Data annotation

@Entity
@Table(name = "courses")
@Data // Use Lombok to generate getters, setters, toString, equals, and hashCode methods
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(255)") // Updated columnDefinition
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_students")
    private Integer maxStudents = 30;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
} 