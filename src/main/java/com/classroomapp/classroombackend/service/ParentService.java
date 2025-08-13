package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.StudentParent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for Parent management
 * Based on PARENT_ROLE_SPEC.md requirements
 */
public interface ParentService {

    /**
     * Create a new parent
     */
    Parent createParent(Parent parent);

    /**
     * Update parent information
     */
    Parent updateParent(Long parentId, Parent parent);

    /**
     * Get parent by ID
     */
    Optional<Parent> getParentById(Long parentId);

    /**
     * Get parent by user ID
     */
    Optional<Parent> getParentByUserId(Long userId);

    /**
     * Get parent by email
     */
    Optional<Parent> getParentByEmail(String email);

    /**
     * Get all children for a parent
     */
    List<StudentParent> getChildrenByParentId(Long parentId);

    /**
     * Get child IDs for parent (for JWT token)
     */
    List<Long> getChildIdsByParentId(Long parentId);

    /**
     * Link parent to student
     */
    StudentParent linkParentToStudent(Long parentId, Long studentId, StudentParent.RelationType relationType, 
                                     Boolean isPrimary, Boolean legalGuardian);

    /**
     * Unlink parent from student
     */
    void unlinkParentFromStudent(Long parentId, Long studentId);

    /**
     * Check if parent has access to student
     */
    boolean hasAccessToStudent(Long parentId, Long studentId);

    /**
     * Get all parents for a student
     */
    List<Parent> getParentsByStudentId(Long studentId);

    /**
     * Get primary parent for student
     */
    Optional<Parent> getPrimaryParentByStudentId(Long studentId);

    /**
     * Update parent status
     */
    Parent updateParentStatus(Long parentId, Parent.ParentStatus status);

    /**
     * Get active parents
     */
    List<Parent> getActiveParents();

    /**
     * Search parents by name
     */
    List<Parent> searchParentsByName(String name);

    /**
     * Count children for parent
     */
    Long countChildrenByParentId(Long parentId);

    /**
     * Validate parent-student relationship
     */
    boolean validateParentStudentRelationship(Long parentId, Long studentId);

    /**
     * Create parent from user registration
     */
    Parent createParentFromUser(Long userId, String name, String phone, String email);

    /**
     * Get parents with pending leave notices
     */
    List<Parent> getParentsWithPendingLeaveNotices();

    /**
     * Get child's schedule (timetable) for date range
     */
    List<Map<String, Object>> getChildSchedule(Long childId, LocalDate startDate, LocalDate endDate);

    /**
     * Get child's exam schedule for date range
     */
    List<Map<String, Object>> getChildExams(Long childId, LocalDate startDate, LocalDate endDate);

    /**
     * Get child's billing data (invoices and payments)
     */
    Map<String, Object> getChildBillingData(Long childId, LocalDate startDate, LocalDate endDate);

    /**
     * Check if parent has billing access for child
     */
    boolean hasChildBillingAccess(Long parentId, Long childId);

    /**
     * Get billing document (invoice or receipt) as byte array
     */
    byte[] getBillingDocument(Long parentId, Long documentId, String type);
}