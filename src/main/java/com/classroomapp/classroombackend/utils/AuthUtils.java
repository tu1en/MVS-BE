package com.classroomapp.classroombackend.utils;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for authentication-related operations
 */
@Component
@Slf4j
public class AuthUtils {
    
    private static UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        AuthUtils.userRepository = userRepository;
    }
    
    /**
     * Get current authenticated user ID
     * @return current user ID or null if not authenticated
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String username = userDetails.getUsername();
                
                // Find user by username (which is email in our system)
                Optional<User> userOpt = userRepository.findByEmail(username);
                if (userOpt.isPresent()) {
                    return userOpt.get().getId();
                } else {
                    // Try finding by username field
                    userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        return userOpt.get().getId();
                    }
                }
                
                log.warn("User not found with username/email: {}", username);
                return null;
            }
            
            log.warn("Principal is not an instance of UserDetails: {}", principal.getClass());
            return null;
            
        } catch (Exception e) {
            log.error("Error getting current user ID", e);
            return null;
        }
    }
    
    /**
     * Get current authenticated user
     * @return current user or null if not authenticated
     */
    public static User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String username = userDetails.getUsername();
                
                // Find user by email first
                Optional<User> userOpt = userRepository.findByEmail(username);
                if (userOpt.isPresent()) {
                    return userOpt.get();
                } else {
                    // Try finding by username field
                    userOpt = userRepository.findByUsername(username);
                    return userOpt.orElse(null);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }
    
    /**
     * Get current user's role ID
     * @return current user's role ID or null if not authenticated
     */
    public static Integer getCurrentUserRoleId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getRoleId() : null;
    }
    
    /**
     * Get current user's role name
     * @return current user's role name or null if not authenticated
     */
    public static String getCurrentUserRole() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    /**
     * Check if current user has specific role
     * @param roleId the role ID to check
     * @return true if user has the role
     */
    public static boolean hasRole(Integer roleId) {
        Integer currentRoleId = getCurrentUserRoleId();
        return currentRoleId != null && currentRoleId.equals(roleId);
    }
    
    /**
     * Check if current user has specific role name
     * @param roleName the role name to check
     * @return true if user has the role
     */
    public static boolean hasRole(String roleName) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(roleName);
    }
    
    /**
     * Check if current user is Manager or HR
     * @return true if user is Manager
     */
    public static boolean isManager() {
        return hasRole(3) || hasRole("MANAGER");
    }
    
    /**
     * Check if current user is Admin
     * @return true if user is Admin
     */
    public static boolean isAdmin() {
        return hasRole(4) || hasRole("ADMIN");
    }
    
    /**
     * Check if current user is Teacher
     * @return true if user is Teacher
     */
    public static boolean isTeacher() {
        return hasRole(2) || hasRole("TEACHER");
    }
    
    /**
     * Check if current user is Accountant
     * @return true if user is Accountant
     */
    public static boolean isAccountant() {
        return hasRole(5) || hasRole("ACCOUNTANT");
    }
    
    /**
     * Check if current user can manage shifts (Manager or Admin)
     * @return true if user can manage shifts
     */
    public static boolean canManageShifts() {
        return isManager() || isAdmin();
    }
    
    /**
     * Check if current user is eligible for shift assignment (not Teacher)
     * @return true if user can be assigned shifts
     */
    public static boolean isEligibleForShiftAssignment() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.isEligibleForShiftAssignment();
    }
    
    /**
     * Get current user's email
     * @return current user's email or null if not authenticated
     */
    public static String getCurrentUserEmail() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }
    
    /**
     * Get current user's full name
     * @return current user's full name or null if not authenticated
     */
    public static String getCurrentUserFullName() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getFullName() : null;
    }
}
