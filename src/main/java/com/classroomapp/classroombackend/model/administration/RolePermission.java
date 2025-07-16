package com.classroomapp.classroombackend.model.administration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing role-permission relationships
 */
@Entity
@Table(name = "role_permissions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private SystemRole role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private SystemPermission permission;
    
    @Column(name = "is_granted", columnDefinition = "BIT DEFAULT 1")
    private Boolean isGranted = true;
    
    @Column(name = "is_inherited", columnDefinition = "BIT DEFAULT 0")
    private Boolean isInherited = false;
    
    @Column(name = "inherited_from_role_id")
    private Long inheritedFromRoleId;
    
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "conditions", length = 1000)
    private String conditions; // JSON string for conditional permissions
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "granted_by")
    private Long grantedBy;
    
    @Column(name = "revoked_by")
    private Long revokedBy;
    
    // Business logic methods
    
    /**
     * Check if permission is currently active
     */
    public boolean isCurrentlyActive() {
        if (!Boolean.TRUE.equals(isGranted)) {
            return false;
        }
        
        if (revokedAt != null) {
            return false;
        }
        
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if permission is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if permission is revoked
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }
    
    /**
     * Grant permission
     */
    public void grant(Long grantedBy) {
        this.isGranted = true;
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = grantedBy;
        this.revokedAt = null;
        this.revokedBy = null;
    }
    
    /**
     * Revoke permission
     */
    public void revoke(Long revokedBy) {
        this.isGranted = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedBy = revokedBy;
    }
    
    /**
     * Set expiration date
     */
    public void setExpiration(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    /**
     * Remove expiration
     */
    public void removeExpiration() {
        this.expiresAt = null;
    }
    
    /**
     * Check if permission has conditions
     */
    public boolean hasConditions() {
        return conditions != null && !conditions.trim().isEmpty();
    }
    
    /**
     * Get permission status description
     */
    public String getStatusDescription() {
        if (isRevoked()) {
            return "Đã thu hồi";
        }
        
        if (isExpired()) {
            return "Đã hết hạn";
        }
        
        if (!Boolean.TRUE.equals(isGranted)) {
            return "Chưa cấp";
        }
        
        if (isCurrentlyActive()) {
            return "Đang hoạt động";
        }
        
        return "Không xác định";
    }
    
    /**
     * Get permission type description
     */
    public String getTypeDescription() {
        if (Boolean.TRUE.equals(isInherited)) {
            return "Kế thừa";
        }
        
        return "Trực tiếp";
    }
    
    /**
     * Get time until expiration
     */
    public String getTimeUntilExpiration() {
        if (expiresAt == null) {
            return "Không hết hạn";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return "Đã hết hạn";
        }
        
        long hours = java.time.Duration.between(now, expiresAt).toHours();
        if (hours < 24) {
            return hours + " giờ";
        } else {
            long days = hours / 24;
            return days + " ngày";
        }
    }
    
    /**
     * Validate role permission
     */
    public boolean isValid() {
        if (role == null || permission == null) {
            return false;
        }
        
        // Check if role and permission are active
        if (!role.isCurrentlyActive() || !permission.isCurrentlyActive()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Create inherited permission
     */
    public static RolePermission createInherited(SystemRole role, SystemPermission permission, 
                                                Long inheritedFromRoleId, Long grantedBy) {
        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        rp.setIsGranted(true);
        rp.setIsInherited(true);
        rp.setInheritedFromRoleId(inheritedFromRoleId);
        rp.setGrantedAt(LocalDateTime.now());
        rp.setGrantedBy(grantedBy);
        
        return rp;
    }
    
    /**
     * Create direct permission
     */
    public static RolePermission createDirect(SystemRole role, SystemPermission permission, Long grantedBy) {
        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        rp.setIsGranted(true);
        rp.setIsInherited(false);
        rp.setGrantedAt(LocalDateTime.now());
        rp.setGrantedBy(grantedBy);
        
        return rp;
    }
    
    /**
     * Create temporary permission with expiration
     */
    public static RolePermission createTemporary(SystemRole role, SystemPermission permission, 
                                               LocalDateTime expiresAt, Long grantedBy) {
        RolePermission rp = createDirect(role, permission, grantedBy);
        rp.setExpiresAt(expiresAt);
        
        return rp;
    }
    
    /**
     * Create conditional permission
     */
    public static RolePermission createConditional(SystemRole role, SystemPermission permission, 
                                                 String conditions, Long grantedBy) {
        RolePermission rp = createDirect(role, permission, grantedBy);
        rp.setConditions(conditions);
        
        return rp;
    }
    
    /**
     * Get permission summary
     */
    public String getPermissionSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (role != null && permission != null) {
            summary.append(role.getRoleName())
                   .append(" -> ")
                   .append(permission.getPermissionName());
        }
        
        summary.append(" (").append(getStatusDescription()).append(")");
        
        if (Boolean.TRUE.equals(isInherited)) {
            summary.append(" [Kế thừa]");
        }
        
        if (expiresAt != null) {
            summary.append(" [Hết hạn: ").append(getTimeUntilExpiration()).append("]");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if permission can be modified
     */
    public boolean canBeModified() {
        // Inherited permissions cannot be modified directly
        if (Boolean.TRUE.equals(isInherited)) {
            return false;
        }
        
        // System permissions require special handling
        if (permission != null && Boolean.TRUE.equals(permission.getIsSystemPermission())) {
            return role != null && role.getRoleLevel() == SystemRole.RoleLevel.SUPER_ADMIN;
        }
        
        return true;
    }
    
    /**
     * Check if permission can be revoked
     */
    public boolean canBeRevoked() {
        if (!canBeModified()) {
            return false;
        }
        
        return Boolean.TRUE.equals(isGranted) && !isRevoked();
    }
    
    /**
     * Check if permission can be granted
     */
    public boolean canBeGranted() {
        if (!canBeModified()) {
            return false;
        }
        
        return !Boolean.TRUE.equals(isGranted) || isRevoked();
    }
}
