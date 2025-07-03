package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import com.classroomapp.classroombackend.constants.RoleConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.util.UserMapper;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public List<UserDto> FindAllUsers() {
        // Convert list of User entities to UserDto objects
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto FindUserById(Long id) {
        // Find user by ID or throw exception if not found
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto FindUserByUsername(String username) {
        // Find user by username or throw exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto CreateUser(UserDto userDto) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        
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
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto UpdateUser(Long id, UserDto userDto) {
        // Check if user exists
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if username is being changed and is already taken by another user
        if (!existingUser.getUsername().equals(userDto.getUsername()) && 
                userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        
        // Check if email is being changed and is already taken by another user
        if (!existingUser.getEmail().equals(userDto.getEmail()) && 
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Update fields
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFullName(userDto.getFullName());
        existingUser.setRoleId(userDto.getRoleId());
        
        // Note: Password should be updated in a separate endpoint with proper validation
        
        // Save updated user and return as DTO
        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toDto(updatedUser);
    }

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
        List<User> users = userRepository.findByRoleId(roleId);
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
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
    public UserDto updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Sử dụng RoleConstants để đảm bảo nhất quán
        int roleId;
        switch (newRole.toUpperCase()) {
            case "STUDENT": 
                roleId = RoleConstants.STUDENT; 
                break;
            case "TEACHER": 
                roleId = RoleConstants.TEACHER; 
                break;
            case "MANAGER": 
                roleId = RoleConstants.MANAGER; 
                break;
            case "ACCOUNTANT": 
                roleId = RoleConstants.ACCOUNTANT; 
                break;
            case "ADMIN":
                roleId = RoleConstants.ADMIN;
                break;
            default: 
                throw new IllegalArgumentException("Vai trò không hợp lệ. Vui lòng chọn một trong các vai trò: STUDENT, TEACHER, MANAGER, ACCOUNTANT, ADMIN");
        }
        
        user.setRoleId(roleId);
        User updated = userRepository.save(user);
        return UserMapper.toDto(updated);
    }

    @Override
    public String resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Sinh mật khẩu mới
        String newPassword = generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    // Hàm sinh mật khẩu ngẫu nhiên (private helper)
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Nếu User chưa có trường isLocked, dùng status=0 là locked
        user.setStatus("0"); // 0 = locked, 1 = active
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setStatus("1"); // 1 = active
        userRepository.save(user);
    }
}