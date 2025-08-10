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
    
    @Override
    @Transactional
    public boolean createUserWithActiveStatus(String email, String fullName, String role) {
        try {
            log.info("Creating user with active status - email: {}, name: {}, role: {}", email, fullName, role);
            Optional<User> optionalUser = userRepository.findByEmail(email);
            
            if (optionalUser.isPresent()) {
                // User exists, update role và set active status
                User user = optionalUser.get();
                user.setRoleId(role != null ? convertRoleToRoleId(role) : convertRoleToRoleId("STUDENT"));
                user.setStatus("active");
                userRepository.save(user);
                log.info("Updated existing user {} with role {} and active status", email, role);
                return true;
            } else {
                // Create new user với trạng thái active
                String tempPassword = UUID.randomUUID().toString().substring(0, 8);
                User user = new User();
                user.setEmail(email);
                user.setUsername(email);
                user.setPassword(passwordEncoder.encode(tempPassword));
                user.setFullName(fullName);
                user.setRoleId(role != null ? convertRoleToRoleId(role) : convertRoleToRoleId("STUDENT"));
                user.setStatus("active");
                
                User savedUser = userRepository.save(user);
                log.info("Created new user: {} with ID: {}, role: {}, and active status", email, savedUser.getId(), role);
                
                // Send email with account information
                try {
                    emailService.sendAccountInfoEmail(
                        email, 
                        fullName, 
                        getReadableRole(role != null ? role : "STUDENT"),
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
            log.error("Error creating user with active status: {}", email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean createUserWithoutContract(String email, String fullName, String role) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                // User exists, update role và trạng thái
                User user = optionalUser.get();
                user.setRoleId(role != null ? convertRoleToRoleId(role) : null);
                user.setStatus("pending_contract");
                userRepository.save(user);
                return true;
            } else {
                // Create new user với trạng thái chưa có hợp đồng
                String tempPassword = UUID.randomUUID().toString().substring(0, 8);
                User user = new User();
                user.setEmail(email);
                user.setUsername(email);
                user.setPassword(passwordEncoder.encode(tempPassword));
                user.setFullName(fullName);
                user.setRoleId(role != null ? convertRoleToRoleId(role) : null);
                user.setStatus("pending_contract");
                userRepository.save(user);
                // Không gửi mail tài khoản ở đây (sẽ gửi khi ký hợp đồng)
                return true;
            }
        } catch (Exception e) {
            log.error("Error creating user without contract: {}", email, e);
            return false;
        }
    } 
    
    @Override
    public boolean hasActiveContract(String email) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // Kiểm tra trạng thái user
                // Nếu status là "active" thì đã có hợp đồng
                // Nếu status là "pending_contract" thì chưa có hợp đồng
                return "active".equals(user.getStatus());
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking contract status for user: {}", email, e);
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
            case "ACCOUNTANT": return "Accountant";
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
            case "ACCOUNTANT":
                return 5; // New role for accountant
            default:
                return 1; // Default to STUDENT
        }
    }
} 