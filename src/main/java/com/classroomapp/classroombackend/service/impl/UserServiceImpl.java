package com.classroomapp.classroombackend.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.Role;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender;

    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setEnabled("active".equalsIgnoreCase(user.getStatus()));
        dto.setRoles(Collections.singleton(user.getRole()));
        return dto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDto> findAllUsers(String keyword, Pageable pageable) {
        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return userPage.map(this::convertToUserDto);
    }

    @Transactional
    @Override
    public UserDto updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = "";
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails)principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }

        if (user.getEmail().equals(currentUsername)) {
            throw new BusinessLogicException("Cannot disable your own account.");
        }

        user.setStatus(enabled ? "active" : "disabled");
        User updatedUser = userRepository.save(user);
        return convertToUserDto(updatedUser);
    }

    @Override
    public List<UserDto> FindAllUsers() {
        // Convert list of User entities to UserDto objects
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto FindUserById(Long id) {
        // Find user by ID or throw exception if not found
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return this.convertToUserDto(user);
    }

    @Override
    public UserDto FindUserByUsername(String username) {
        // Find user by username or throw exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return this.convertToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Convert DTO to entity
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getName());
        user.setStatus(userDto.isEnabled() ? "active" : "disabled");
        // Set default password (should be handled with proper encoding in real app)
        user.setPassword("$2a$10$X7VYFDeMB7FB1Mh0vC99v.cAEGAVVwcVXV94R2l9fR1A8oCgJchZ6"); // Encoded '123456789'
        // Set role if provided
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            String role = userDto.getRoles().iterator().next();
            Integer roleId = convertRoleToRoleId(role);
            user.setRoleId(roleId);
        } else {
            // Default to STUDENT role
            user.setRoleId(RoleConstants.STUDENT);
        }
        
        // Set department cho Accountant
        if (user.getRoleId() != null && user.getRoleId() == RoleConstants.ACCOUNTANT) {
            user.setDepartment("Kế toán viên");
        }
        
        // Save user and return as DTO
        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        // Check if user exists
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if email is being changed and is already taken by another user
        if (!existingUser.getEmail().equals(userDto.getEmail()) && 
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Update fields
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFullName(userDto.getName());
        existingUser.setStatus(userDto.isEnabled() ? "active" : "disabled");
        // Update role if provided
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            String role = userDto.getRoles().iterator().next();
            Integer roleId = convertRoleToRoleId(role);
            existingUser.setRoleId(roleId);
        }
        
        // Set department cho Accountant khi update
        if (existingUser.getRoleId() != null && existingUser.getRoleId() == RoleConstants.ACCOUNTANT) {
            existingUser.setDepartment("Kế toán viên");
        }
        
        // Save updated user and return as DTO
        User updatedUser = userRepository.save(existingUser);
        return convertToUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Check if user exists
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        
        // Prevent self-deletion
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = "";
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails)principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }
        
        User user = userRepository.findById(id).get();
        if (user.getEmail().equals(currentUsername)) {
            throw new BusinessLogicException("Cannot delete your own account.");
        }
        
        // Delete user
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        // Check if user exists
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Reset password to default '123456789' (encoded)
        existingUser.setPassword("$2a$10$X7VYFDeMB7FB1Mh0vC99v.cAEGAVVwcVXV94R2l9fR1A8oCgJchZ6");
        
        // Save updated user
        userRepository.save(existingUser);
        // In a real application, notify user via email about the password reset
    }

    private Integer convertRoleToRoleId(String role) {
        switch (role.toUpperCase()) {
            case "STUDENT":
                return RoleConstants.STUDENT;
            case "TEACHER":
                return RoleConstants.TEACHER;
            case "MANAGER":
                return RoleConstants.MANAGER;
            case "ACCOUNTANT":
                return RoleConstants.ACCOUNTANT;
            case "ADMIN":
                return RoleConstants.ADMIN;
            default:
                return RoleConstants.STUDENT; // Default to STUDENT
        }
    }

    @Override
    public boolean IsUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean IsEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public List<UserDto> FindUsersByRole(Integer roleId) {
        // Use the repository method to find users by role ID
        List<User> users = userRepository.findByRoleId(roleId);
        return users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto updateUserRoles(Long userId, Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BusinessLogicException("Roles cannot be empty.");
        }
        // The current schema only supports one role per user.
        // We throw an error if more than one role is provided to make this limitation clear.
        if (roleNames.size() > 1) {
            throw new BusinessLogicException("System currently supports only one role per user.");
        }

        String newRoleName = roleNames.iterator().next();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role newRole = roleRepository.findByName(newRoleName)
                .orElseThrow(() -> new BusinessLogicException("Role not found: " + newRoleName));

        // Business rule: Prevent admin from removing their own admin role
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String currentUsername = ((UserDetails) principal).getUsername();
            if (user.getEmail().equals(currentUsername) &&
                user.getRoleId() == RoleConstants.ADMIN &&
                !newRole.getId().equals(RoleConstants.ADMIN)) {
                throw new BusinessLogicException("Cannot remove ADMIN role from your own account.");
            }
        }

        user.setRoleId(newRole.getId());
        User updatedUser = userRepository.save(user);
        return convertToUserDto(updatedUser);
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Password Reset");
            String content = "<p>Please click on the link below to reset your password:</p>"
                    + "<p><a href='" + resetLink + "'>Reset Password</a></p>"
                    + "<br>"
                    + "<p>Ignore this email if you did not request a password reset.</p>";
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Override
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
}