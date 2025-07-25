package com.classroomapp.classroombackend.entity;

import com.classroomapp.classroombackend.entity.enumeration.CourseTeacherStatus;
import com.classroomapp.classroombackend.entity.enumeration.TeacherRole;
import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing teacher assignment to courses
 * Phân công giảng viên cho khóa học
 */
@Entity
@Table(name = "course_teachers",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"course_id", "teacher_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private TeacherRole role = TeacherRole.MAIN_INSTRUCTOR;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CourseTeacherStatus status = CourseTeacherStatus.PENDING;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes; // Ghi chú từ Manager

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    /**
     * Business method: Check if this teacher is main instructor
     */
    public boolean isMainInstructor() {
        return role == TeacherRole.MAIN_INSTRUCTOR && isActive;
    }

    /**
     * Business method: Check if assignment can be accepted
     */
    public boolean canAccept() {
        return status == CourseTeacherStatus.PENDING && isActive;
    }

    /**
     * Business method: Check if assignment can be declined
     */
    public boolean canDecline() {
        return status == CourseTeacherStatus.PENDING && isActive;
    }

    /**
     * Business method: Check if assignment can be removed
     */
    public boolean canRemove() {
        return status != CourseTeacherStatus.REMOVED && isActive;
    }

    /**
     * Accept the assignment
     */
    public void acceptAssignment() {
        if (!canAccept()) {
            throw new IllegalStateException("Cannot accept this assignment");
        }
        status = CourseTeacherStatus.ACCEPTED;
        acceptedAt = LocalDateTime.now();
    }

    /**
     * Decline the assignment
     */
    public void declineAssignment() {
        if (!canDecline()) {
            throw new IllegalStateException("Cannot decline this assignment");
        }
        status = CourseTeacherStatus.DECLINED;
    }

    /**
     * Remove the assignment
     */
    public void removeAssignment() {
        if (!canRemove()) {
            throw new IllegalStateException("Cannot remove this assignment");
        }
        status = CourseTeacherStatus.REMOVED;
        removedAt = LocalDateTime.now();
        isActive = false;
    }

    public String getStatusDescription() {
        return switch (status) {
            case PENDING -> "Đang chờ chấp nhận";
            case ACCEPTED -> "Đã chấp nhận";
            case DECLINED -> "Đã từ chối";
            case REMOVED -> "Đã gỡ bỏ";
            default -> status.name();
        };
    }
}