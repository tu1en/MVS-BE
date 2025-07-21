package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDate;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    // New fields for Classroom & Slot Management
    @Size(max = 50, message = "Classroom code cannot exceed 50 characters")
    @Column(columnDefinition = "NVARCHAR(50)", unique = true)
    private String code;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "NVARCHAR(20) DEFAULT 'DRAFT'")
    private ClassroomStatus status = ClassroomStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    // New relationship for sessions
    @OneToMany(mappedBy = "classroom", cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "classroom"})
    private List<Session> sessions = new ArrayList<>();

    /**
     * Classroom status enumeration
     */
    public enum ClassroomStatus {
        DRAFT("Bản nháp"),
        ACTIVE("Đang hoạt động"),
        COMPLETED("Đã hoàn thành"),
        ARCHIVED("Đã lưu trữ");

        private final String description;

        ClassroomStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Lifecycle callbacks
     */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter and Setter methods
    public String getClassroomName() {
        return name;
    }

    public void setClassroomName(String classroomName) {
        this.name = classroomName;
    }

    public Long getTeacherId() {
        return createdBy != null ? createdBy.getId() : null;
    }

    public void setTeacherId(Long teacherId) {
        // This method is for compatibility with DTO
        // In practice, you should set the createdBy User object
        if (teacherId != null) {
            User teacher = new User();
            teacher.setId(teacherId);
            this.createdBy = teacher;
        } else {
            this.createdBy = null;
        }
    }

    /**
     * Business logic methods
     */

    /**
     * Check if classroom can be modified
     */
    public boolean canBeModified() {
        return status == ClassroomStatus.DRAFT || status == ClassroomStatus.ACTIVE;
    }

    /**
     * Check if classroom can be deleted
     */
    public boolean canBeDeleted() {
        return status == ClassroomStatus.DRAFT &&
               (sessions == null || sessions.isEmpty()) &&
               (enrollments == null || enrollments.isEmpty());
    }

    /**
     * Check if sessions can be added to this classroom
     */
    public boolean canAddSessions() {
        return status == ClassroomStatus.DRAFT || status == ClassroomStatus.ACTIVE;
    }

    /**
     * Validate status transition
     */
    public boolean isValidStatusTransition(ClassroomStatus newStatus) {
        if (status == newStatus) return true;

        switch (status) {
            case DRAFT:
                return newStatus == ClassroomStatus.ACTIVE || newStatus == ClassroomStatus.ARCHIVED;
            case ACTIVE:
                return newStatus == ClassroomStatus.COMPLETED || newStatus == ClassroomStatus.ARCHIVED;
            case COMPLETED:
                return newStatus == ClassroomStatus.ARCHIVED;
            case ARCHIVED:
                return false; // Cannot transition from ARCHIVED
            default:
                return false;
        }
    }

    /**
     * Update status with validation
     */
    public void updateStatus(ClassroomStatus newStatus) {
        if (!isValidStatusTransition(newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get total number of sessions
     */
    public int getTotalSessions() {
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Get number of completed sessions
     */
    public long getCompletedSessionsCount() {
        if (sessions == null) return 0;
        return sessions.stream()
                .filter(session -> session.getStatus() == Session.SessionStatus.COMPLETED)
                .count();
    }

    /**
     * Calculate classroom progress percentage
     */
    public double getProgressPercentage() {
        if (sessions == null || sessions.isEmpty()) return 0.0;
        return (double) getCompletedSessionsCount() / sessions.size() * 100.0;
    }

    /**
     * Check if classroom is within date range
     */
    public boolean isWithinDateRange(LocalDate date) {
        if (startDate == null || endDate == null) return true;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    // Convenience methods for status checking
    public boolean isDraft() { return status == ClassroomStatus.DRAFT; }
    public boolean isActive() { return status == ClassroomStatus.ACTIVE; }
    public boolean isCompleted() { return status == ClassroomStatus.COMPLETED; }
    public boolean isArchived() { return status == ClassroomStatus.ARCHIVED; }
}
