package com.classroomapp.classroombackend.model.administration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing system permissions
 */
@Entity
@Table(name = "system_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "permission_name", length = 100, nullable = false)
    private String permissionName;
    
    @Column(name = "permission_code", length = 50, nullable = false, unique = true)
    private String permissionCode;
    
    @Column(name = "module_code", length = 30, nullable = false)
    private String moduleCode;
    
    @Column(name = "module_name", length = 100, nullable = false)
    private String moduleName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", length = 20, nullable = false)
    private PermissionType permissionType = PermissionType.FUNCTIONAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 20)
    private ResourceType resourceType;
    
    @Column(name = "resource_pattern", length = 200)
    private String resourcePattern;
    
    @Column(name = "is_system_permission", columnDefinition = "BIT DEFAULT 0")
    private Boolean isSystemPermission = false;
    
    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;
    
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;
    
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
     * Permission type enumeration
     */
    public enum PermissionType {
        FUNCTIONAL("Chức năng"),
        DATA("Dữ liệu"),
        SYSTEM("Hệ thống"),
        API("API"),
        UI("Giao diện");
        
        private final String description;
        
        PermissionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Resource type enumeration
     */
    public enum ResourceType {
        ENDPOINT("API Endpoint"),
        PAGE("Trang web"),
        COMPONENT("Component"),
        DATA("Dữ liệu"),
        FILE("File"),
        SYSTEM("Hệ thống");
        
        private final String description;
        
        ResourceType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business logic methods
    
    /**
     * Check if permission can be modified
     */
    public boolean canBeModified() {
        return !Boolean.TRUE.equals(isSystemPermission);
    }
    
    /**
     * Check if permission can be deleted
     */
    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(isSystemPermission) && Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Check if permission is currently active
     */
    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Get permission display name
     */
    public String getDisplayName() {
        return permissionName + " (" + moduleCode + ")";
    }
    
    /**
     * Get full permission description
     */
    public String getFullDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(permissionName);
        
        if (description != null && !description.trim().isEmpty()) {
            desc.append(" - ").append(description);
        }
        
        desc.append(" [").append(moduleCode).append("]");
        
        return desc.toString();
    }
    
    /**
     * Check if permission matches a resource pattern
     */
    public boolean matchesResource(String resource) {
        if (resourcePattern == null || resourcePattern.trim().isEmpty()) {
            return false;
        }
        
        if (resource == null) {
            return false;
        }
        
        // Simple pattern matching - can be enhanced with regex
        if (resourcePattern.contains("*")) {
            String pattern = resourcePattern.replace("*", ".*");
            return resource.matches(pattern);
        }
        
        return resourcePattern.equals(resource);
    }
    
    /**
     * Validate permission data
     */
    public boolean isValidPermission() {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            return false;
        }
        
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }
        
        if (moduleCode == null || moduleCode.trim().isEmpty()) {
            return false;
        }
        
        if (moduleName == null || moduleName.trim().isEmpty()) {
            return false;
        }
        
        if (permissionType == null) {
            return false;
        }
        
        // Permission code should be uppercase and contain only letters, numbers, and underscores
        if (!permissionCode.matches("^[A-Z0-9_]+$")) {
            return false;
        }
        
        // Module code should be uppercase and contain only letters and underscores
        if (!moduleCode.matches("^[A-Z_]+$")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Activate permission
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * Deactivate permission
     */
    public void deactivate() {
        if (canBeModified()) {
            this.isActive = false;
        } else {
            throw new IllegalStateException("Không thể vô hiệu hóa permission hệ thống");
        }
    }
    
    /**
     * Clone permission for customization
     */
    public SystemPermission cloneForCustomization(String newName, String newCode) {
        SystemPermission clone = new SystemPermission();
        clone.setPermissionName(newName);
        clone.setPermissionCode(newCode);
        clone.setModuleCode(this.moduleCode);
        clone.setModuleName(this.moduleName);
        clone.setDescription("Sao chép từ: " + this.permissionName);
        clone.setPermissionType(this.permissionType);
        clone.setResourceType(this.resourceType);
        clone.setResourcePattern(this.resourcePattern);
        clone.setIsSystemPermission(false);
        clone.setIsActive(true);
        clone.setSortOrder(this.sortOrder);
        
        return clone;
    }
    
    /**
     * Get permission summary for display
     */
    public String getPermissionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Permission: ").append(permissionName);
        summary.append(", Module: ").append(moduleCode);
        summary.append(", Type: ").append(permissionType.getDescription());
        summary.append(", Status: ").append(isCurrentlyActive() ? "Active" : "Inactive");
        
        if (Boolean.TRUE.equals(isSystemPermission)) {
            summary.append(" (System Permission)");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if permission is for a specific module
     */
    public boolean isForModule(String module) {
        return moduleCode != null && moduleCode.equalsIgnoreCase(module);
    }
    
    /**
     * Check if permission is system-level
     */
    public boolean isSystemLevel() {
        return Boolean.TRUE.equals(isSystemPermission) || 
               permissionType == PermissionType.SYSTEM;
    }
    
    /**
     * Get permission hierarchy level based on type
     */
    public int getHierarchyLevel() {
        switch (permissionType) {
            case SYSTEM:
                return 1;
            case DATA:
                return 2;
            case FUNCTIONAL:
                return 3;
            case API:
                return 4;
            case UI:
                return 5;
            default:
                return 6;
        }
    }
    
    /**
     * Check if permission is more critical than another
     */
    public boolean isMoreCriticalThan(SystemPermission other) {
        if (other == null) {
            return true;
        }
        
        return this.getHierarchyLevel() < other.getHierarchyLevel();
    }
}
