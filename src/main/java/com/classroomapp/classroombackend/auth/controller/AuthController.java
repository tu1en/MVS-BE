package com.classroomapp.classroombackend.auth.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.auth.service.AuthService;
import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.requestmanagement.CreateRequestDto;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.RequestService;
import com.classroomapp.classroombackend.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xử lý các API liên quan đến Authentication
 */
@RestController("authController")
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RequestService requestService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthService authService, UserRepository userRepository,
                          UserService userService, RequestService requestService,
                          JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.requestService = requestService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gửi yêu cầu đăng ký tài khoản
     */
    @PostMapping("/register")
    public ResponseEntity<?> submitRegistrationRequest(@Valid @RequestBody CreateRequestDto createRequestDto) {
        requestService.createRegistrationRequest(createRequestDto);
        return ResponseEntity.ok(Map.of("message", "Gửi yêu cầu đăng ký thành công. Vui lòng chờ duyệt."));
    }

    /**
     * Đăng nhập với username hoặc email + password
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            log.info("Login attempt for user: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElse(userRepository.findByEmail(username).orElse(null));

            if (user == null) {
                log.warn("Login failed: User not found - {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không tìm thấy người dùng / User not found"));
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Login failed: Invalid password for user - {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Sai mật khẩu / Invalid password"));
            }

            String roleName = jwtUtil.convertUserRoleToName(user.getRoleEnum());
            log.info("User {} authenticated successfully with role {}", username, roleName);

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", user.getEmail());
            claims.put("email", user.getEmail());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRoleEnum().toRoleId());
            claims.put("roles", new String[]{roleName});

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
                    .compact();

            log.info("JWT token generated for user: {}", username);

            Map<String, String> response = new HashMap<>();
            response.put("role", roleName);
            response.put("roleId", String.valueOf(user.getRoleEnum().toRoleId()));
            response.put("token", token);
            response.put("userId", String.valueOf(user.getId()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi đăng nhập: " + e.getMessage()));
        }
    }

    /**
     * Gửi email reset mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng / User not found"));

        String roleName = jwtUtil.convertUserRoleToName(user.getRoleEnum());
        log.info("Generating reset token for user: {}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRoleEnum().toRoleId());
        claims.put("roles", new String[]{roleName});

        String resetToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
                .compact();

        userService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return ResponseEntity.ok("Đã gửi email đặt lại mật khẩu.");
    }

    /**
     * Reset mật khẩu
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordConfirmationDto passwordConfirmation) {
        authService.resetPassword(passwordConfirmation);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công.");
    }

    /**
     * Đăng nhập bằng Google
     */
    @PostMapping("/google-login")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> credentials) {
        String idToken = credentials.get("idToken");
        log.info("Google login attempt received");

        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            log.info("Google token verified successfully");
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            log.error("Google token verification failed", e);
            throw new IllegalArgumentException("Token Google không hợp lệ / Invalid Google token", e);
        }

        String email = decodedToken.getEmail();
        log.info("Google login attempt for email: {}", email);

        Map<String, Object> response = new HashMap<>();
        boolean userExists = userRepository.findByEmail(email).isPresent();
        if (!userExists) {
            log.warn("Google login failed: No account found for email: {}", email);
            response.put("success", false);
            response.put("message", "Tài khoản này chưa được đăng ký trong hệ thống / Account not registered in the system");
            response.put("email", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = userRepository.findByEmail(email).get();
        String roleName = jwtUtil.convertUserRoleToName(user.getRoleEnum());
        log.info("User found with email {}, role: {}", email, roleName);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getEmail());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRoleEnum().toRoleId());
        claims.put("roles", new String[]{roleName});

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
                .compact();

        log.info("JWT token generated for Google login user: {}", email);

        response.put("success", true);
        response.put("message", "Đăng nhập thành công / Login successful");
        response.put("role", roleName);
        response.put("roleId", String.valueOf(user.getRoleEnum().toRoleId()));
        response.put("token", token);
        response.put("userId", String.valueOf(user.getId()));

        return ResponseEntity.ok(response);
    }

    /**
     * Validate token
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken() {
        log.info("Token validation request received");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Token validation failed: No valid authentication found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token không hợp lệ / Invalid token"));
        }

        String username = authentication.getName();
        log.info("Token validation successful for user: {}", username);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("message", "Token hợp lệ / Token is valid");
        response.put("username", username);
        response.put("authorities", authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toArray());

        return ResponseEntity.ok(response);
    }

    /**
     * Đổi mật khẩu
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
        log.info("Password change request received");
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof org.springframework.security.core.userdetails.UserDetails)
                ? ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername()
                : principal.toString();

        if (username == null) {
            log.warn("Password change failed: User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Người dùng chưa xác thực / User not authenticated");
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            log.warn("Password change failed: Empty passwords provided");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Mật khẩu cũ và mật khẩu mới không được để trống / Old and new passwords cannot be empty");
        }

        if (oldPassword.equals(newPassword)) {
            log.warn("Password change failed: New password same as old password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Mật khẩu mới không được trùng mật khẩu cũ / New password must be different from old password");
        }

        if (newPassword.length() > 50) {
            log.warn("Password change failed: New password too long");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Mật khẩu mới không được vượt quá 50 ký tự / New password cannot exceed 50 characters");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng / User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed: Incorrect old password for user: {}", username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Mật khẩu cũ không chính xác / Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);

        return ResponseEntity.ok("Đổi mật khẩu thành công / Password changed successfully");
    }
}
