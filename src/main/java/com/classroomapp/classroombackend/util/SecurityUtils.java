package com.classroomapp.classroombackend.util;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security utility class for authentication and authorization operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {
    
    private final UserRepository userRepository;
    
    /**
     * Get current authenticated user ID
     * @return current user ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Không tìm thấy người dùng đã xác thực trong security context");
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
                
                log.warn("Không tìm thấy người dùng với username/email: {}", username);
                return null;
            }
            
            log.warn("Principal không phải là một instance của UserDetails: {}", principal.getClass());
            return null;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy ID người dùng hiện tại", e);
            return null;
        }
    }
    
    /**
     * Get current authenticated user ID or default value
     * @return current user ID or 1L as default if not authenticated
     */
    public Long getCurrentUserIdOrDefault() {
        Long userId = getCurrentUserId();
        return userId != null ? userId : 1L; // Default to user ID 1 if not authenticated
    }
    
    /**
     * Get current authenticated user ID or specified default
     * @param defaultUserId default user ID to return if not authenticated
     * @return current user ID or default value
     */
    public Long getCurrentUserIdOrDefault(Long defaultUserId) {
        Long userId = getCurrentUserId();
        return userId != null ? userId : defaultUserId;
    }
    
    /**
     * Get current authenticated user
     * @return current user or null if not authenticated
     */
    public User getCurrentUser() {
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
            log.error("Lỗi khi lấy người dùng hiện tại", e);
            return null;
        }
    }
    
    /**
     * Get current user's role ID
     * @return current user's role ID or null if not authenticated
     */
    public Integer getCurrentUserRoleId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getRoleId() : null;
    }
    
    /**
     * Get current user's role name
     * @return current user's role name or null if not authenticated
     */
    public String getCurrentUserRole() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    /**
     * Check if current user has specific role
     * @param roleId the role ID to check
     * @return true if user has the role
     */
    public boolean hasRole(Integer roleId) {
        Integer currentRoleId = getCurrentUserRoleId();
        return currentRoleId != null && currentRoleId.equals(roleId);
    }
    
    /**
     * Check if current user has specific role name
     * @param roleName the role name to check
     * @return true if user has the role
     */
    public boolean hasRole(String roleName) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(roleName);
    }
    
    /**
     * Check if current user is Manager or HR
     * @return true if user is Manager
     */
    public boolean isManager() {
        return hasRole(3) || hasRole("MANAGER");
    }
    
    /**
     * Check if current user is Admin
     * @return true if user is Admin
     */
    public boolean isAdmin() {
        return hasRole(4) || hasRole("ADMIN");
    }
    
    /**
     * Check if current user is Teacher
     * @return true if user is Teacher
     */
    public boolean isTeacher() {
        return hasRole(2) || hasRole("TEACHER");
    }
    
    /**
     * Check if current user is Accountant
     * @return true if user is Accountant
     */
    public boolean isAccountant() {
        return hasRole(5) || hasRole("ACCOUNTANT");
    }
    
    /**
     * Check if current user can manage shifts (Manager or Admin)
     * @return true if user can manage shifts
     */
    public boolean canManageShifts() {
        return isManager() || isAdmin();
    }
    
    /**
     * Check if current user can manage violations (Manager or Admin)
     * @return true if user can manage violations
     */
    public boolean canManageViolations() {
        return isManager() || isAdmin();
    }
    
    /**
     * Check if current user can manage salary (Manager, Admin, or Accountant)
     * @return true if user can manage salary
     */
    public boolean canManageSalary() {
        return isManager() || isAdmin() || isAccountant();
    }
    
    /**
     * Check if current user can view reports (Manager, Admin, or Accountant)
     * @return true if user can view reports
     */
    public boolean canViewReports() {
        return isManager() || isAdmin() || isAccountant();
    }
    
    /**
     * Get current user's email
     * @return current user's email or null if not authenticated
     */
    public String getCurrentUserEmail() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }
    
    /**
     * Get current user's full name
     * @return current user's full name or null if not authenticated
     */
    public String getCurrentUserFullName() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getFullName() : null;
    }
    
    /**
     * Check if current user is authenticated
     * @return true if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
    
    /**
     * Get current username
     * @return current username or null if not authenticated
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy tên đăng nhập hiện tại", e);
            return null;
        }
    }
    
    /**
     * Get current user with exception if not found
     * @return current user or throws exception if not authenticated
     * @throws RuntimeException if user is not authenticated or not found
     */
    public User getCurrentUserRequired() {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("Người dùng chưa xác thực hoặc không tìm thấy");
        }
        return user;
    }
    
    /**
     * Get current user ID with exception if not found
     * @return current user ID or throws exception if not authenticated
     * @throws RuntimeException if user is not authenticated or not found
     */
    public Long getCurrentUserIdRequired() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("Người dùng chưa xác thực hoặc không tìm thấy");
        }
        return userId;
    }
    
    
    /**
     * Check if current user can approve attendance explanations
     * @return true if user can approve attendance explanations
     */
    public boolean canApproveAttendanceExplanations() {
        return isManager() || isAdmin();
    }
    
    /**
     * Check if current user can manage user shift assignments
     * @return true if user can manage shift assignments
     */
    public boolean canManageUserShiftAssignments() {
        return isManager() || isAdmin();
    }
    
    /**
     * Check if current user is HR staff (Manager or Admin)
     * @return true if user is HR staff
     */
    public boolean isHRStaff() {
        return isManager() || isAdmin();
    }
    
    /**
     * Check if current user can submit attendance explanations
     * @return true if user can submit explanations (everyone except Admin)
     */
    public boolean canSubmitAttendanceExplanations() {
        return isAuthenticated() && !isAdmin(); // All authenticated users except Admin can submit explanations
    }
    
    /**
     * Check if current user is eligible for shift assignment
     * @return true if user can be assigned to shifts
     */
    public boolean isEligibleForShiftAssignment() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.isEligibleForShiftAssignment();
    }
    
    /**
     * Get current user department ID (if applicable)
     * @return current user's department ID or null
     */
    public Long getCurrentUserDepartmentId() {
        User currentUser = getCurrentUser();
        // Assuming User has departmentId field, adjust as needed
        return currentUser != null ? currentUser.getDepartmentId() != null ? currentUser.getDepartmentId().longValue() : null : null;
    }
    
    /**
     * Check if current user can view attendance data for specific user
     * @param targetUserId the user ID to check attendance access for
     * @return true if current user can view attendance for target user
     */
    public boolean canViewAttendanceFor(Long targetUserId) {
        if (canManageViolations()) {
            return true; // Managers and Admins can view all attendance
        }
        
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(targetUserId); // Users can view their own attendance
    }
    
    /**
     * Check if current user can generate reports
     * @return true if user can generate HR reports
     */
    public boolean canGenerateReports() {
        return canViewReports();
    }
    
    /**
     * Get current authentication object
     * @return current Authentication or null if not authenticated
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Check if current user can edit attendance records
     * @return true if user can edit attendance records
     */
    public boolean canEditAttendanceRecords() {
        return isManager() || isAdmin();
    }
    
    
    // ===== ROLE GROUPING METHODS FOR FIXING EMPLOYEE/STAFF REFERENCES =====
    
    /**
     * Check if current user is staff (non-student)
     * Staff includes: TEACHER, MANAGER, ADMIN, ACCOUNTANT
     * This replaces the invalid 'EMPLOYEE' role checks
     */
    public boolean isStaff() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }
            
            return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(role -> role.equals("ROLE_TEACHER") || 
                               role.equals("ROLE_MANAGER") || 
                               role.equals("ROLE_ADMIN") || 
                               role.equals("ROLE_ACCOUNTANT"));
        } catch (Exception e) {
            log.warn("Lỗi khi kiểm tra vai trò nhân sự: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if current user is employee (same as staff for this system)
     * Employee includes: TEACHER, MANAGER, ADMIN, ACCOUNTANT
     * This replaces the invalid 'EMPLOYEE' role checks
     */
    public boolean isEmployee() {
        return isStaff(); // In this system, employee = staff
    }
    
    /**
     * Check if current user is administrative staff
     * Admin staff includes: MANAGER, ADMIN, ACCOUNTANT
     */
    public boolean isAdminStaff() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }
            
            return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(role -> role.equals("ROLE_MANAGER") || 
                               role.equals("ROLE_ADMIN") || 
                               role.equals("ROLE_ACCOUNTANT"));
        } catch (Exception e) {
            log.warn("Lỗi khi kiểm tra vai trò nhân sự hành chính: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if current user is teaching staff
     */
    public boolean isTeachingStaff() {
        return isTeacher();
    }
    
    /**
     * Get user role as clean string (without ROLE_ prefix)
     */
    public String getUserRoleClean() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            
            return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            log.warn("Lỗi khi lấy vai trò người dùng: {}", e.getMessage());
            return null;
        }
    }
}
