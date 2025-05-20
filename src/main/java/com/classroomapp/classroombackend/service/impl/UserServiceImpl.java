package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.util.UserMapper;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        existingUser.setRole(userDto.getRole());
        
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
} 