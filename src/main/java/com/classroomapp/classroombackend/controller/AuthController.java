package com.classroomapp.classroombackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.RegisterDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.util.UserMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public AuthController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Register a new user
     * @param registerDto registration information
     * @return created user
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> RegisterUser(@Valid @RequestBody RegisterDto registerDto) {
        // Check if username already exists
        if (userService.IsUsernameExists(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        
        // Check if email already exists
        if (userService.IsEmailExists(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());
        
        // Set default role if not provided
        user.setRole(registerDto.getRole() != null ? registerDto.getRole() : "STUDENT");
        
        User savedUser = userRepository.save(user);
        
        return new ResponseEntity<>(UserMapper.toDto(savedUser), HttpStatus.CREATED);
    }
    
    // Note: Login functionality would be implemented here
    // In a production application, this would typically use JWT or other token-based authentication
} 