package com.classroomapp.classroombackend.repository.parentmanagement;

import com.classroomapp.classroombackend.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Parent entity
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    /**
     * Find parent by user ID
     */
    Optional<Parent> findByUserId(Long userId);

    /**
     * Find parent by email
     */
    Optional<Parent> findByEmail(String email);

    /**
     * Find parent by phone number
     */
    Optional<Parent> findByPhone(String phone);

    /**
     * Find active parents
     */
    List<Parent> findByStatus(Parent.ParentStatus status);

    /**
     * Find parents by name (case insensitive, partial match)
     */
    @Query("SELECT p FROM Parent p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Parent> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Check if parent exists by user ID
     */
    boolean existsByUserId(Long userId);

    /**
     * Check if parent exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if parent exists by phone
     */
    boolean existsByPhone(String phone);

    /**
     * Find parents with children count
     */
    @Query("SELECT p FROM Parent p LEFT JOIN FETCH p.studentParents sp WHERE p.status = :status")
    List<Parent> findActiveParentsWithChildren(@Param("status") Parent.ParentStatus status);

    /**
     * Find parent by student ID through relationship
     */
    @Query("SELECT DISTINCT p FROM Parent p " +
           "JOIN p.studentParents sp " +
           "WHERE sp.studentId = :studentId AND sp.endAt IS NULL")
    List<Parent> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find primary parent for student
     */
    @Query("SELECT p FROM Parent p " +
           "JOIN p.studentParents sp " +
           "WHERE sp.studentId = :studentId AND sp.isPrimary = true AND sp.endAt IS NULL")
    Optional<Parent> findPrimaryParentByStudentId(@Param("studentId") Long studentId);

    /**
     * Count total children for parent
     */
    @Query("SELECT COUNT(sp) FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.endAt IS NULL")
    Long countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Find parents with pending leave notices
     */
    @Query("SELECT DISTINCT p FROM Parent p " +
           "JOIN p.leaveNotices ln " +
           "WHERE ln.status IN ('SENT', 'DELIVERED')")
    List<Parent> findParentsWithPendingLeaveNotices();
}