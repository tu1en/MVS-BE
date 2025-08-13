package com.classroomapp.classroombackend.repository.parentmanagement;

import com.classroomapp.classroombackend.model.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentParent relationship entity
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Repository
public interface StudentParentRepository extends JpaRepository<StudentParent, Long> {

    /**
     * Find all children for a parent
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.endAt IS NULL")
    List<StudentParent> findActiveChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Find all parents for a student
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.studentId = :studentId AND sp.endAt IS NULL")
    List<StudentParent> findActiveParentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Find specific relationship
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.studentId = :studentId AND sp.endAt IS NULL")
    Optional<StudentParent> findActiveRelationship(@Param("parentId") Long parentId, @Param("studentId") Long studentId);

    /**
     * Find primary relationships for parent
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.isPrimary = true AND sp.endAt IS NULL")
    List<StudentParent> findPrimaryRelationshipsByParentId(@Param("parentId") Long parentId);

    /**
     * Find primary parent for student
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.studentId = :studentId AND sp.isPrimary = true AND sp.endAt IS NULL")
    Optional<StudentParent> findPrimaryParentByStudentId(@Param("studentId") Long studentId);

    /**
     * Find legal guardians for student
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.studentId = :studentId AND sp.legalGuardian = true AND sp.endAt IS NULL")
    List<StudentParent> findLegalGuardiansByStudentId(@Param("studentId") Long studentId);

    /**
     * Find relationships by relation type
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.relationType = :relationType AND sp.endAt IS NULL")
    List<StudentParent> findByParentIdAndRelationType(@Param("parentId") Long parentId, 
                                                      @Param("relationType") StudentParent.RelationType relationType);

    /**
     * Check if relationship exists
     */
    @Query("SELECT COUNT(sp) > 0 FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.studentId = :studentId AND sp.endAt IS NULL")
    boolean existsActiveRelationship(@Param("parentId") Long parentId, @Param("studentId") Long studentId);

    /**
     * Count active children for parent
     */
    @Query("SELECT COUNT(sp) FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.endAt IS NULL")
    Long countActiveChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Count active parents for student
     */
    @Query("SELECT COUNT(sp) FROM StudentParent sp WHERE sp.studentId = :studentId AND sp.endAt IS NULL")
    Long countActiveParentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Find relationships ending in date range
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.endAt BETWEEN :startDate AND :endDate")
    List<StudentParent> findRelationshipsEndingBetween(@Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Find all historical relationships for student
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.studentId = :studentId ORDER BY sp.startAt DESC")
    List<StudentParent> findAllRelationshipsByStudentId(@Param("studentId") Long studentId);

    /**
     * Find all historical relationships for parent
     */
    @Query("SELECT sp FROM StudentParent sp WHERE sp.parentId = :parentId ORDER BY sp.startAt DESC")
    List<StudentParent> findAllRelationshipsByParentId(@Param("parentId") Long parentId);

    /**
     * Find relationships with detailed parent and student info
     */
    @Query("SELECT sp FROM StudentParent sp " +
           "LEFT JOIN FETCH sp.parent " +
           "LEFT JOIN FETCH sp.student " +
           "WHERE sp.parentId = :parentId AND sp.endAt IS NULL")
    List<StudentParent> findActiveRelationshipsWithDetailsForParent(@Param("parentId") Long parentId);

    /**
     * Find students IDs for parent (for JWT childIds)
     */
    @Query("SELECT sp.studentId FROM StudentParent sp WHERE sp.parentId = :parentId AND sp.endAt IS NULL")
    List<Long> findStudentIdsByParentId(@Param("parentId") Long parentId);
}