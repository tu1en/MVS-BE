package com.classroomapp.classroombackend.model.administration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing system roles with permissions
 */
@Entity
@Table(name = "system_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "role_name", length = 50, nullable = false, unique = true)
    private String roleName;
    
    @Column(name = "role_code", length = 20, nullable = false, unique = true)
    private String roleCode;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role_level", length = 20, nullable = false)
    private RoleLevel roleLevel = RoleLevel.USER;
    
    @Column(name = "is_system_role", columnDefinition = "BIT DEFAULT 0")
    private Boolean isSystemRole = false;
    
    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;
    
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;
    
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * Role level enumeration
     */
    public enum RoleLevel {
        SUPER_ADMIN("Super Admin"),
        ADMIN("Admin"),
        MANAGER("Manager"),
        SUPERVISOR("Supervisor"),
        USER("User"),
        GUEST("Guest");
        
        private final String description;
        
        RoleLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return ordinal();
        }
        
        public boolean isHigherThan(RoleLevel other) {
            return this.getLevel() < other.getLevel();
        }
        
        public boolean isLowerThan(RoleLevel other) {
            return this.getLevel() > other.getLevel();
        }
    }
    
    // Business logic methods
    
    /**
     * Check if role can be modified
     */
    public boolean canBeModified() {
        return !Boolean.TRUE.equals(isSystemRole);
    }
    
    /**
     * Check if role can be deleted
     */
    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(isSystemRole) && Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Check if role has specific permission
     */
    public boolean hasPermission(String permissionCode) {
        if (rolePermissions == null) {
            return false;
        }
        
        return rolePermissions.stream()
            .anyMatch(rp -> rp.getPermission().getPermissionCode().equals(permissionCode) 
                         && Boolean.TRUE.equals(rp.getIsGranted()));
    }
    
    /**
     * Check if role has any permission from a module
     */
    public boolean hasModulePermission(String moduleCode) {
        if (rolePermissions == null) {
            return false;
        }
        
        return rolePermissions.stream()
            .anyMatch(rp -> rp.getPermission().getModuleCode().equals(moduleCode) 
                         && Boolean.TRUE.equals(rp.getIsGranted()));
    }
    
    /**
     * Get granted permissions count
     */
    public long getGrantedPermissionsCount() {
        if (rolePermissions == null) {
            return 0;
        }
        
        return rolePermissions.stream()
            .filter(rp -> Boolean.TRUE.equals(rp.getIsGranted()))
            .count();
    }
    
    /**
     * Check if role is higher level than another role
     */
    public boolean isHigherThan(SystemRole other) {
        if (other == null) {
            return true;
        }
        
        return this.roleLevel.isHigherThan(other.roleLevel);
    }
    
    /**
     * Check if role can manage another role
     */
    public boolean canManage(SystemRole other) {
        if (other == null) {
            return true;
        }
        
        // System roles can only be managed by SUPER_ADMIN
        if (Boolean.TRUE.equals(other.isSystemRole) && this.roleLevel != RoleLevel.SUPER_ADMIN) {
            return false;
        }
        
        // Can manage roles of lower level
        return this.isHigherThan(other);
    }
    
    /**
     * Get role display name
     */
    public String getDisplayName() {
        return roleName + " (" + roleLevel.getDescription() + ")";
    }
    
    /**
     * Check if role is currently active
     */
    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Activate role
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * Deactivate role
     */
    public void deactivate() {
        if (canBeModified()) {
            this.isActive = false;
        } else {
            throw new IllegalStateException("Không thể vô hiệu hóa role hệ thống");
        }
    }
    
    /**
     * Clone role for customization
     */
    public SystemRole cloneForCustomization(String newName, String newCode) {
        SystemRole clone = new SystemRole();
        clone.setRoleName(newName);
        clone.setRoleCode(newCode);
        clone.setDescription("Sao chép từ: " + this.roleName);
        clone.setRoleLevel(this.roleLevel);
        clone.setIsSystemRole(false);
        clone.setIsActive(true);
        clone.setSortOrder(this.sortOrder);
        
        return clone;
    }
    
    /**
     * Validate role data
     */
    public boolean isValidRole() {
        if (roleName == null || roleName.trim().isEmpty()) {
            return false;
        }
        
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return false;
        }
        
        if (roleLevel == null) {
            return false;
        }
        
        // Role code should be uppercase and contain only letters and underscores
        if (!roleCode.matches("^[A-Z_]+$")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get role summary for display
     */
    public String getRoleSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Role: ").append(roleName);
        summary.append(", Level: ").append(roleLevel.getDescription());
        summary.append(", Permissions: ").append(getGrantedPermissionsCount());
        summary.append(", Status: ").append(isCurrentlyActive() ? "Active" : "Inactive");
        
        if (Boolean.TRUE.equals(isSystemRole)) {
            summary.append(" (System Role)");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if role has administrative privileges
     */
    public boolean hasAdminPrivileges() {
        return roleLevel == RoleLevel.SUPER_ADMIN || roleLevel == RoleLevel.ADMIN;
    }
    
    /**
     * Check if role has management privileges
     */
    public boolean hasManagementPrivileges() {
        return roleLevel == RoleLevel.SUPER_ADMIN || 
               roleLevel == RoleLevel.ADMIN || 
               roleLevel == RoleLevel.MANAGER;
    }
    
    /**
     * Get role hierarchy level
     */
    public int getHierarchyLevel() {
        return roleLevel.getLevel();
    }
}
