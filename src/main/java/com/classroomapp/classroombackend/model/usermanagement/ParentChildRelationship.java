package com.classroomapp.classroombackend.model.usermanagement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parent_child_relationships")
public class ParentChildRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType; // e.g., MOTHER, FATHER, GUARDIAN

    private String relationshipNotes;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String status = "ACTIVE";

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getParent() { return parent; }
    public void setParent(User parent) { this.parent = parent; }

    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }

    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }

    public String getRelationshipNotes() { return relationshipNotes; }
    public void setRelationshipNotes(String relationshipNotes) { this.relationshipNotes = relationshipNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public enum RelationshipType {
        MOTHER, FATHER, GUARDIAN, OTHER
    }
}