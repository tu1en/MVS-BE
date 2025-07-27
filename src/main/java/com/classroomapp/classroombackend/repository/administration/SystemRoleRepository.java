package com.classroomapp.classroombackend.repository.administration;

import com.classroomapp.classroombackend.model.administration.SystemRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SystemRole entity
 */
@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {
    
    /**
     * Find role by role code
     */
    Optional<SystemRole> findByRoleCode(String roleCode);
    
    /**
     * Find role by role name
     */
    Optional<SystemRole> findByRoleName(String roleName);
    
    /**
     * Find all active roles
     */
    @Query("SELECT r FROM SystemRole r WHERE r.isActive = true ORDER BY r.sortOrder ASC, r.roleName ASC")
    List<SystemRole> findAllActive();
    
    /**
     * Find active roles paginated
     */
    @Query("SELECT r FROM SystemRole r WHERE r.isActive = true ORDER BY r.sortOrder ASC, r.roleName ASC")
    Page<SystemRole> findAllActive(Pageable pageable);
    
    /**
     * Find roles by level
     */
    @Query("SELECT r FROM SystemRole r WHERE r.roleLevel = :level AND r.isActive = true ORDER BY r.sortOrder ASC")
    List<SystemRole> findByRoleLevel(@Param("level") SystemRole.RoleLevel level);
    
    /**
     * Find system roles
     */
    @Query("SELECT r FROM SystemRole r WHERE r.isSystemRole = true ORDER BY r.sortOrder ASC")
    List<SystemRole> findSystemRoles();
    
    /**
     * Find custom roles (non-system)
     */
    @Query("SELECT r FROM SystemRole r WHERE r.isSystemRole = false ORDER BY r.sortOrder ASC")
    Page<SystemRole> findCustomRoles(Pageable pageable);
    
    /**
     * Find roles by level range
     */
    @Query("SELECT r FROM SystemRole r WHERE r.roleLevel IN :levels AND r.isActive = true ORDER BY r.roleLevel ASC, r.sortOrder ASC")
    List<SystemRole> findByRoleLevels(@Param("levels") List<SystemRole.RoleLevel> levels);
    
    /**
     * Find roles higher than specified level
     */
    @Query("SELECT r FROM SystemRole r WHERE r.roleLevel < :level AND r.isActive = true ORDER BY r.roleLevel ASC")
    List<SystemRole> findRolesHigherThan(@Param("level") SystemRole.RoleLevel level);
    
    /**
     * Find roles lower than specified level
     */
    @Query("SELECT r FROM SystemRole r WHERE r.roleLevel > :level AND r.isActive = true ORDER BY r.roleLevel ASC")
    List<SystemRole> findRolesLowerThan(@Param("level") SystemRole.RoleLevel level);
    
    /**
     * Search roles by name or description
     */
    @Query("SELECT r FROM SystemRole r WHERE r.isActive = true AND " +
           "(LOWER(r.roleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY r.sortOrder ASC")
    Page<SystemRole> searchRoles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Count roles by level
     */
    @Query("SELECT r.roleLevel, COUNT(r) FROM SystemRole r WHERE r.isActive = true GROUP BY r.roleLevel")
    List<Object[]> countRolesByLevel();
    
    /**
     * Find roles created by user
     */
    @Query("SELECT r FROM SystemRole r WHERE r.createdBy = :userId ORDER BY r.createdAt DESC")
    Page<SystemRole> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Check if role code exists
     */
    @Query("SELECT COUNT(r) > 0 FROM SystemRole r WHERE r.roleCode = :roleCode AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsByRoleCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);
    
    /**
     * Check if role name exists
     */
    @Query("SELECT COUNT(r) > 0 FROM SystemRole r WHERE r.roleName = :roleName AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsByRoleName(@Param("roleName") String roleName, @Param("excludeId") Long excludeId);
    
    /**
     * Find roles with permissions count
     */
    @Query("SELECT r, COUNT(rp) as permissionCount FROM SystemRole r " +
           "LEFT JOIN r.rolePermissions rp ON rp.isGranted = true " +
           "WHERE r.isActive = true " +
           "GROUP BY r " +
           "ORDER BY r.sortOrder ASC")
    List<Object[]> findRolesWithPermissionCount();
    
    /**
     * Find roles that can manage specified role
     */
    @Query("SELECT r FROM SystemRole r WHERE r.roleLevel < :targetLevel AND r.isActive = true ORDER BY r.roleLevel ASC")
    List<SystemRole> findRolesThatCanManage(@Param("targetLevel") SystemRole.RoleLevel targetLevel);
    
    /**
     * Find next sort order
     */
    @Query("SELECT COALESCE(MAX(r.sortOrder), 0) + 1 FROM SystemRole r")
    Integer getNextSortOrder();
    
    /**
     * Find roles updated since date
     */
    @Query("SELECT r FROM SystemRole r WHERE r.updatedAt >= :sinceDate ORDER BY r.updatedAt DESC")
    List<SystemRole> findUpdatedSince(@Param("sinceDate") java.time.LocalDateTime sinceDate);
    
    /**
     * Get role hierarchy
     */
    @Query("SELECT r FROM SystemRole r WHERE r.isActive = true ORDER BY r.roleLevel ASC, r.sortOrder ASC")
    List<SystemRole> getRoleHierarchy();
    
    /**
     * Find orphaned roles (creator no longer exists)
     */
    @Query("SELECT r FROM SystemRole r WHERE r.createdBy NOT IN (SELECT u.id FROM User u) AND r.isSystemRole = false")
    List<SystemRole> findOrphanedRoles();
    
    /**
     * Get role statistics
     */
    @Query("SELECT " +
           "COUNT(r) as totalRoles, " +
           "COUNT(CASE WHEN r.isActive = true THEN 1 END) as activeRoles, " +
           "COUNT(CASE WHEN r.isSystemRole = true THEN 1 END) as systemRoles, " +
           "COUNT(CASE WHEN r.isSystemRole = false THEN 1 END) as customRoles " +
           "FROM SystemRole r")
    Object[] getRoleStatistics();
}
