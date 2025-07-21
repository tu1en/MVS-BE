package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Slot entity representing a time slot within a session
 * Each slot belongs to a session and can have multiple attachments
 */
@Entity
@Table(name = "slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String slotName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "NVARCHAR(20) DEFAULT 'PLANNED'")
    private SlotStatus status = SlotStatus.PLANNED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "slots"})
    private Session session;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "slot"})
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Slot status enumeration
     */
    public enum SlotStatus {
        PENDING("Đang chờ xử lý"),
        PLANNED("Đã lên kế hoạch"),
        ACTIVE("Đang hoạt động"),
        DONE("Đã hoàn thành");

        private final String description;

        SlotStatus(String description) {
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
        validateTimeRange();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateTimeRange();
    }

    /**
     * Business logic methods
     */
    
    /**
     * Validate that start time is before end time
     */
    private void validateTimeRange() {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    /**
     * Check if slot can be modified
     * Slots can only be modified if status is not DONE
     */
    public boolean canBeModified() {
        return status != SlotStatus.DONE;
    }

    /**
     * Check if slot can be deleted
     * Slots can only be deleted if status is PLANNED and no attachments exist
     */
    public boolean canBeDeleted() {
        return status == SlotStatus.PLANNED && (attachments == null || attachments.isEmpty());
    }

    /**
     * Check if attachments can be uploaded to this slot
     * Attachments can only be uploaded if status is ACTIVE
     */
    public boolean canUploadAttachments() {
        return status == SlotStatus.ACTIVE;
    }

    /**
     * Get slot duration in minutes
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Get slot duration as formatted string (e.g., "1h 30m")
     */
    public String getFormattedDuration() {
        long minutes = getDurationInMinutes();
        if (minutes == 0) return "0m";
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        if (hours == 0) {
            return remainingMinutes + "m";
        } else if (remainingMinutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + remainingMinutes + "m";
        }
    }

    /**
     * Check if this slot overlaps with another slot
     */
    public boolean overlapsWith(Slot other) {
        if (other == null || other.startTime == null || other.endTime == null) return false;
        if (this.startTime == null || this.endTime == null) return false;
        
        return (this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime));
    }

    /**
     * Check if this slot overlaps with a time range
     */
    public boolean overlapsWith(LocalTime otherStart, LocalTime otherEnd) {
        if (otherStart == null || otherEnd == null) return false;
        if (this.startTime == null || this.endTime == null) return false;
        
        return (this.startTime.isBefore(otherEnd) && this.endTime.isAfter(otherStart));
    }

    /**
     * Get total number of attachments
     */
    public int getTotalAttachments() {
        return attachments != null ? attachments.size() : 0;
    }

    /**
     * Get total size of all attachments in bytes
     */
    public long getTotalAttachmentSize() {
        if (attachments == null) return 0;
        return attachments.stream()
                .mapToLong(Attachment::getFileSize)
                .sum();
    }

    /**
     * Get formatted total attachment size
     */
    public String getFormattedTotalAttachmentSize() {
        long bytes = getTotalAttachmentSize();
        if (bytes == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Validate status transition
     */
    public boolean isValidStatusTransition(SlotStatus newStatus) {
        if (status == newStatus) return true;
        
        switch (status) {
            case PLANNED:
                return newStatus == SlotStatus.ACTIVE || newStatus == SlotStatus.DONE;
            case ACTIVE:
                return newStatus == SlotStatus.DONE;
            case DONE:
                return false; // Cannot transition from DONE
            default:
                return false;
        }
    }

    /**
     * Update status with validation
     */
    public void updateStatus(SlotStatus newStatus) {
        if (!isValidStatusTransition(newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if slot is currently active (based on current time and session date)
     */
    public boolean isCurrentlyActive() {
        if (session == null || session.getSessionDate() == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slotStart = session.getSessionDate().atTime(startTime);
        LocalDateTime slotEnd = session.getSessionDate().atTime(endTime);
        
        return now.isAfter(slotStart) && now.isBefore(slotEnd);
    }

    // Convenience methods for status checking
    public boolean isPending() { return status == SlotStatus.PENDING; }
    public boolean isPlanned() { return status == SlotStatus.PLANNED; }
    public boolean isActive() { return status == SlotStatus.ACTIVE; }
    public boolean isDone() { return status == SlotStatus.DONE; }

    /**
     * Get session ID (convenience method)
     */
    public Long getSessionId() {
        return session != null ? session.getId() : null;
    }

    /**
     * Set session ID (convenience method)
     */
    public void setSessionId(Long sessionId) {
        if (this.session == null) {
            this.session = new Session();
        }
        this.session.setId(sessionId);
    }

    /**
     * Get slot name (convenience method)
     */
    public String getSlotName() {
        return slotName;
    }

    /**
     * Set slot name (convenience method)
     */
    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    @Override
    public String toString() {
        return String.format("Slot{id=%d, time=%s-%s, status=%s, session=%d}", 
            id, startTime, endTime, status, session != null ? session.getId() : null);
    }
}
