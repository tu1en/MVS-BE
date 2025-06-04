package com.classroomapp.classroombackend.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.UserServiceExtension;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceExtensionImpl implements UserServiceExtension {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public boolean updateUserRole(String email, String role) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRoleId(convertRoleToRoleId(role));
            userRepository.save(user);
            log.info("Updated role for user: {} to {}", email, role);
            return true;
        }
        return false;
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public boolean createOrUpdateUser(String email, String fullName, String role) {
        try {
            log.info("Creating or updating user with email: {}, name: {}, role: {}", email, fullName, role);
            Optional<User> optionalUser = userRepository.findByEmail(email);
            
            if (optionalUser.isPresent()) {
                // User exists, update role
                User user = optionalUser.get();
                Integer roleId = convertRoleToRoleId(role);
                log.info("User exists - current role: {}, new role: {}", user.getRoleId(), roleId);
                user.setRoleId(roleId);
                userRepository.save(user);
                log.info("Updated role for existing user: {} to {}", email, role);
                return true;
            } else {
                // Create new user with temporary password
                log.info("User does not exist, creating new user");
                String tempPassword = UUID.randomUUID().toString().substring(0, 8);
                User user = new User();
                user.setEmail(email);
                user.setUsername(email); // Use email as username
                user.setPassword(passwordEncoder.encode(tempPassword));
                user.setFullName(fullName);
                user.setRoleId(convertRoleToRoleId(role));
                user.setStatus("active");
                
                User savedUser = userRepository.save(user);
                log.info("Created new user: {} with ID: {} and role {}", email, savedUser.getId(), role);
                
                // Send email with temporary password
                try {
                    emailService.sendAccountInfoEmail(
                        email, 
                        fullName, 
                        getReadableRole(role),
                        email, // username
                        tempPassword
                    );
                    log.info("Sent account information email to new user: {}", email);
                } catch (Exception e) {
                    log.error("Failed to send account information email to: {}", email, e);
                    // Don't fail user creation if email sending fails
                }
                
                return true;
            }
        } catch (Exception e) {
            log.error("Error creating/updating user: {}", email, e);
            return false;
        }
    }
    
    /**
     * Returns a human-readable role name
     */
    private String getReadableRole(String role) {
        switch (role) {
            case "STUDENT": return "Student";
            case "TEACHER": return "Teacher";
            case "MANAGER": return "Manager";
            case "ADMIN": return "Administrator";
            default: return role;
        }
    }
    
    // Helper method to convert role string to roleId
    private Integer convertRoleToRoleId(String role) {
        switch (role) {
            case "STUDENT":
                return 1;
            case "TEACHER":
                return 2;
            case "MANAGER":
                return 3;
            case "ADMIN":
                return 4;
            default:
                return 1; // Default to STUDENT
        }
    }
} 