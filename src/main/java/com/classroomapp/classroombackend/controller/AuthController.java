package com.classroomapp.classroombackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.RegisterDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.util.UserMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

// Thêm các import sau vào đầu file
import java.util.HashMap;
import java.util.Map;
// Thêm import
import com.classroomapp.classroombackend.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    // Thêm vào constructor
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user
     * 
     * @param registerDto registration information
     * @return created user
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> RegisterUser(@RequestBody RegisterDto registerDto) {
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
        user.setRoleId(registerDto.getRoleId() != null ? registerDto.getRoleId() : 1);

        User savedUser = userRepository.save(user);

        return new ResponseEntity<>(UserMapper.toDto(savedUser), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
    
        System.out.println("Login attempt for user: " + username);
    
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
    
        Map<String, String> response = new HashMap<>();
    
        // Chuyển đổi roleId thành tên vai trò để thêm vào token
        String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
        System.out.println("Login successful for user: " + username + " with role: " + roleName + " (roleId: " + user.getRoleId() + ")");
        
        // Thêm cả email và username vào claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());  // Subject là username
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRoleId());
        claims.put("roles", new String[]{roleName});
        
        // Generate JWT token mới với claims đầy đủ
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 giờ
            .signWith(SignatureAlgorithm.HS512, jwtUtil.getSecretKey())
            .compact();
            
        System.out.println("Generated new token for user: " + username);
        
        response.put("role", roleName);
        response.put("roleId", user.getRoleId().toString());
        response.put("token", token);
    
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
    
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
        // Generate a password reset token with claims
        String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRoleId());
        claims.put("roles", new String[]{roleName});
        
        String resetToken = Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
            .signWith(SignatureAlgorithm.HS512, jwtUtil.getSecretKey())
            .compact();
            
        userService.sendPasswordResetEmail(user.getEmail(), resetToken);
    
        return ResponseEntity.ok("Password reset email sent successfully.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
    
        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
    
        // Get username from token
        String username = jwtUtil.getUsernameFromToken(token);
    
        // Update password
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    
        return ResponseEntity.ok("Password reset successfully.");
    }

    @PostMapping("/google-login")
    public ResponseEntity<Map<String, String>> googleLogin(@RequestBody Map<String, String> credentials) {
        String idToken = credentials.get("idToken");
        
        // Verify Google ID token
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            throw new IllegalArgumentException("Invalid Google ID token", e);
        }
        String email = decodedToken.getEmail();
        
        // Check if user exists
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                // Create new user if not exists
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(decodedToken.getName());
                newUser.setRoleId(1); // Default role
                return userRepository.save(newUser);
            });
        
        // Generate JWT
        String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getEmail());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRoleId());
        claims.put("roles", new String[]{roleName});
        
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getEmail())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
            .signWith(SignatureAlgorithm.HS512, jwtUtil.getSecretKey())
            .compact();
        
        Map<String, String> response = new HashMap<>();
        response.put("role", roleName);
        response.put("roleId", user.getRoleId().toString());
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }
}