package com.classroomapp.classroombackend.model.hrmanagement;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Entity for Staff Shift Assignment - Maps staff to shifts
 * Based on guild.md specifications for attendance management
 */
@Entity
@Table(name = "staff_shift_assignments", 
       indexes = {
           @Index(name = "idx_staff_assignments_staff", columnList = "staff_id, effective_from"),
           @Index(name = "idx_staff_assignments_shift", columnList = "shift_id"),
           @Index(name = "idx_staff_assignments_active", columnList = "staff_id,effective_from,effective_until")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_staff_assignments_staff"))
    private User staff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_staff_assignments_shift"))
    private Shift shift;
    
    @Column(name = "effective_from", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_until")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveUntil;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", 
                foreignKey = @ForeignKey(name = "fk_staff_assignments_assigned_by"))
    private User assignedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public boolean isCurrentAssignment() {
        LocalDate now = LocalDate.now();
        return !effectiveFrom.isAfter(now) && 
               (effectiveUntil == null || !effectiveUntil.isBefore(now));
    }
    
    @PrePersist
    @PreUpdate
    private void validateAssignment() {
        if (effectiveFrom != null && effectiveUntil != null && 
            effectiveUntil.isBefore(effectiveFrom)) {
            throw new IllegalStateException("Effective until date must be after effective from date");
        }
    }
}