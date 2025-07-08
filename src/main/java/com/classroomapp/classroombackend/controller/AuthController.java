package com.classroomapp.classroombackend.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.requestmanagement.CreateRequestDto;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.AuthService;
import com.classroomapp.classroombackend.service.RequestService;
import com.classroomapp.classroombackend.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for authentication-related endpoints
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RequestService requestService; // Inject RequestService
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthService authService, UserRepository userRepository, 
                         UserService userService, RequestService requestService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.requestService = requestService; // Initialize RequestService
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Submits a registration request for a new user
     * 
     * @param createRequestDto registration request information
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<?> submitRegistrationRequest(@Valid @RequestBody CreateRequestDto createRequestDto) {
        requestService.createRegistrationRequest(createRequestDto);
        return ResponseEntity.ok(Map.of("message", "Request submitted successfully. Please wait for approval."));
    }

    /**
     * Authenticate user with username and password
     * 
     * @param loginRequest login information
     * @return login response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            System.out.println("Login attempt for user: " + username);
            System.out.println("Password provided: " + password);

            // Try to find user by username first, then by email
            User user = userRepository.findByUsername(username)
                    .orElse(userRepository.findByEmail(username).orElse(null));
            
            if (user == null) {
                System.out.println("User not found: " + username);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            System.out.println("User found: " + user.getUsername() + " (email: " + user.getEmail() + ")");
            System.out.println("Stored password hash: " + user.getPassword());
            System.out.println("Password encoder matches: " + passwordEncoder.matches(password, user.getPassword()));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("Invalid password for user: " + username);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            Map<String, String> response = new HashMap<>();

            // Chuyển đổi roleId thành tên vai trò để thêm vào token
            String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
            System.out.println("Login successful for user: " + username + " with role: " + roleName + " (roleId: " + user.getRoleId() + ")");
            
            // FIX: Use email as the subject for consistency
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", user.getEmail());
            claims.put("email", user.getEmail());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRoleId());
            claims.put("roles", new String[]{roleName});
            
            // Generate JWT token mới với claims đầy đủ
            String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // FIX: Subject must be the email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 giờ
                .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
                .compact();
            
            System.out.println("Generated new token for user: " + username);
            
            response.put("role", roleName);
            response.put("roleId", user.getRoleId().toString());
            response.put("token", token);
            response.put("userId", user.getId().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }    /**
     * Request password reset
     * 
     * @param request password reset information
     * @return success message
     */
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
            .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
            .compact();
            
        userService.sendPasswordResetEmail(user.getEmail(), resetToken);

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
    }    /**
     * Authenticate with Google ID token
     * 
     * @param credentials Google authentication information
     * @return login response with JWT token
     */
    @PostMapping("/google-login")
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
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
        // Lấy thông tin người dùng đã xác thực từ Security Context
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        
        if (username == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
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