package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classrooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;
    
    @Column(columnDefinition = "NVARCHAR(50)")
    private String section;
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String subject;
    
    // ADD STATUS FIELD
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ACTIVE'")
    private ClassroomStatus status = ClassroomStatus.ACTIVE;
    
    // The teacher who created/owns this classroom
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User teacher;
    
    @Column(name = "course_id")
    private Long courseId;
    
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private Set<ClassroomEnrollment> enrollments = new HashSet<>();
    
    public Set<User> getStudents() {
        Set<User> students = new HashSet<>();
        for (ClassroomEnrollment enrollment : this.enrollments) {
            students.add(enrollment.getUser());
        }
        return students;
    }
   
    // Syllabus for this classroom - one classroom has one syllabus
    @OneToOne(mappedBy = "classroom", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "classroom"})
    private Syllabus syllabus;
   
    // Schedules for this classroom - one classroom has many schedule entries
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();
    
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<Lecture> lectures = new ArrayList<>();
   
    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;
   
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Assignment relationship - assuming it exists based on mapper
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<com.classroomapp.classroombackend.model.assignmentmanagement.Assignment> assignments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ClassroomStatus.ACTIVE;
        }
    }
   
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Status enum
    public enum ClassroomStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        ARCHIVED("Archived"),
        DELETED("Deleted");
        
        private final String displayName;
        
        ClassroomStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Utility methods for status
    public boolean isActive() {
        return this.status == ClassroomStatus.ACTIVE;
    }
    
    public void activate() {
        this.status = ClassroomStatus.ACTIVE;
    }
    
    public void deactivate() {
        this.status = ClassroomStatus.INACTIVE;
    }
    
    public void archive() {
        this.status = ClassroomStatus.ARCHIVED;
    }
    
    // Existing getters - keep these
    public String getName() { return name; }
    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<com.classroomapp.classroombackend.model.assignmentmanagement.Assignment> getAssignments() { return assignments; }
}