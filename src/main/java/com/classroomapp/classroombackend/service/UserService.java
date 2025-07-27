package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.enums.UserRole;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.usermanagement.User.RoleEnum;

public interface UserService {
    List<UserDto> getAllUsers();
    
    UserDto getUserById(Long id);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    List<UserDto> getUsersByRole(UserRole role);
    
    Page<UserDto> findAllUsers(String keyword, Pageable pageable);
    
    UserDto createUser(UserDto userDto);
    
    UserDto updateUser(Long id, UserDto userDto);
    
    UserDto updateUserStatus(Long userId, boolean enabled);
    
    UserDto updateUserRoles(Long userId, Set<String> roleNames);
    
    void deleteUser(Long id);
    
    void resetPassword(Long id);
    
    boolean usernameExists(String username);
    
    boolean emailExists(String email);
    
    void sendPasswordResetEmail(String email, String resetLink);
    
    User save(User user);
    
    long count();

    // =====================================================
    // PHASE 1: New methods for role_enum and soft delete
    // =====================================================

    /**
     * Find users by role enum (excluding soft deleted)
     * @param roleEnum the role enum to search for
     * @return List of users with the specified role and not deleted
     */
    List<User> findByRoleEnum(RoleEnum roleEnum);

    /**
     * Find active users by role enum
     * @param roleEnum the role enum
     * @return List of active users with the specified role
     */
    List<User> findActiveUsersByRoleEnum(RoleEnum roleEnum);

    /**
     * Soft delete user
     * @param id the user ID to soft delete
     */
    void softDeleteUser(Long id);

    /**
     * Restore soft deleted user
     * @param id the user ID to restore
     */
    void restoreUser(Long id);

    /**
     * Update user last login time
     * @param username the username
     */
    void updateLastLogin(String username);

    /**
     * Find users eligible for course assignment (Teachers and Managers)
     * @return List of users who can be assigned to courses
     */
    List<User> findUsersEligibleForCourseAssignment();

    /**
     * Get user statistics by role
     * @return Statistics of users by role enum
     */
    java.util.Map<RoleEnum, Long> getUserStatisticsByRole();

    /**
     * Validate user permissions for specific action
     * @param userId the user ID
     * @param action the action to validate
     * @param resourceType the resource type
     * @param resourceId the specific resource ID (optional)
     * @return true if user has permission, false otherwise
     */
    boolean validateUserPermission(Long userId, String action, String resourceType, Long resourceId);

    /**
     * Find user by username (excluding soft deleted)
     * @param username the username
     * @return Optional containing user if found and not deleted
     */
    Optional<User> findByUsernameActive(String username);

    /**
     * Find user by email (excluding soft deleted)
     * @param email the email
     * @return Optional containing user if found and not deleted
     */
    Optional<User> findByEmailActive(String email);
}