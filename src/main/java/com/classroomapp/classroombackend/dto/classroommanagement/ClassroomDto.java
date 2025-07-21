package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Classroom entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDto {

    private Long id;

    private String classroomName;

    private String description;

    private Long teacherId;

    private String teacherName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Additional information
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer upcomingSessions;
    private Integer totalStudents;
    private Double progressPercentage;

    // Status flags for UI
    private Boolean canBeModified;
    private Boolean canBeDeleted;
    private Boolean hasActiveSessions;
    private Boolean hasStudents;

    // Session information
    private List<SessionDto> recentSessions;
    private SessionDto nextSession;
    private SessionDto lastSession;

    /**
     * Constructor for basic classroom info
     */
    public ClassroomDto(Long id, String classroomName, String description, Long teacherId, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.classroomName = classroomName;
        this.description = description;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Set default values
        this.totalSessions = 0;
        this.completedSessions = 0;
        this.upcomingSessions = 0;
        this.totalStudents = 0;
        this.progressPercentage = 0.0;
        
        // Set status flags
        this.canBeModified = true;
        this.canBeDeleted = true;
        this.hasActiveSessions = false;
        this.hasStudents = false;
    }

    /**
     * Calculate progress percentage based on sessions
     */
    public void calculateProgress() {
        if (totalSessions == null || totalSessions == 0) {
            this.progressPercentage = 0.0;
            return;
        }
        
        if (completedSessions == null) {
            this.completedSessions = 0;
        }
        
        this.progressPercentage = (double) completedSessions / totalSessions * 100.0;
    }

    /**
     * Check if classroom is active
     */
    public boolean isActive() {
        return hasActiveSessions != null && hasActiveSessions;
    }

    /**
     * Check if classroom is editable
     */
    public boolean isEditable() {
        return canBeModified != null && canBeModified;
    }

    /**
     * Check if classroom is deletable
     */
    public boolean isDeletable() {
        return canBeDeleted != null && canBeDeleted && !isActive();
    }

    /**
     * Get status color for UI
     */
    public String getStatusColor() {
        if (hasActiveSessions != null && hasActiveSessions) {
            return "green";
        } else if (upcomingSessions != null && upcomingSessions > 0) {
            return "blue";
        } else if (totalSessions != null && totalSessions > 0) {
            return "orange";
        } else {
            return "default";
        }
    }

    /**
     * Get status text for display
     */
    public String getStatusText() {
        if (hasActiveSessions != null && hasActiveSessions) {
            return "Đang hoạt động";
        } else if (upcomingSessions != null && upcomingSessions > 0) {
            return "Có buổi học sắp tới";
        } else if (totalSessions != null && totalSessions > 0) {
            return "Đã hoàn thành";
        } else {
            return "Chưa có buổi học";
        }
    }

    /**
     * Get summary text for display
     */
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        
        if (totalSessions != null && totalSessions > 0) {
            summary.append(String.format("%d buổi học", totalSessions));
            if (completedSessions != null && completedSessions > 0) {
                summary.append(String.format(" (%d hoàn thành)", completedSessions));
            }
        } else {
            summary.append("Chưa có buổi học");
        }
        
        if (totalStudents != null && totalStudents > 0) {
            summary.append(String.format(" • %d học sinh", totalStudents));
        }
        
        return summary.toString();
    }

    /**
     * Check if classroom has sessions
     */
    public boolean hasSessions() {
        return totalSessions != null && totalSessions > 0;
    }

    /**
     * Check if classroom has students
     */
    public boolean hasStudentsEnrolled() {
        return hasStudents != null && hasStudents;
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
}
