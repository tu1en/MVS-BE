package com.classroomapp.classroombackend.repository.administration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.administration.SystemPermission;

/**
 * Repository for SystemPermission entity
 */
@Repository
public interface SystemPermissionRepository extends JpaRepository<SystemPermission, Long> {
    
    /**
     * Find permission by permission code
     */
    Optional<SystemPermission> findByPermissionCode(String permissionCode);
    
    /**
     * Find permission by permission name
     */
    Optional<SystemPermission> findByPermissionName(String permissionName);
    
    /**
     * Find all active permissions
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.isActive = true ORDER BY p.sortOrder ASC, p.permissionName ASC")
    List<SystemPermission> findByIsActiveTrue();
    
    /**
     * Find active permissions paginated
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.isActive = true ORDER BY p.sortOrder ASC, p.permissionName ASC")
    Page<SystemPermission> findAllActive(Pageable pageable);
    
    /**
     * Find permissions by module
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.moduleCode = :moduleCode AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<SystemPermission> findByModuleCode(@Param("moduleCode") String moduleCode);
    
    /**
     * Find permissions by type
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.permissionType = :type AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<SystemPermission> findByPermissionType(@Param("type") SystemPermission.PermissionType type);
    
    /**
     * Find system permissions
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.isSystemPermission = true ORDER BY p.sortOrder ASC")
    List<SystemPermission> findSystemPermissions();
    
    /**
     * Find custom permissions (non-system)
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.isSystemPermission = false ORDER BY p.sortOrder ASC")
    Page<SystemPermission> findCustomPermissions(Pageable pageable);
    
    /**
     * Search permissions by name or description
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.isActive = true AND " +
           "(LOWER(p.permissionName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY p.sortOrder ASC")
    Page<SystemPermission> searchPermissions(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find permissions by resource type
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.resourceType = :resourceType AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<SystemPermission> findByResourceType(@Param("resourceType") SystemPermission.ResourceType resourceType);
    
    /**
     * Find permissions by resource pattern
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.resourcePattern LIKE :pattern AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<SystemPermission> findByResourcePattern(@Param("pattern") String pattern);
    
    /**
     * Check if permission code exists
     */
    @Query("SELECT COUNT(p) > 0 FROM SystemPermission p WHERE p.permissionCode = :permissionCode AND (:excludeId IS NULL OR p.id != :excludeId)")
    boolean existsByPermissionCode(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);
    
    /**
     * Check if permission name exists
     */
    @Query("SELECT COUNT(p) > 0 FROM SystemPermission p WHERE p.permissionName = :permissionName AND (:excludeId IS NULL OR p.id != :excludeId)")
    boolean existsByPermissionName(@Param("permissionName") String permissionName, @Param("excludeId") Long excludeId);
    
    /**
     * Find permissions created by user
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.createdBy = :userId ORDER BY p.createdAt DESC")
    Page<SystemPermission> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get permission statistics
     */
    @Query("SELECT " +
           "COUNT(p) as totalPermissions, " +
           "COUNT(CASE WHEN p.isActive = true THEN 1 END) as activePermissions, " +
           "COUNT(CASE WHEN p.isSystemPermission = true THEN 1 END) as systemPermissions, " +
           "COUNT(CASE WHEN p.isSystemPermission = false THEN 1 END) as customPermissions " +
           "FROM SystemPermission p")
    Object[] getPermissionStatistics();
    
    /**
     * Find next sort order
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) + 1 FROM SystemPermission p")
    Integer getNextSortOrder();
    
    /**
     * Find permissions updated since date
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.updatedAt >= :sinceDate ORDER BY p.updatedAt DESC")
    List<SystemPermission> findUpdatedSince(@Param("sinceDate") java.time.LocalDateTime sinceDate);
    
    /**
     * Find orphaned permissions (creator no longer exists)
     */
    @Query("SELECT p FROM SystemPermission p WHERE p.createdBy NOT IN (SELECT u.id FROM User u) AND p.isSystemPermission = false")
    List<SystemPermission> findOrphanedPermissions();
    
    /**
     * Count permissions by module
     */
    @Query("SELECT p.moduleCode, COUNT(p) FROM SystemPermission p WHERE p.isActive = true GROUP BY p.moduleCode")
    List<Object[]> countPermissionsByModule();
    
    /**
     * Count permissions by type
     */
    @Query("SELECT p.permissionType, COUNT(p) FROM SystemPermission p WHERE p.isActive = true GROUP BY p.permissionType")
    List<Object[]> countPermissionsByType();
    
    /**
     * Find permissions with role count
     */
    @Query("SELECT p, COUNT(rp) as roleCount FROM SystemPermission p " +
           "LEFT JOIN RolePermission rp ON rp.permission = p AND rp.isGranted = true " +
           "WHERE p.isActive = true " +
           "GROUP BY p " +
           "ORDER BY p.sortOrder ASC")
    List<Object[]> findPermissionsWithRoleCount();
}
