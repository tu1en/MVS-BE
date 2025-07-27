package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface ClassroomEnrollmentRepository extends JpaRepository<ClassroomEnrollment, ClassroomEnrollmentId> {
    
    // Find enrollments by classroom ID
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId")
    List<ClassroomEnrollment> findByClassroomId(@Param("classroomId") Long classroomId);
    
    // Find enrollments by user ID
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.userId = :userId")
    List<ClassroomEnrollment> findByUserId(@Param("userId") Long userId);
    
    // Find specific enrollment by composite key components
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId AND ce.id.userId = :userId")
    Optional<ClassroomEnrollment> findByClassroomIdAndUserId(@Param("classroomId") Long classroomId, @Param("userId") Long userId);
    
    // Check if enrollment exists
    @Query("SELECT COUNT(ce) > 0 FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId AND ce.id.userId = :userId")
    boolean existsByClassroomIdAndUserId(@Param("classroomId") Long classroomId, @Param("userId") Long userId);
    
    // Count students in classroom
    @Query("SELECT COUNT(ce) FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId")
    long countByClassroomId(@Param("classroomId") Long classroomId);
    
    // Count classrooms for student
    @Query("SELECT COUNT(ce) FROM ClassroomEnrollment ce WHERE ce.id.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // Find with joins for performance - by classroom
    @Query("SELECT ce FROM ClassroomEnrollment ce " +
           "JOIN FETCH ce.classroom c " +
           "JOIN FETCH ce.user u " +
           "WHERE ce.id.classroomId = :classroomId")
    List<ClassroomEnrollment> findByClassroomIdWithDetails(@Param("classroomId") Long classroomId);
    
    // Find with joins for performance - by user
    @Query("SELECT ce FROM ClassroomEnrollment ce " +
           "JOIN FETCH ce.classroom c " +
           "JOIN FETCH ce.user u " +
           "WHERE ce.id.userId = :userId")
    List<ClassroomEnrollment> findByUserIdWithDetails(@Param("userId") Long userId);
    
    // Active enrollments only
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.userId = :userId AND ce.status = 'ACTIVE'")
    List<ClassroomEnrollment> findActiveEnrollmentsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId AND ce.status = 'ACTIVE'")
    List<ClassroomEnrollment> findActiveEnrollmentsByClassroomId(@Param("classroomId") Long classroomId);
    
    // Delete specific enrollment using composite key components
    @Modifying
    @Transactional
    @Query("DELETE FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId AND ce.id.userId = :userId")
    void deleteByClassroomIdAndUserId(@Param("classroomId") Long classroomId, @Param("userId") Long userId);
    
    // SOLUTION 1: Use roleId field (recommended - assuming STUDENT role has ID = 1)
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
           "(SELECT ce.id.userId FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId) " +
           "AND u.roleId = 1")
    List<User> findStudentsNotInClassroom(@Param("classroomId") Long classroomId);

    // SOLUTION 2: Use string literal for enum comparison (FIXED VERSION)
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
           "(SELECT ce.id.userId FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId) " +
           "AND u.roleEnum = 'STUDENT'")
    List<User> findStudentsNotInClassroomByRoleEnum(@Param("classroomId") Long classroomId);

    // SOLUTION 3: Filter by roleId with parameter for flexibility
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
           "(SELECT ce.id.userId FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId) " +
           "AND u.roleId = :roleId")
    List<User> findUsersNotInClassroomByRoleId(@Param("classroomId") Long classroomId, @Param("roleId") Integer roleId);

    // SOLUTION 4: Generic method without role filtering (filter in service layer)
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
           "(SELECT ce.id.userId FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId)")
    List<User> findUsersNotInClassroom(@Param("classroomId") Long classroomId);

    // SOLUTION 5: Using a more specific join approach
    @Query("SELECT u FROM User u " +
           "LEFT JOIN ClassroomEnrollment ce ON u.id = ce.id.userId AND ce.id.classroomId = :classroomId " +
           "WHERE ce.id.userId IS NULL AND u.roleId = 1")
    List<User> findUsersNotEnrolledInClassroom(@Param("classroomId") Long classroomId);

    // Find enrollments by classroom ID (alternative method name for compatibility)
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId")
    List<ClassroomEnrollment> findById_ClassroomId(@Param("classroomId") Long classroomId);

    // Find enrollments by user ID (alternative method name for compatibility) 
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.userId = :userId")
    List<ClassroomEnrollment> findById_UserId(@Param("userId") Long userId);

    // Get enrollment with progress details
    @Query("SELECT ce FROM ClassroomEnrollment ce " +
           "WHERE ce.id.classroomId = :classroomId AND ce.id.userId = :userId")
    Optional<ClassroomEnrollment> findEnrollmentWithProgress(@Param("classroomId") Long classroomId, @Param("userId") Long userId);

    // Bulk update progress
    @Modifying
    @Transactional
    @Query("UPDATE ClassroomEnrollment ce SET ce.progressPercentage = :progress, ce.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ce.id.classroomId = :classroomId AND ce.id.userId = :userId")
    int updateProgress(@Param("classroomId") Long classroomId, @Param("userId") Long userId, @Param("progress") Double progress);
    
    // Additional utility queries
    
    // Find all students in a classroom (returns User objects)
    @Query("SELECT ce.user FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId AND ce.status = 'ACTIVE'")
    List<User> findStudentsByClassroomId(@Param("classroomId") Long classroomId);
    
    // Find all classrooms for a student (returns Classroom objects)
    @Query("SELECT ce.classroom FROM ClassroomEnrollment ce WHERE ce.id.userId = :userId AND ce.status = 'ACTIVE'")
    List<com.classroomapp.classroombackend.model.classroommanagement.Classroom> findClassroomsByUserId(@Param("userId") Long userId);
    
    // Get enrollment summary for dashboard
    @Query("SELECT ce.id.classroomId, ce.classroom.name, ce.progressPercentage, ce.status, ce.enrolledAt " +
           "FROM ClassroomEnrollment ce WHERE ce.id.userId = :userId")
    List<Object[]> findEnrollmentSummaryByUserId(@Param("userId") Long userId);
    
    // Find enrollments by status
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.status = :status")
    List<ClassroomEnrollment> findByStatus(@Param("status") com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment.EnrollmentStatus status);
    
    // Count active enrollments in classroom
    @Query("SELECT COUNT(ce) FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId AND ce.status = 'ACTIVE'")
    long countActiveEnrollmentsByClassroomId(@Param("classroomId") Long classroomId);
    
    // Find enrollments with low progress (for intervention)
    @Query("SELECT ce FROM ClassroomEnrollment ce WHERE ce.id.classroomId = :classroomId " +
           "AND ce.progressPercentage < :threshold AND ce.status = 'ACTIVE'")
    List<ClassroomEnrollment> findLowProgressEnrollments(@Param("classroomId") Long classroomId, @Param("threshold") Double threshold);
}