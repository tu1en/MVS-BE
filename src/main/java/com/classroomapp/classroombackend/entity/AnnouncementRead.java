package com.classroomapp.classroombackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.usermanagement.User;

import java.time.LocalDateTime;

/**
 * Entity representing when a user has read an announcement
 */
@Entity
@Table(name = "announcement_read", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"announcement_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = true;
    
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = true;
        }
    }
}
