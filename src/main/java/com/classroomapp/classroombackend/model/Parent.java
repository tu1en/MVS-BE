package com.classroomapp.classroombackend.model;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a parent in the system
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Entity
@Table(name = "parents")
@Data
@NoArgsConstructor
@ToString(exclude = {"user", "studentParents", "leaveNotices", "messages"})
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Nationalized
    @Column(name = "name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ParentStatus status = ParentStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<StudentParent> studentParents;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ParentLeaveNotice> leaveNotices;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ParentMessage> messages;

    @OneToOne(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private ParentNotificationPrefs notificationPrefs;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ParentBillingAccess> billingAccess;

    // Constructors

    public Parent(Long userId, String name, String phone, String email) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = ParentStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods

    /**
     * Check if parent is active
     */
    public boolean isActive() {
        return ParentStatus.ACTIVE.equals(this.status);
    }

    /**
     * Get primary children (students) of this parent
     */
    public List<StudentParent> getPrimaryRelationships() {
        return studentParents != null ? 
            studentParents.stream()
                .filter(StudentParent::isPrimary)
                .toList() : 
            List.of();
    }

    /**
     * Get count of children this parent is responsible for
     */
    public int getChildrenCount() {
        return studentParents != null ? studentParents.size() : 0;
    }

    // Enums

    public enum ParentStatus {
        ACTIVE,
        INACTIVE,
        BLOCKED
    }
}