package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Integer overallRating; // 1-5 stars
    
    @Column
    private Integer teachingQualityRating; // 1-5 stars
    
    @Column
    private Integer courseMaterialRating; // 1-5 stars
    
    @Column
    private Integer supportRating; // 1-5 stars
    
    @Column(length = 50)
    private String category = "GENERAL"; // GENERAL, TEACHING, CONTENT, SUPPORT
    
    @Column(length = 50)
    private String status = "SUBMITTED"; // SUBMITTED, REVIEWED, ACKNOWLEDGED
    
    @Column
    private Boolean isAnonymous = false;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column(columnDefinition = "TEXT")
    private String response;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void markAsReviewed(User reviewer, String response) {
        this.reviewedBy = reviewer;
        this.response = response;
        this.reviewedAt = LocalDateTime.now();
        this.status = "REVIEWED";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void acknowledge() {
        this.status = "ACKNOWLEDGED";
        this.updatedAt = LocalDateTime.now();
    }
}
