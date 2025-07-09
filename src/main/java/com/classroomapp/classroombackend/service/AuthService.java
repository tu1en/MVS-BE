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
     * Register a new user
     * 
     * @param registerDto registration data
     * @return created user data
     */
    UserDto registerUser(RegisterDto registerDto);

    LoginResponseDto authenticateUser(LoginRequestDto loginRequest);

    LoginResponseDto authenticateWithGoogle(GoogleAuthRequestDto googleAuthRequest);

    void requestPasswordReset(PasswordResetRequestDto passwordResetRequest);

    void resetPassword(PasswordConfirmationDto passwordConfirmation);

    String generateToken(String username, Integer roleId, String email);
}
