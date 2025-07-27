package com.classroomapp.classroombackend.auth.service;

import com.classroomapp.classroombackend.dto.GoogleAuthRequestDto;
import com.classroomapp.classroombackend.dto.LoginRequestDto;
import com.classroomapp.classroombackend.dto.LoginResponseDto;
import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.PasswordResetRequestDto;
import com.classroomapp.classroombackend.dto.RegisterDto;
import com.classroomapp.classroombackend.dto.UserDto;

/**
 * Service for authentication-related operations
 */
public interface AuthService {

    /**
     * Register a new user
     *
     * @param registerDto registration data
     * @return created user data
     */
    UserDto registerUser(RegisterDto registerDto);

    /**
     * Authenticate user with username/password
     *
     * @param loginRequest login credentials
     * @return JWT token and user details
     */
    LoginResponseDto authenticateUser(LoginRequestDto loginRequest);

    /**
     * Authenticate user with Google OAuth
     *
     * @param googleAuthRequest Google token info
     * @return JWT token and user details
     */
    LoginResponseDto authenticateWithGoogle(GoogleAuthRequestDto googleAuthRequest);

    /**
     * Request password reset (send email or OTP)
     *
     * @param passwordResetRequest email or username
     */
    void requestPasswordReset(PasswordResetRequestDto passwordResetRequest);

    /**
     * Confirm password reset with token
     *
     * @param passwordConfirmation new password confirmation
     */
    void resetPassword(PasswordConfirmationDto passwordConfirmation);

    /**
     * Generate JWT token for given user info
     *
     * @param username user's username
     * @param roleId   user's role
     * @param email    user's email
     * @return JWT token
     */
    String generateToken(String username, Integer roleId, String email);
}
