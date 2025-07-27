package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.classroomapp.classroombackend.model.usermanagement.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "webrtc_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;
    
    @Column(name = "room_id", nullable = false)
    private String roomId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;
    
    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (connectedAt == null) {
            connectedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void disconnect() {
        this.isActive = false;
        this.disconnectedAt = LocalDateTime.now();
    }
}