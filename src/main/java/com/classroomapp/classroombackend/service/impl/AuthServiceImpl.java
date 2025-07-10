package com.classroomapp.classroombackend.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.GoogleAuthRequestDto;
import com.classroomapp.classroombackend.dto.LoginRequestDto;
import com.classroomapp.classroombackend.dto.LoginResponseDto;
import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.PasswordResetRequestDto;
import com.classroomapp.classroombackend.dto.RegisterDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.AuthService;
import com.classroomapp.classroombackend.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AuthService
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
    }

    @Override
    public LoginResponseDto authenticateUser(LoginRequestDto loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());
        
        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get user details
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Generate token - FIX: Use email as the primary subject for the token
            String token = generateToken(user.getEmail(), user.getRoleId(), user.getEmail());
            
            // Get role name
            String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
            
            log.info("User authenticated successfully: {}", user.getUsername());
            
            // Build response
            return LoginResponseDto.builder()
                    .token(token)
                    .role(roleName)
                    .roleId(user.getRoleId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .userId(user.getId())
                    .build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    @Override
    public UserDto registerUser(RegisterDto registerDto) {
        log.info("Registering new user: {}", registerDto.getUsername());
        
        // Check if username already exists
        if (userService.IsUsernameExists(registerDto.getUsername())) {
            log.warn("Registration failed: Username {} already exists", registerDto.getUsername());
            throw new IllegalArgumentException("Username already taken");
        }

        // Check if email already exists
        if (userService.IsEmailExists(registerDto.getEmail())) {
            log.warn("Registration failed: Email {} already registered", registerDto.getEmail());
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
        log.info("User registered successfully: {}", savedUser.getUsername());

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public LoginResponseDto authenticateWithGoogle(GoogleAuthRequestDto googleAuthRequest) {
        log.info("Authenticating with Google ID token");
        
        try {
            // Verify Google ID token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(googleAuthRequest.getIdToken());
            String email = decodedToken.getEmail();
            
            log.info("Google token verified for email: {}", email);
            
            // Check if user exists
            User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create new user if not exists
                    log.info("Creating new user for Google account: {}", email);
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email); // Use email as username for Google users
                    newUser.setFullName(decodedToken.getName());
                    newUser.setPassword(passwordEncoder.encode(generateRandomPassword()));
                    newUser.setRoleId(1); // Default role - STUDENT
                    return userRepository.save(newUser);
                });
            
            // Generate JWT
            String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
            String token = generateToken(user.getEmail(), user.getRoleId(), user.getEmail());
            
            log.info("User authenticated via Google successfully: {}", email);
            
            return LoginResponseDto.builder()
                    .token(token)
                    .role(roleName)
                    .roleId(user.getRoleId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .userId(user.getId())
                    .build();
        } catch (FirebaseAuthException e) {
            log.error("Google authentication failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Google ID token", e);
        }
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDto passwordResetRequest) {
        log.info("Password reset requested for email: {}", passwordResetRequest.getEmail());
        
        User user = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate a password reset token
        String resetToken = generateToken(user.getUsername(), user.getRoleId(), user.getEmail());
        
        userService.sendPasswordResetEmail(user.getEmail(), resetToken);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Override
    public void resetPassword(PasswordConfirmationDto passwordConfirmation) {
        log.info("Processing password reset with token");
        
        // Validate token
        if (!jwtUtil.validateToken(passwordConfirmation.getToken())) {
            log.warn("Invalid or expired password reset token");
            throw new IllegalArgumentException("Invalid or expired token");
        }

        // Get username from token
        String username = jwtUtil.getSubjectFromToken(passwordConfirmation.getToken());

        // Update password
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(passwordConfirmation.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successful for user: {}", username);
    }

    @Override
    public String generateToken(String subject, Integer roleId, String email) {
        log.debug("Generating JWT token for user: {}", subject);
        
        String roleName = jwtUtil.convertRoleIdToName(roleId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);  // Subject can be email or username
        claims.put("email", email);
        claims.put("role", roleId); // Keep roleId for potential fine-grained checks
        claims.put("roles", new String[]{roleName}); // Keep roles for standard Spring Security
        
        // Generate JWT token with claims
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(subject) // The principal identifier
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
            .signWith(jwtUtil.getSecretKeyFromString())
            .compact();
        
        log.debug("JWT token generated successfully");
        return token;
    }
    
    /**
     * Generate random password for Google users
     * 
     * @return random password
     */
    private String generateRandomPassword() {
        return "GoogleAuth-" + System.currentTimeMillis();
    }
}
