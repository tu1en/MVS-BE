package com.classroomapp.classroombackend.controller;

import java.util.Date;
// Thêm các import sau vào đầu file
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.GoogleAuthRequestDto;
import com.classroomapp.classroombackend.dto.LoginRequestDto;
import com.classroomapp.classroombackend.dto.LoginResponseDto;
import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.PasswordResetRequestDto;
import com.classroomapp.classroombackend.dto.RegisterDto;
<<<<<<< HEAD
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.service.AuthService;

import jakarta.validation.Valid;
=======
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;
// Thêm import
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.util.UserMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
>>>>>>> master
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for authentication-related endpoints
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }    /**
     * Register a new user
     * 
     * @param registerDto registration information
     * @return created user
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        log.info("Registration request received for username: {}", registerDto.getUsername());
        UserDto registeredUser = authService.registerUser(registerDto);
        log.info("Registration successful for username: {}", registerDto.getUsername());
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Authenticate user with username and password
     * 
     * @param loginRequest login information
     * @return login response with JWT token
     */
    @PostMapping("/login")
<<<<<<< HEAD
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUsername());
        LoginResponseDto response = authService.authenticateUser(loginRequest);
        log.info("Login successful for username: {}", loginRequest.getUsername());
=======
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
            .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
            .compact();
              System.out.println("Generated new token for user: " + username);
        
        response.put("role", roleName);
        response.put("roleId", user.getRoleId().toString());
        response.put("token", token);
        response.put("userId", user.getId().toString());
    
>>>>>>> master
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset
     * 
     * @param passwordResetRequest password reset information
     * @return success message
     */
    @PostMapping("/forgot-password")
<<<<<<< HEAD
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDto passwordResetRequest) {
        log.info("Password reset requested for email: {}", passwordResetRequest.getEmail());
        authService.requestPasswordReset(passwordResetRequest);
=======
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
            .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
            .compact();
            
        userService.sendPasswordResetEmail(user.getEmail(), resetToken);
    
>>>>>>> master
        return ResponseEntity.ok("Password reset email sent successfully.");
    }

    /**
     * Reset password with token
     * 
     * @param passwordConfirmation password confirmation information
     * @return success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordConfirmationDto passwordConfirmation) {
        log.info("Password reset confirmation received");
        authService.resetPassword(passwordConfirmation);
        return ResponseEntity.ok("Password reset successfully.");
    }

    /**
     * Authenticate with Google ID token
     * 
     * @param googleAuthRequest Google authentication information
     * @return login response with JWT token
     */
    @PostMapping("/google-login")
<<<<<<< HEAD
    public ResponseEntity<LoginResponseDto> googleLogin(@Valid @RequestBody GoogleAuthRequestDto googleAuthRequest) {
        log.info("Google login request received");
        LoginResponseDto response = authService.authenticateWithGoogle(googleAuthRequest);
        log.info("Google login successful");
=======
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> credentials) {
        String idToken = credentials.get("idToken");
        
        // Verify Google ID token
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            log.error("Invalid Google ID token", e);
            throw new IllegalArgumentException("Token Google không hợp lệ", e);
        }
        
        String email = decodedToken.getEmail();
        log.info("Google login attempt with email: {}", email);
        
        // Check if user exists
        Map<String, Object> response = new HashMap<>();
        
        // Kiểm tra tài khoản tồn tại thay vì tự động tạo mới
        boolean userExists = userRepository.findByEmail(email).isPresent();
        if (!userExists) {
            log.warn("Google login failed: No account found for email: {}", email);
            response.put("success", false);
            response.put("message", "Tài khoản này chưa được đăng ký trong hệ thống");
            response.put("email", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        User user = userRepository.findByEmail(email).get();
        log.info("User found with email {}, role: {}", email, user.getRoleId());
        
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
            .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
            .compact();
        
        response.put("success", true);
        response.put("message", "Đăng nhập thành công");
        response.put("role", roleName);
        response.put("roleId", user.getRoleId().toString());
        response.put("token", token);
        response.put("userId", user.getId().toString());
        
>>>>>>> master
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request, @RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        // Basic validation
        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu cũ và mật khẩu mới không được để trống");
        }
        
        if (oldPassword.equals(newPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu mới không được trùng với mật khẩu cũ");
        }
        
        if (newPassword.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu mới không được vượt quá 50 ký tự");
        }
        
        // Get username from token
        String username = jwtUtil.getUsernameFromToken(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu cũ không chính xác");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}