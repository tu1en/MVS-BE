package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity để quản lý quyền của students trong live session
 */
@Entity
@Table(name = "live_session_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSessionPermission {
    
    public enum PermissionType {
        AUDIO_ENABLED,
        VIDEO_ENABLED, 
        SCREEN_SHARE_ENABLED,
        CHAT_ENABLED,
        WHITEBOARD_ENABLED,
        RAISE_HAND_ENABLED,
        DOCUMENT_ACCESS_ENABLED
    }
    
    public enum PermissionStatus {
        GRANTED,
        DENIED,
        PENDING
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "live_stream_id", nullable = false)
    private LiveStream liveStream;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_status", nullable = false)
    @Builder.Default
    private PermissionStatus permissionStatus = PermissionStatus.DENIED;
    
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Check if permission is currently valid
     */
    public boolean isCurrentlyValid() {
        if (!isActive || permissionStatus != PermissionStatus.GRANTED) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check if permission has been granted
        if (grantedAt != null && grantedAt.isAfter(now)) {
            return false;
        }
        
        // Check if permission has expired
        if (expiresAt != null && expiresAt.isBefore(now)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Grant permission with optional expiration
     */
    public void grantPermission(LocalDateTime expiresAt, String reason) {
        this.permissionStatus = PermissionStatus.GRANTED;
        this.grantedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.reason = reason;
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    /**
     * Deny permission
     */
    public void denyPermission(String reason) {
        this.permissionStatus = PermissionStatus.DENIED;
        this.grantedAt = null;
        this.expiresAt = null;
        this.reason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Revoke permission
     */
    public void revokePermission(String reason) {
        this.permissionStatus = PermissionStatus.DENIED;
        this.expiresAt = LocalDateTime.now(); // Immediate expiration
        this.reason = reason;
        this.updatedAt = LocalDateTime.now();
    }
}