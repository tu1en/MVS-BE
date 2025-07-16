package com.classroomapp.classroombackend.controller;

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

            // Chuyá»ƒn Ä‘á»•i roleId thÃ nh tÃªn vai trÃ² Ä‘á»ƒ thÃªm vÃ o token
            String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
            System.out.println("Login successful for user: " + username + " with role: " + roleName + " (roleId: " + user.getRoleId() + ")");
            
            // FIX: Use email as the subject for consistency
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", user.getEmail());
            claims.put("email", user.getEmail());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRoleId());
            claims.put("roles", new String[]{roleName});
            
            // Generate JWT token má»›i vá»›i claims Ä‘áº§y Ä‘á»§
            String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // CONSISTENT: Subject is always email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 giá»
                .signWith(jwtUtil.getSecretKeyFromString(), SignatureAlgorithm.HS512)
                .compact();

            log.info("AuthController - Generated JWT token for user: {} with role: {}", user.getEmail(), user.getRoleId());
            
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
            throw new IllegalArgumentException("Token Google khÃ´ng há»£p lá»‡", e);
        }
        
        String email = decodedToken.getEmail();
        log.info("Google login attempt with email: {}", email);
        
        // Check if user exists
        Map<String, Object> response = new HashMap<>();
        
        // Kiá»ƒm tra tÃ i khoáº£n tá»“n táº¡i thay vÃ¬ tá»± Ä‘á»™ng táº¡o má»›i
        boolean userExists = userRepository.findByEmail(email).isPresent();
        if (!userExists) {
            log.warn("Google login failed: No account found for email: {}", email);
            response.put("success", false);
            response.put("message", "TÃ i khoáº£n nÃ y chÆ°a Ä‘Æ°á»£c Ä‘Äƒng kÃ½ trong há»‡ thá»‘ng");
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
        response.put("message", "ÄÄƒng nháº­p thÃ nh cÃ´ng");
        response.put("role", roleName);
        response.put("roleId", user.getRoleId().toString());
        response.put("token", token);
        response.put("userId", user.getId().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Validate JWT token endpoint
     * This endpoint is used by the frontend to validate if the current token is still valid
     *
     * @return validation response
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken() {
        log.info("Token validation endpoint called");
        try {
            // If we reach this point, the JWT filter has already validated the token
            // and set up the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Authentication object: {}", authentication != null ? authentication.getName() : "null");

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token is not valid"));
            }

            // Get user details from the authentication
            Object principal = authentication.getPrincipal();
            String username = null;

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            log.info("Token validation successful for user: {}", username);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Token is valid");
            response.put("username", username);
            response.put("authorities", authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toArray());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "message", "Token validation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
        // Láº¥y thÃ´ng tin ngÆ°á»i dÃ¹ng Ä‘Ã£ xÃ¡c thá»±c tá»« Security Context
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Máº­t kháº©u cÅ© vÃ  máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        
        if (oldPassword.equals(newPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c trÃ¹ng vá»›i máº­t kháº©u cÅ©");
        }
        
        if (newPassword.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 50 kÃ½ tá»±");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Máº­t kháº©u cÅ© khÃ´ng chÃ­nh xÃ¡c");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok("Äá»•i máº­t kháº©u thÃ nh cÃ´ng");
    }
}