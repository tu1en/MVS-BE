package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserMapper;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class UserController {
    
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current user's profile.
     * This is the single authoritative endpoint for fetching the logged-in user's profile.
     * @return UserDto of the current user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // Log principal type for diagnostic purposes
            logger.info("Principal class: " + (principal != null ? principal.getClass().getName() : "null"));
            
            if (principal instanceof CustomUserDetails) {
                // Use our custom UserDetails which has direct access to the User entity
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                User user = userDetails.getUser();
                logger.info("Fetching profile for current user (CustomUserDetails): " + user.getEmail());
                UserDto userDto = UserMapper.toDto(user);
                return ResponseEntity.ok(userDto);
            } else if (principal instanceof UserDetails) {
                // Handle standard UserDetails - the username is actually the email in our system
                String email = ((UserDetails) principal).getUsername();
                logger.info("Fetching profile for current user (UserDetails): " + email);
                
                // Find user by email
                User user = userService.findUserEntityByEmail(email);
                if (user == null) {
                    logger.severe("User not found with email: " + email);
                    return ResponseEntity.status(404).build();
                }
                
                UserDto userDto = UserMapper.toDto(user);
                return ResponseEntity.ok(userDto);
            } else {
                // Fallback to string representation
                String username = principal.toString();
                logger.info("Fetching profile for current user (fallback): " + username);
                
                // Try to find by email first
                User user = userService.findUserEntityByEmail(username);
                if (user != null) {
                    logger.info("Found user by email: " + username);
                    UserDto userDto = UserMapper.toDto(user);
                    return ResponseEntity.ok(userDto);
                }
                
                // Fall back to username search
                logger.info("Attempting to find by username: " + username);
                UserDto userDto = userService.FindUserByUsername(username);
                return ResponseEntity.ok(userDto);
            }
        } catch (Exception e) {
            logger.severe("Error retrieving current user profile: " + e.getMessage());
            e.printStackTrace(); // Log stack trace for better debugging
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get all users
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers() {
        // This endpoint should be replaced by the one in AdminController
        // Returning what the old service method returns to maintain some functionality.
        return ResponseEntity.ok(userService.FindAllUsers());
    }

    /**
     * Get user by ID
     * @param id user ID
     * @return user with specified ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        // This endpoint should be replaced by AdminController or a /me endpoint
        return ResponseEntity.ok(userService.FindUserById(id));
    }

    /**
     * Get users by role ID
     * @param roleId role ID (1=STUDENT, 2=TEACHER, 3=MANAGER, 0=ADMIN)
     * @return list of users with specified role
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable Long roleId) {
        // This method is likely broken due to Role entity changes.
        // Returning what the old service method returns to maintain some functionality.
        return ResponseEntity.ok(userService.FindUsersByRole(roleId.intValue()));
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<UserDto>> getTeachers() {
        // Role 2 corresponds to TEACHER
        return ResponseEntity.ok(userService.FindUsersByRole(2));
    }

    private String getRoleName(Long roleId) {
        switch (roleId.intValue()) {
            case 0: return "ADMIN";
            case 1: return "STUDENT";
            case 2: return "TEACHER";
            case 3: return "MANAGER";
            default: return "UNKNOWN";
        }
    }

    /**
     * Delete user
     * @param id user ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.DeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam String keyword) {
        // This should use the new service method with pagination
        // For now, returning empty list.
        return new ArrayList<>();
    }
}