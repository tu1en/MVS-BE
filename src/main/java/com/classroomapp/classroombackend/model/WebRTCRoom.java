package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "webrtc_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId;
    
    @Column(name = "room_name")
    private String roomName;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "max_participants")
    private Integer maxParticipants = 50;
    
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @OneToMany(mappedBy = "roomId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WebRTCSession> sessions = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void incrementParticipants() {
        this.currentParticipants++;
    }
    
    public void decrementParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }
    
    public void closeRoom() {
        this.isActive = false;
        this.closedAt = LocalDateTime.now();
    }
    
    public boolean hasCapacity() {
        return currentParticipants < maxParticipants;
    }
}