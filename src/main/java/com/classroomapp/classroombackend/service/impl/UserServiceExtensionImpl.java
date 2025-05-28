package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.UserServiceExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceExtensionImpl implements UserServiceExtension {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
            Optional<User> optionalUser = userRepository.findByEmail(email);
            
            if (optionalUser.isPresent()) {
                // User exists, update role
                User user = optionalUser.get();
                user.setRoleId(convertRoleToRoleId(role));
                userRepository.save(user);
                log.info("Updated role for existing user: {} to {}", email, role);
                return true;
            } else {
                // Create new user with temporary password
                String tempPassword = UUID.randomUUID().toString().substring(0, 8);
                User user = new User();
                user.setEmail(email);
                user.setUsername(email); // Use email as username
                user.setPassword(passwordEncoder.encode(tempPassword));
                user.setFullName(fullName);
                user.setRoleId(convertRoleToRoleId(role));
                user.setStatus("active");
                
                userRepository.save(user);
                log.info("Created new user: {} with role {}", email, role);
                
                // TODO: Send email with temporary password
                return true;
            }
        } catch (Exception e) {
            log.error("Error creating/updating user: {}", email, e);
            return false;
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