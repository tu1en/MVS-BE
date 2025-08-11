package com.classroomapp.classroombackend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService.ConflictType;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedule_conflicts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConflict {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_entity_1_id")
    private ClassEntity classEntity1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_entity_2_id")
    private ClassEntity classEntity2;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_type", nullable = false)
    private ConflictType conflictType;
    
    @Column(name = "details", columnDefinition = "NVARCHAR(MAX)")
    private String details;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolution_notes", columnDefinition = "NVARCHAR(MAX)")
    private String resolutionNotes;
    
    // Convenience fields for display (không lưu trong DB)
    @Transient
    private String className;
    
    @Transient
    private String schedule;
    
    @Transient
    private String teacherName;
    
    @Transient
    private String roomName;
    
    // Constructor tương thích với ScheduleConflictService (9 tham số)
    public ScheduleConflict(Long classId, String className, String schedule, ConflictType conflictType, String details, 
                           LocalDate startDate, LocalDate endDate, String teacherName, String roomName) {
        this.conflictType = conflictType;
        this.details = details;
        this.startDate = startDate;
        this.endDate = endDate;
        this.detectedAt = LocalDateTime.now();
        this.resolved = false;
        // Set transient fields
        this.className = className;
        this.schedule = schedule;
        this.teacherName = teacherName;
        this.roomName = roomName;
    }
    
    // Constructor for convenience (shortened version) - 5 tham số
    public ScheduleConflict(Long classId, String className, String schedule, ConflictType conflictType, String details) {
        this(classId, className, schedule, conflictType, details, null, null, null, null);
    }
    
    // Constructor for JPA entities
    public ScheduleConflict(ClassEntity classEntity1, ClassEntity classEntity2, ConflictType conflictType, String details) {
        this.classEntity1 = classEntity1;
        this.classEntity2 = classEntity2;
        this.conflictType = conflictType;
        this.details = details;
        this.detectedAt = LocalDateTime.now();
        this.resolved = false;
    }
    
    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
        if (resolved == null) {
            resolved = false;
        }
    }
}