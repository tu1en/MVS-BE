package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classroom_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomEnrollment {

    @EmbeddedId
    private ClassroomEnrollmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("classroomId")
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thêm DEFAULT value cho SQL Server
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ACTIVE'")
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    // Thêm DEFAULT value cho SQL Server
    @Column(name = "enrolled_at", nullable = false, columnDefinition = "DATETIME2(6) DEFAULT GETDATE()")
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "progress_percentage", columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private Double progressPercentage = 0.0;

    @Column(name = "notes", length = 500)
    private String notes;

    // Thêm DEFAULT value cho SQL Server
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2(6) DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2(6) DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    // Constructor with classroom and user
    public ClassroomEnrollment(Classroom classroom, User user) {
        this.id = new ClassroomEnrollmentId(classroom.getId(), user.getId());
        this.classroom = classroom;
        this.user = user;
        this.status = EnrollmentStatus.ACTIVE;
        this.progressPercentage = 0.0;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.enrolledAt == null) {
            this.enrolledAt = now;
        }
        if (this.status == null) {
            this.status = EnrollmentStatus.ACTIVE;
        }
        if (this.progressPercentage == null) {
            this.progressPercentage = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enrollment status enum
    public enum EnrollmentStatus {
        ACTIVE("Active"),
        COMPLETED("Completed"),
        DROPPED("Dropped"),
        SUSPENDED("Suspended"),
        PENDING("Pending");

        private final String displayName;

        EnrollmentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Utility methods
    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return this.status == EnrollmentStatus.COMPLETED;
    }

    public void markCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.progressPercentage = 100.0;
    }

    public void markDropped(String reason) {
        this.status = EnrollmentStatus.DROPPED;
        this.notes = reason;
    }

    public void updateProgress(Double progress) {
        if (progress != null && progress >= 0 && progress <= 100) {
            this.progressPercentage = progress;
            if (progress >= 100.0 && this.status == EnrollmentStatus.ACTIVE) {
                markCompleted();
            }
        }
    }

    // Helper methods
    public String getClassroomName() {
        return this.classroom != null ? this.classroom.getName() : null;
    }

    public String getStudentName() {
        return this.user != null ? this.user.getFullName() : null;
    }

    public String getStudentEmail() {
        return this.user != null ? this.user.getEmail() : null;
    }

    public Long getClassroomId() {
        return this.id != null ? this.id.getClassroomId() : null;
    }

    public Long getUserId() {
        return this.id != null ? this.id.getUserId() : null;
    }
}