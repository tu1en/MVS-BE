package com.classroomapp.classroombackend.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "Schedule")
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roleEntity"})
    private User teacher;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classroom_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Classroom classroom;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;
    
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;
    
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "room")
    private String room;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "color", length = 7, columnDefinition = "VARCHAR(7) DEFAULT '#1890ff'")
    private String color = "#1890ff";
    
    @Column(name = "materials_url")
    private String materialsUrl;
    
    @Column(name = "meet_url")
    private String meetUrl;
    
    @Column(name = "is_recurring", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isRecurring = false;
    
    @Column(name = "is_cancelled", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isCancelled = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        
        if (color == null || color.trim().isEmpty()) {
            color = "#1890ff";
        }
        
        if (isRecurring == null) {
            isRecurring = false;
        }
        
        if (isCancelled == null) {
            isCancelled = false;
        }
        
        if (title == null || title.trim().isEmpty()) {
            if (classroom != null) {
                title = classroom.getName();
            } else {
                title = "Lịch học";
            }
        }
        
        if (location == null || location.trim().isEmpty()) {
            location = "Phòng học";
        }
        
        if (startDatetime != null) {
            dayOfWeek = startDatetime.getDayOfWeek().getValue();
            startTime = startDatetime.toLocalTime();
        }
        
        if (endDatetime != null) {
            endTime = endDatetime.toLocalTime();
        }
        
        if (subject == null || subject.trim().isEmpty()) {
            subject = title;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        if (startDatetime != null) {
            dayOfWeek = startDatetime.getDayOfWeek().getValue();
            startTime = startDatetime.toLocalTime();
        }
        
        if (endDatetime != null) {
            endTime = endDatetime.toLocalTime();
        }
    }
    
    // Utility methods remain the same...
    public String getPeriod() {
        if (startDatetime == null) return "Unknown";
        
        int hour = startDatetime.getHour();
        if (hour < 12) {
            return "Morning";
        } else if (hour < 17) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }
    
    public long getDurationInMinutes() {
        if (startDatetime == null || endDatetime == null) return 0;
        
        return java.time.Duration.between(startDatetime, endDatetime).toMinutes();
    }
    
    public boolean isToday() {
        if (startDatetime == null) return false;
        
        return startDatetime.toLocalDate().equals(java.time.LocalDate.now());
    }
    
    public boolean isUpcoming() {
        if (startDatetime == null) return false;
        
        return startDatetime.isAfter(LocalDateTime.now());
    }
    
    public boolean isCurrentlyActive() {
        if (startDatetime == null || endDatetime == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDatetime) && !now.isAfter(endDatetime);
    }
    
    public Long getClassroomId() {
        return classroom != null ? classroom.getId() : null;
    }
    
    public String getClassroomName() {
        return classroom != null ? classroom.getName() : null;
    }
    
    public Long getTeacherId() {
        return teacher != null ? teacher.getId() : null;
    }
    
    public String getTeacherName() {
        return teacher != null ? teacher.getFullName() : null;
    }
    
    public String getTimeRange() {
        if (startDatetime == null || endDatetime == null) return "";
        
        return String.format("%s - %s", 
            startDatetime.toLocalTime().toString().substring(0, 5),
            endDatetime.toLocalTime().toString().substring(0, 5));
    }
    
    public String getFormattedDate() {
        if (startDatetime == null) return "";
        
        return startDatetime.toLocalDate().toString();
    }
    
    public DayOfWeek getDayOfWeekEnum() {
        if (dayOfWeek == null) return null;
        return DayOfWeek.of(dayOfWeek);
    }
    
    public String getDayOfWeekName() {
        if (dayOfWeek == null) return "";
        
        String[] days = {"", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "";
    }
}