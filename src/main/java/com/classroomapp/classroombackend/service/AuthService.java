package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.GoogleAuthRequestDto;
import com.classroomapp.classroombackend.dto.LoginRequestDto;
import com.classroomapp.classroombackend.dto.LoginResponseDto;
import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.PasswordResetRequestDto;
import com.classroomapp.classroombackend.dto.RegisterDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;

/**
 * Service for authentication-related operations
 */
public interface AuthService {

    /**
     * Authenticate user with username and password
     * 
     * @param loginRequest login request data
     * @return login response with JWT token and user information
     */
    LoginResponseDto authenticateUser(LoginRequestDto loginRequest);
    
    /**
     * Register a new user
     * 
     * @param registerDto registration data
     * @return created user data
     */
    UserDto registerUser(RegisterDto registerDto);
    
    /**
     * Authenticate user with Google ID token
     * 
     * @param googleAuthRequest Google authentication request data
     * @return login response with JWT token and user information
     */
    LoginResponseDto authenticateWithGoogle(GoogleAuthRequestDto googleAuthRequest);
    
    /**
     * Request password reset
     * 
     * @param passwordResetRequest password reset request data with email
     */
    void requestPasswordReset(PasswordResetRequestDto passwordResetRequest);
    
    /**
     * Reset password using token
     * 
     * @param passwordConfirmation password confirmation data with token and new password
     */
    void resetPassword(PasswordConfirmationDto passwordConfirmation);
    
    /**
     * Generate JWT token for a user
     * 
     * @param username username
     * @param roleId role ID
     * @param email email
     * @return generated token
     */
    String generateToken(String username, Integer roleId, String email);
}
