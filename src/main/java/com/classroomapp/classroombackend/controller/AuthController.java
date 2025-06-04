package com.classroomapp.classroombackend.controller;

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
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.service.AuthService;

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
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUsername());
        LoginResponseDto response = authService.authenticateUser(loginRequest);
        log.info("Login successful for username: {}", loginRequest.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset
     * 
     * @param passwordResetRequest password reset information
     * @return success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDto passwordResetRequest) {
        log.info("Password reset requested for email: {}", passwordResetRequest.getEmail());
        authService.requestPasswordReset(passwordResetRequest);
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
    public ResponseEntity<LoginResponseDto> googleLogin(@Valid @RequestBody GoogleAuthRequestDto googleAuthRequest) {
        log.info("Google login request received");
        LoginResponseDto response = authService.authenticateWithGoogle(googleAuthRequest);
        log.info("Google login successful");
        return ResponseEntity.ok(response);
    }
}