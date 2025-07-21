package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.model.classroommanagement.Session;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Session entity
 * Used for API requests and responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

    private Long id;

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    private String classroomName;

    @NotNull(message = "Session date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    private String description;

    @NotNull(message = "Status is required")
    private Session.SessionStatus status;

    private String statusDescription;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Slot information
    private List<SlotDto> slots;
    private Integer totalSlots;
    private Integer completedSlots;
    private Double progressPercentage;

    // Classroom information
    private String teacherName;
    private Long teacherId;

    // Status flags for UI
    private Boolean canBeModified;
    private Boolean canBeDeleted;
    private Boolean canAddSlots;
    private Boolean isPastSession;
    private Boolean isToday;
    private Boolean isFutureSession;

    // Additional metadata
    private String formattedDate; // For display purposes
    private String relativeDate; // "Today", "Tomorrow", "Yesterday", etc.

    /**
     * Constructor for basic session info without slots
     */
    public SessionDto(Long id, Long classroomId, String classroomName, LocalDate sessionDate, 
                     String description, Session.SessionStatus status, LocalDateTime createdAt, 
                     LocalDateTime updatedAt) {
        this.id = id;
        this.classroomId = classroomId;
        this.classroomName = classroomName;
        this.sessionDate = sessionDate;
        this.description = description;
        this.status = status;
        this.statusDescription = status != null ? status.getDescription() : null;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Set status flags
        this.canBeModified = status == Session.SessionStatus.UPCOMING;
        this.canBeDeleted = status == Session.SessionStatus.UPCOMING;
        this.canAddSlots = status == Session.SessionStatus.UPCOMING || status == Session.SessionStatus.IN_PROGRESS;
        
        // Set date flags
        LocalDate today = LocalDate.now();
        this.isPastSession = sessionDate != null && sessionDate.isBefore(today);
        this.isToday = sessionDate != null && sessionDate.equals(today);
        this.isFutureSession = sessionDate != null && sessionDate.isAfter(today);
        
        // Set formatted dates
        if (sessionDate != null) {
            this.formattedDate = sessionDate.toString();
            this.relativeDate = getRelativeDateString(sessionDate, today);
        }
    }

    /**
     * Get relative date string for display
     */
    private String getRelativeDateString(LocalDate sessionDate, LocalDate today) {
        if (sessionDate.equals(today)) {
            return "Hôm nay";
        } else if (sessionDate.equals(today.plusDays(1))) {
            return "Ngày mai";
        } else if (sessionDate.equals(today.minusDays(1))) {
            return "Hôm qua";
        } else if (sessionDate.isAfter(today) && sessionDate.isBefore(today.plusDays(7))) {
            return "Tuần này";
        } else if (sessionDate.isBefore(today) && sessionDate.isAfter(today.minusDays(7))) {
            return "Tuần trước";
        } else {
            return sessionDate.toString();
        }
    }

    /**
     * Calculate and set progress percentage based on slots
     */
    public void calculateProgress() {
        if (slots == null || slots.isEmpty()) {
            this.progressPercentage = 0.0;
            this.totalSlots = 0;
            this.completedSlots = 0;
            return;
        }

        this.totalSlots = slots.size();
        this.completedSlots = (int) slots.stream()
            .filter(slot -> slot.getStatus() == com.classroomapp.classroombackend.model.classroommanagement.Slot.SlotStatus.DONE)
            .count();
        
        this.progressPercentage = totalSlots > 0 ? (double) completedSlots / totalSlots * 100.0 : 0.0;
    }

    /**
     * Check if session is editable
     */
    public boolean isEditable() {
        return canBeModified != null && canBeModified;
    }

    /**
     * Check if session is deletable
     */
    public boolean isDeletable() {
        return canBeDeleted != null && canBeDeleted;
    }

    /**
     * Get status color for UI
     */
    public String getStatusColor() {
        if (status == null) return "default";
        
        switch (status) {
            case UPCOMING:
                return "blue";
            case IN_PROGRESS:
                return "orange";
            case COMPLETED:
                return "green";
            default:
                return "default";
        }
    }

    /**
     * Get status icon for UI
     */
    public String getStatusIcon() {
        if (status == null) return "question";
        
        switch (status) {
            case UPCOMING:
                return "clock-circle";
            case IN_PROGRESS:
                return "play-circle";
            case COMPLETED:
                return "check-circle";
            default:
                return "question-circle";
        }
    }

    /**
     * Get progress color based on percentage
     */
    public String getProgressColor() {
        if (progressPercentage == null) return "default";
        
        if (progressPercentage >= 100) {
            return "success";
        } else if (progressPercentage >= 75) {
            return "normal";
        } else if (progressPercentage >= 50) {
            return "active";
        } else if (progressPercentage > 0) {
            return "exception";
        } else {
            return "default";
        }
    }

    /**
     * Check if session has slots
     */
    public boolean hasSlots() {
        return slots != null && !slots.isEmpty();
    }

    /**
     * Get slot count
     */
    public int getSlotCount() {
        return slots != null ? slots.size() : 0;
    }

    /**
     * Check if all slots are completed
     */
    public boolean isFullyCompleted() {
        return hasSlots() && completedSlots != null && completedSlots.equals(totalSlots);
    }

    /**
     * Check if session has any active slots
     */
    public boolean hasActiveSlots() {
        if (slots == null) return false;
        return slots.stream()
            .anyMatch(slot -> slot.getStatus() == com.classroomapp.classroombackend.model.classroommanagement.Slot.SlotStatus.ACTIVE);
    }

    /**
     * Get summary text for display
     */
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        
        if (hasSlots()) {
            summary.append(String.format("%d slot%s", totalSlots, totalSlots > 1 ? "s" : ""));
            if (completedSlots > 0) {
                summary.append(String.format(" (%d hoàn thành)", completedSlots));
            }
        } else {
            summary.append("Chưa có slot");
        }
        
        return summary.toString();
    }
}
