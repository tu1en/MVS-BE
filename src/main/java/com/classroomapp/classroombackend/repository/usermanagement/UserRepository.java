package com.classroomapp.classroombackend.repository.usermanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email
     * @param email the email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

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
}
