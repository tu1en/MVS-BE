package com.classroomapp.classroombackend.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    /*
    // TODO: Refactor CreateUser with a new CreateUserRequestDto
    @Override
    public UserDto CreateUser(UserDto userDto) {
        // Check if username or email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Convert DTO to entity
        User user = UserMapper.toEntity(userDto);
        
        // Encode password before saving
        // Note: In a real application, you would receive the password in a separate DTO
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Save user and return as DTO
        User savedUser = userRepository.save(user);
        return this.convertToUserDto(savedUser);
    }
    */

    /*
    // TODO: Refactor UpdateUser with a new UpdateUserRequestDto
    @Override
    public UserDto UpdateUser(Long id, UserDto userDto) {
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
        
        // Note: Password should be updated in a separate endpoint with proper validation
        
        // Save updated user and return as DTO
        User updatedUser = userRepository.save(existingUser);
        return this.convertToUserDto(updatedUser);
    }
    */

    @Override
    public void DeleteUser(Long id) {
        // Check if user exists
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        
        // Delete user
        userRepository.deleteById(id);
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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" +
            "http://localhost:3000/reset-password?token=" + resetLink);
        mailSender.send(message);
    }
    
    @Override
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
}