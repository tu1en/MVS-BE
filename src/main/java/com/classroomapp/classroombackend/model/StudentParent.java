package com.classroomapp.classroombackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing the relationship between a student and a parent
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Entity
@Table(name = "student_parent")
@Data
@NoArgsConstructor
@ToString(exclude = {"student", "parent"})
public class StudentParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    private RelationType relationType;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "legal_guardian")
    private Boolean legalGuardian = false;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonBackReference
    private Parent parent;

    // Constructors

    public StudentParent(Long studentId, Long parentId, RelationType relationType, 
                        Boolean isPrimary, Boolean legalGuardian) {
        this.studentId = studentId;
        this.parentId = parentId;
        this.relationType = relationType;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.legalGuardian = legalGuardian != null ? legalGuardian : false;
        this.startAt = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        if (startAt == null) {
            startAt = LocalDate.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods

    /**
     * Check if this relationship is currently active
     */
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return (startAt == null || !startAt.isAfter(now)) && 
               (endAt == null || !endAt.isBefore(now));
    }

    /**
     * Check if this parent is primary for the student
     */
    public boolean isPrimary() {
        return Boolean.TRUE.equals(this.isPrimary);
    }

    /**
     * Check if this parent is legal guardian for the student
     */
    public boolean isLegalGuardian() {
        return Boolean.TRUE.equals(this.legalGuardian);
    }

    /**
     * Terminate this relationship
     */
    public void terminate() {
        this.endAt = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get relation type display name in Vietnamese
     */
    public String getRelationDisplayName() {
        return switch (this.relationType) {
            case MOTHER -> "Mẹ";
            case FATHER -> "Bố";
            case GUARDIAN -> "Người giám hộ";
            case OTHER -> "Khác";
        };
    }

    // Enums

    public enum RelationType {
        MOTHER,   // Mẹ
        FATHER,   // Bố
        GUARDIAN, // Người giám hộ
        OTHER     // Khác
    }
}