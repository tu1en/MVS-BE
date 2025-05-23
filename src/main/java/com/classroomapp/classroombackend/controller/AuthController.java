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

// Thêm các import sau vào đầu file
import java.util.HashMap;
import java.util.Map;
// Thêm import
import com.classroomapp.classroombackend.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    // Thêm vào constructor
    private final JwtUtil jwtUtil;
    
    public AuthController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        Map<String, String> response = new HashMap<>();
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        response.put("role", user.getRole());
        response.put("token", token);
        response.put("role", user.getRole());
        
        return ResponseEntity.ok(response);
    }
}