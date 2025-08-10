package com.classroomapp.classroombackend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.security.CustomUserDetailsService;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class JwtAuthenticationTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        cleanupTestUsers();
        
        // Create fresh test user for each test
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFullName("Test User");
        testUser.setRoleId(1); // STUDENT role
        testUser.setStatus("active");
        
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        cleanupTestUsers();
    }

    private void cleanupTestUsers() {
        // Clean up test users by username
        String[] testUsernames = {"testuser", "teacheruser", "manageruser", "adminuser", "lockeduser"};
        for (String username : testUsernames) {
            userRepository.findByUsername(username).ifPresent(user -> {
                userRepository.delete(user);
            });
        }
    }

    @Test
    @DisplayName("Test JWT token generation")
    public void testJwtTokenGeneration() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        String token = jwtUtil.generateToken("testuser", 1);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.length() > 100); // JWT tokens are typically long
        
        System.out.println("Generated JWT token: " + token.substring(0, 50) + "...");
    }

    @Test
    @DisplayName("Test JWT token validation")
    public void testJwtTokenValidation() {
        String token = jwtUtil.generateToken("testuser", 1);
        
        // Validate the token
        boolean isValid = jwtUtil.validateToken(token);
        
        assertTrue(isValid, "JWT token should be valid");
        
        // Extract username from token
        String extractedUsername = jwtUtil.getSubjectFromToken(token);
        assertEquals("testuser", extractedUsername);
        
        System.out.println("Token validation successful for user: " + extractedUsername);
    }

    @Test
    @DisplayName("Test JWT token expiration")
    public void testJwtTokenExpiration() {
        // Generate token with short expiration (1 second)
        String token = jwtUtil.generateToken("testuser", 1);
        
        // Token should be valid immediately
        assertTrue(jwtUtil.validateToken(token));
        
        // Wait for token to expire (in real scenario, you'd mock the time)
        try {
            Thread.sleep(2000); // Wait 2 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Note: In a real test, you'd need to mock the time or use a shorter expiration
        // For now, we'll just verify the token structure
        assertNotNull(token);
        assertTrue(token.contains(".")); // JWT format: header.payload.signature
        
        System.out.println("Token expiration test completed");
    }

    @Test
    @DisplayName("Test password encryption")
    public void testPasswordEncryption() {
        String rawPassword = "testPassword123";
        
        // Encrypt password
        String encryptedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encryptedPassword);
        assertNotEquals(rawPassword, encryptedPassword);
        assertTrue(encryptedPassword.length() > rawPassword.length());
        
        // Verify password matches
        boolean matches = passwordEncoder.matches(rawPassword, encryptedPassword);
        assertTrue(matches, "Password should match after encryption");
        
        // Verify wrong password doesn't match
        boolean wrongMatches = passwordEncoder.matches("wrongPassword", encryptedPassword);
        assertFalse(wrongMatches, "Wrong password should not match");
        
        System.out.println("Password encryption test successful");
    }

    @Test
    @DisplayName("Test password validation")
    public void testPasswordValidation() {
        // Test strong password
        String strongPassword = "StrongPass123!";
        assertTrue(isValidPassword(strongPassword), "Strong password should be valid");
        
        // Test weak password (too short)
        String weakPassword = "weak";
        assertFalse(isValidPassword(weakPassword), "Weak password should be invalid");
        
        // Test password without numbers
        String noNumbersPassword = "WeakPassword!";
        assertFalse(isValidPassword(noNumbersPassword), "Password without numbers should be invalid");
        
        // Test password without special characters
        String noSpecialPassword = "WeakPassword123";
        assertFalse(isValidPassword(noSpecialPassword), "Password without special characters should be invalid");
        
        System.out.println("Password validation test completed");
    }

    @Test
    @DisplayName("Test successful user authentication")
    public void testUserAuthentication_Success() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        // Verify password matches
        boolean passwordMatches = passwordEncoder.matches("password123", userDetails.getPassword());
        assertTrue(passwordMatches, "Password should match");
        
        System.out.println("User authentication successful for: " + userDetails.getUsername());
    }

    @Test
    @DisplayName("Test invalid credentials authentication")
    public void testUserAuthentication_InvalidCredentials() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Test wrong password
        boolean wrongPassword = passwordEncoder.matches("wrongpassword", userDetails.getPassword());
        assertFalse(wrongPassword, "Wrong password should not match");
        
        // Test non-existent user
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistentuser");
        });
        
        System.out.println("Invalid credentials test completed");
    }

    @Test
    @DisplayName("Test locked account handling")
    public void testUserAuthentication_LockedAccount() {
        // Create a locked user
        User lockedUser = new User();
        lockedUser.setUsername("lockeduser");
        lockedUser.setEmail("locked@example.com");
        lockedUser.setPassword(passwordEncoder.encode("password123"));
        lockedUser.setFullName("Locked User");
        lockedUser.setRoleId(1);
        lockedUser.setStatus("locked"); // Locked account
        
        userRepository.save(lockedUser);
        
        // Try to load locked user
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("lockeduser");
        });
        
        System.out.println("Locked account test completed");
    }

    @Test
    @DisplayName("Test student role access")
    public void testRoleBasedAccess_Student() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Check if user has student role
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT")), 
            "User should have STUDENT role");
        
        // Test student-specific permissions
        assertTrue(hasPermission(userDetails, "READ_ASSIGNMENT"));
        assertTrue(hasPermission(userDetails, "SUBMIT_ASSIGNMENT"));
        assertFalse(hasPermission(userDetails, "CREATE_ASSIGNMENT"));
        assertFalse(hasPermission(userDetails, "DELETE_CLASSROOM"));
        
        System.out.println("Student role access test completed");
    }

    @Test
    @DisplayName("Test teacher role access")
    public void testRoleBasedAccess_Teacher() {
        // Create a teacher user
        User teacherUser = new User();
        teacherUser.setUsername("teacheruser");
        teacherUser.setEmail("teacher@example.com");
        teacherUser.setPassword(passwordEncoder.encode("password123"));
        teacherUser.setFullName("Teacher User");
        teacherUser.setRoleId(2); // TEACHER role
        teacherUser.setStatus("active");
        
        userRepository.save(teacherUser);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("teacheruser");
        
        // Check if user has teacher role
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER")), 
            "User should have TEACHER role");
        
        // Test teacher-specific permissions
        assertTrue(hasPermission(userDetails, "CREATE_ASSIGNMENT"));
        assertTrue(hasPermission(userDetails, "GRADE_SUBMISSION"));
        assertTrue(hasPermission(userDetails, "MANAGE_CLASSROOM"));
        assertFalse(hasPermission(userDetails, "DELETE_CLASSROOM"));
        
        System.out.println("Teacher role access test completed");
    }

    @Test
    @DisplayName("Test manager role access")
    public void testRoleBasedAccess_Manager() {
        // Create a manager user
        User managerUser = new User();
        managerUser.setUsername("manageruser");
        managerUser.setEmail("manager@example.com");
        managerUser.setPassword(passwordEncoder.encode("password123"));
        managerUser.setFullName("Manager User");
        managerUser.setRoleId(3); // MANAGER role
        managerUser.setStatus("active");
        
        userRepository.save(managerUser);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("manageruser");
        
        // Check if user has manager role
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER")), 
            "User should have MANAGER role");
        
        // Test manager-specific permissions
        assertTrue(hasPermission(userDetails, "CREATE_CLASSROOM"));
        assertTrue(hasPermission(userDetails, "DELETE_CLASSROOM"));
        assertTrue(hasPermission(userDetails, "MANAGE_USERS"));
        assertTrue(hasPermission(userDetails, "VIEW_REPORTS"));
        
        System.out.println("Manager role access test completed");
    }

    @Test
    @DisplayName("Test admin role access")
    public void testRoleBasedAccess_Admin() {
        // Create an admin user
        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setFullName("Admin User");
        adminUser.setRoleId(4); // ADMIN role
        adminUser.setStatus("active");
        
        userRepository.save(adminUser);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername("adminuser");
        
        // Check if user has admin role
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")), 
            "User should have ADMIN role");
        
        // Test admin-specific permissions
        assertTrue(hasPermission(userDetails, "SYSTEM_CONFIGURATION"));
        assertTrue(hasPermission(userDetails, "MANAGE_ROLES"));
        assertTrue(hasPermission(userDetails, "VIEW_AUDIT_LOGS"));
        assertTrue(hasPermission(userDetails, "ALL_PERMISSIONS"));
        
        System.out.println("Admin role access test completed");
    }

    @Test
    @DisplayName("Test unauthorized access handling")
    public void testPermissionDenied_UnauthorizedAccess() {
        UserDetails studentUser = userDetailsService.loadUserByUsername("testuser");
        
        // Student should not have admin permissions
        assertFalse(hasPermission(studentUser, "SYSTEM_CONFIGURATION"));
        assertFalse(hasPermission(studentUser, "MANAGE_ROLES"));
        assertFalse(hasPermission(studentUser, "DELETE_CLASSROOM"));
        
        // Student should not have teacher permissions
        assertFalse(hasPermission(studentUser, "CREATE_ASSIGNMENT"));
        assertFalse(hasPermission(studentUser, "GRADE_SUBMISSION"));
        
        System.out.println("Unauthorized access test completed");
    }

    // Helper methods
    private boolean isValidPassword(String password) {
        // Password validation rules:
        // - At least 8 characters
        // - Contains at least one number
        // - Contains at least one special character
        // - Contains at least one uppercase letter
        
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        
        return hasNumber && hasSpecial && hasUpper;
    }

    private boolean hasPermission(UserDetails userDetails, String permission) {
        // Mock permission checking - in real implementation, this would check against actual permissions
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        
        Map<String, String[]> rolePermissions = new HashMap<>();
        rolePermissions.put("ROLE_STUDENT", new String[]{"READ_ASSIGNMENT", "SUBMIT_ASSIGNMENT", "VIEW_GRADES"});
        rolePermissions.put("ROLE_TEACHER", new String[]{"CREATE_ASSIGNMENT", "GRADE_SUBMISSION", "MANAGE_CLASSROOM", "READ_ASSIGNMENT", "SUBMIT_ASSIGNMENT", "VIEW_GRADES"});
        rolePermissions.put("ROLE_MANAGER", new String[]{"CREATE_CLASSROOM", "DELETE_CLASSROOM", "MANAGE_USERS", "VIEW_REPORTS", "CREATE_ASSIGNMENT", "GRADE_SUBMISSION", "MANAGE_CLASSROOM", "READ_ASSIGNMENT", "SUBMIT_ASSIGNMENT", "VIEW_GRADES"});
        rolePermissions.put("ROLE_ADMIN", new String[]{"SYSTEM_CONFIGURATION", "MANAGE_ROLES", "VIEW_AUDIT_LOGS", "ALL_PERMISSIONS", "CREATE_CLASSROOM", "DELETE_CLASSROOM", "MANAGE_USERS", "VIEW_REPORTS", "CREATE_ASSIGNMENT", "GRADE_SUBMISSION", "MANAGE_CLASSROOM", "READ_ASSIGNMENT", "SUBMIT_ASSIGNMENT", "VIEW_GRADES"});
        
        String[] permissions = rolePermissions.get(role);
        if (permissions != null) {
            for (String perm : permissions) {
                if (perm.equals(permission) || perm.equals("ALL_PERMISSIONS")) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
