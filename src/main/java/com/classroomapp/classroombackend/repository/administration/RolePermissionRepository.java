package com.classroomapp.classroombackend.repository.administration;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.administration.RolePermission;

/**
 * Repository for RolePermission entity
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    /**
     * Find role permissions by role ID
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.isGranted = true ORDER BY rp.grantedAt DESC")
    List<RolePermission> findByRoleIdAndIsGrantedTrue(@Param("roleId") Long roleId);

    /**
     * Find role permissions by permission ID
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId AND rp.isGranted = true ORDER BY rp.grantedAt DESC")
    List<RolePermission> findByPermissionIdAndIsGrantedTrue(@Param("permissionId") Long permissionId);

    /**
     * Find specific role-permission combination
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId AND rp.isGranted = true")
    List<RolePermission> findByRoleIdAndPermissionIdAndIsGrantedTrue(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * Find role permission by role and permission (including revoked)
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId ORDER BY rp.createdAt DESC")
    List<RolePermission> findByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * Check if role has permission
     */
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId AND rp.isGranted = true")
    boolean hasPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * Find all granted permissions for role
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.isGranted = true")
    List<RolePermission> findGrantedPermissionsByRole(@Param("roleId") Long roleId);

    /**
     * Find all roles with specific permission
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId AND rp.isGranted = true")
    List<RolePermission> findRolesWithPermission(@Param("permissionId") Long permissionId);
    
    /**
     * Find role permissions granted by user
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.grantedBy = :userId ORDER BY rp.grantedAt DESC")
    Page<RolePermission> findByGrantedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find role permissions revoked by user
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.revokedBy = :userId ORDER BY rp.revokedAt DESC")
    Page<RolePermission> findByRevokedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find permissions granted in date range
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.grantedAt BETWEEN :startDate AND :endDate AND rp.isGranted = true ORDER BY rp.grantedAt DESC")
    List<RolePermission> findGrantedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find permissions revoked in date range
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.revokedAt BETWEEN :startDate AND :endDate ORDER BY rp.revokedAt DESC")
    List<RolePermission> findRevokedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find expired permissions
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.expiresAt IS NOT NULL AND rp.expiresAt < :now AND rp.isGranted = true")
    List<RolePermission> findExpiredPermissions(@Param("now") LocalDateTime now);
    
    /**
     * Find permissions expiring soon
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.expiresAt IS NOT NULL AND rp.expiresAt BETWEEN :now AND :threshold AND rp.isGranted = true")
    List<RolePermission> findExpiringPermissions(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);
    
    /**
     * Find inherited permissions
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.isInherited = true AND rp.isGranted = true")
    List<RolePermission> findInheritedPermissions();
    
    /**
     * Find permissions inherited from specific role
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.inheritedFromRoleId = :fromRoleId AND rp.isInherited = true AND rp.isGranted = true")
    List<RolePermission> findInheritedFromRole(@Param("fromRoleId") Long fromRoleId);
    
    /**
     * Count permissions by role
     */
    @Query("SELECT rp.role.id, COUNT(rp) FROM RolePermission rp WHERE rp.isGranted = true GROUP BY rp.role.id")
    List<Object[]> countPermissionsByRole();

    /**
     * Count roles by permission
     */
    @Query("SELECT rp.permission.id, COUNT(rp) FROM RolePermission rp WHERE rp.isGranted = true GROUP BY rp.permission.id")
    List<Object[]> countRolesByPermission();
    
    /**
     * Get role permission statistics
     */
    @Query("SELECT " +
           "COUNT(rp) as totalAssignments, " +
           "COUNT(CASE WHEN rp.isGranted = true THEN 1 END) as activeAssignments, " +
           "COUNT(CASE WHEN rp.isGranted = false THEN 1 END) as revokedAssignments, " +
           "COUNT(CASE WHEN rp.isInherited = true THEN 1 END) as inheritedAssignments " +
           "FROM RolePermission rp")
    Object[] getRolePermissionStatistics();
    
    /**
     * Find role permissions with conditions
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.conditions IS NOT NULL AND rp.isGranted = true")
    List<RolePermission> findPermissionsWithConditions();
    
    /**
     * Delete all permissions for role
     */
    @Query("DELETE FROM RolePermission rp WHERE rp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * Delete all role assignments for permission
     */
    @Query("DELETE FROM RolePermission rp WHERE rp.permission.id = :permissionId")
    void deleteByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * Find permissions granted recently
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.grantedAt >= :since AND rp.isGranted = true ORDER BY rp.grantedAt DESC")
    List<RolePermission> findRecentlyGranted(@Param("since") LocalDateTime since);
    
    /**
     * Find permissions revoked recently
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.revokedAt >= :since ORDER BY rp.revokedAt DESC")
    List<RolePermission> findRecentlyRevoked(@Param("since") LocalDateTime since);
    
    /**
     * Find role permissions by notes
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.notes LIKE %:keyword% ORDER BY rp.createdAt DESC")
    List<RolePermission> findByNotesContaining(@Param("keyword") String keyword);
}
