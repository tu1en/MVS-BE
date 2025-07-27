package com.classroomapp.classroombackend.repository.usermanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.usermanagement.User.RoleEnum;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username (including soft deleted)
     * @param username the username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by username (excluding soft deleted)
     * @param username the username to search for
     * @return Optional containing user if found and not deleted
     */
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    /**
     * Find a user by email (including soft deleted)
     * @param email the email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by email (excluding soft deleted)
     * @param email the email to search for
     * @return Optional containing user if found and not deleted
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role ID
     * @param roleId the role ID to search for
     * @return List of users with the specified role
     */
    List<User> findByRoleId(Integer roleId);

    /**
     * Find users by status
     * @param status the status to search for
     * @return List of users with the specified status
     */
    List<User> findByStatus(String status);

    /**
     * Find users by department
     * @param department the department to search for
     * @return List of users in the specified department
     */
    List<User> findByDepartment(String department);

    /**
     * Find users by role and status
     * @param roleId the role ID
     * @param status the status
     * @return List of users matching both role and status
     */
    List<User> findByRoleIdAndStatus(Integer roleId, String status);

    /**
     * Find active teachers
     * @return List of active teachers
     */
    @Query("SELECT u FROM User u WHERE u.roleId = 2 AND u.status = 'active'")
    List<User> findActiveTeachers();

    /**
     * Find active students
     * @return List of active students
     */
    @Query("SELECT u FROM User u WHERE u.roleId = 1 AND u.status = 'active'")
    List<User> findActiveStudents();

    /**
     * Search users by name containing keyword
     * @param keyword the search keyword
     * @return List of users whose full name contains the keyword
     */
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.username LIKE %:keyword%")
    List<User> searchUsersByName(@Param("keyword") String keyword);

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email, Pageable pageable);

    List<User> findAllByRoleId(Long roleId);

    @Query("SELECT COUNT(DISTINCT e.user.id) FROM ClassroomEnrollment e WHERE e.classroom.id IN :classroomIds")
    long countStudentsByClassroomIds(@Param("classroomIds") List<Long> classroomIds);

    /**
     * Find users by roleId in list
     * @param roleIds danh sách roleId
     * @return List<User>
     */
    List<User> findByRoleIdIn(List<Integer> roleIds);

    /**
     * Count users by role ID
     * @param roleId the role ID to count
     * @return count of users with the specified role
     */
    long countByRoleId(int roleId);

    /**
     * Find all active users
     * @return List of active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'active'")
    List<User> findActiveUsers();

    /**
     * Search users by keyword in fullName, email or username
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return Page of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // =====================================================
    // PHASE 1: New methods for role_enum and soft delete
    // =====================================================

    /**
     * Find users by role enum (excluding soft deleted)
     * @param roleEnum the role enum to search for
     * @return List of users with the specified role and not deleted
     */
    List<User> findByRoleEnumAndIsDeletedFalse(RoleEnum roleEnum);

    /**
     * Find users by role enum and status (excluding soft deleted)
     * @param roleEnum the role enum
     * @param status the status
     * @return List of users matching both role and status and not deleted
     */
    List<User> findByRoleEnumAndStatusAndIsDeletedFalse(RoleEnum roleEnum, String status);

    /**
     * Override default findAll to exclude soft deleted users
     * @return List of users that are not deleted
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAll();

    /**
     * Override default findById to exclude soft deleted users
     * @param id the user ID
     * @return Optional containing user if found and not deleted
     */
    @Query("SELECT u FROM User u WHERE u.id = ?1 AND u.isDeleted = false")
    Optional<User> findById(Long id);

    /**
     * Find active teachers using role enum
     * @return List of active teachers (not deleted)
     */
    @Query("SELECT u FROM User u WHERE u.roleEnum = 'TEACHER' AND u.status = 'active' AND u.isDeleted = false")
    List<User> findActiveTeachersWithRoleEnum();

    /**
     * Find active students using role enum
     * @return List of active students (not deleted)
     */
    @Query("SELECT u FROM User u WHERE u.roleEnum = 'STUDENT' AND u.status = 'active' AND u.isDeleted = false")
    List<User> findActiveStudentsWithRoleEnum();

    /**
     * Find active managers using role enum
     * @return List of active managers (not deleted)
     */
    @Query("SELECT u FROM User u WHERE u.roleEnum = 'MANAGER' AND u.status = 'active' AND u.isDeleted = false")
    List<User> findActiveManagers();

    /**
     * Count users by role enum (excluding soft deleted)
     * @param roleEnum the role enum to count
     * @return count of users with the specified role and not deleted
     */
    long countByRoleEnumAndIsDeletedFalse(RoleEnum roleEnum);

    /**
     * Search users by keyword (excluding soft deleted)
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return Page of users matching the search criteria and not deleted
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchByKeywordExcludingDeleted(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find users by multiple role enums (excluding soft deleted)
     * @param roleEnums the list of role enums
     * @return List of users with any of the specified roles and not deleted
     */
    List<User> findByRoleEnumInAndIsDeletedFalse(List<RoleEnum> roleEnums);

    /**
     * Find users eligible for course assignment (Teachers and Managers)
     * @return List of users who can be assigned to courses
     */
    @Query("SELECT u FROM User u WHERE u.roleEnum IN ('TEACHER', 'MANAGER') AND u.status = 'active' AND u.isDeleted = false")
    List<User> findUsersEligibleForCourseAssignment();
}
