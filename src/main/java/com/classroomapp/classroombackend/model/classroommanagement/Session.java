package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Session entity representing a learning session within a classroom
 * Each session belongs to a classroom and contains multiple slots
 */
@Entity
@Table(name = "sessions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"classroom_id", "session_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Session date is required")
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "NVARCHAR(20) DEFAULT 'UPCOMING'")
    private SessionStatus status = SessionStatus.UPCOMING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sessions", "enrollments"})
    private Classroom classroom;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "session"})
    private List<Slot> slots = new ArrayList<>();

    /**
     * Session status enumeration
     */
    public enum SessionStatus {
        UPCOMING("Sắp diễn ra"),
        IN_PROGRESS("Đang diễn ra"),
        COMPLETED("Đã hoàn thành");

        private final String description;

        SessionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Lifecycle callbacks
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business logic methods
     */
    
    /**
     * Check if session can be modified
     * Sessions can only be modified if status is UPCOMING
     */
    public boolean canBeModified() {
        return status == SessionStatus.UPCOMING;
    }

    /**
     * Check if session can be deleted
     * Sessions can only be deleted if status is UPCOMING and no slots exist
     */
    public boolean canBeDeleted() {
        return status == SessionStatus.UPCOMING && (slots == null || slots.isEmpty());
    }

    /**
     * Check if slots can be added to this session
     */
    public boolean canAddSlots() {
        return status == SessionStatus.UPCOMING || status == SessionStatus.IN_PROGRESS;
    }

    /**
     * Get total number of slots in this session
     */
    public int getTotalSlots() {
        return slots != null ? slots.size() : 0;
    }

    /**
     * Get number of completed slots
     */
    public long getCompletedSlotsCount() {
        if (slots == null) return 0;
        return slots.stream()
                .filter(slot -> slot.getStatus() == Slot.SlotStatus.DONE)
                .count();
    }

    /**
     * Calculate session progress percentage
     */
    public double getProgressPercentage() {
        if (slots == null || slots.isEmpty()) return 0.0;
        return (double) getCompletedSlotsCount() / slots.size() * 100.0;
    }

    /**
     * Check if session is in the past
     */
    public boolean isPastSession() {
        return sessionDate.isBefore(LocalDate.now());
    }

    /**
     * Check if session is today
     */
    public boolean isToday() {
        return sessionDate.equals(LocalDate.now());
    }

    /**
     * Check if session is in the future
     */
    public boolean isFutureSession() {
        return sessionDate.isAfter(LocalDate.now());
    }

    /**
     * Validate status transition
     */
    public boolean isValidStatusTransition(SessionStatus newStatus) {
        if (status == newStatus) return true;
        
        switch (status) {
            case UPCOMING:
                return newStatus == SessionStatus.IN_PROGRESS || newStatus == SessionStatus.COMPLETED;
            case IN_PROGRESS:
                return newStatus == SessionStatus.COMPLETED;
            case COMPLETED:
                return false; // Cannot transition from COMPLETED
            default:
                return false;
        }
    }

    /**
     * Update status with validation
     */
    public void updateStatus(SessionStatus newStatus) {
        if (!isValidStatusTransition(newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // Convenience methods for status checking
    public boolean isUpcoming() { return status == SessionStatus.UPCOMING; }
    public boolean isInProgress() { return status == SessionStatus.IN_PROGRESS; }
    public boolean isCompleted() { return status == SessionStatus.COMPLETED; }

    /**
     * Get classroom ID (convenience method)
     */
    public Long getClassroomId() {
        return classroom != null ? classroom.getId() : null;
    }

    /**
     * Set classroom ID (convenience method)
     */
    public void setClassroomId(Long classroomId) {
        if (this.classroom == null) {
            this.classroom = new Classroom();
        }
        this.classroom.setId(classroomId);
    }

    @Override
    public String toString() {
        return String.format("Session{id=%d, date=%s, status=%s, classroom=%s}", 
            id, sessionDate, status, classroom != null ? classroom.getName() : "null");
    }
}
