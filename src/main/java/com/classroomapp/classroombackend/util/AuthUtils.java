package com.classroomapp.classroombackend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for authentication related operations
 */
@Component
public class AuthUtils {

    /**
     * Get current authentication
     * @return Authentication object
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get current username
     * @return username string
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Get current user ID (simplified implementation)
     * @return user ID as Long
     */
    public static Long getCurrentUserId() {
        // Simple implementation - in real app, you'd get this from JWT token or UserDetails
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        // For now, return a hash of the username as ID
        return (long) Math.abs(username.hashCode());
    }

    /**
     * Check if user has role
     * @param role role to check
     * @return true if user has role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    /**
     * Check if user has any of the given roles
     * @param roles array of roles to check
     * @return true if user has any of the roles
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    for (String role : roles) {
                        if (grantedAuthority.getAuthority().equals(role)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}