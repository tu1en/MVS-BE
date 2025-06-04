package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ManagerProfileDto;
import com.classroomapp.classroombackend.dto.ProfileUpdateDto;
import com.classroomapp.classroombackend.dto.StudentProfileDto;
import com.classroomapp.classroombackend.dto.TeacherProfileDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.UnauthorizedException;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.UserProfileService;
import com.classroomapp.classroombackend.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto updateStudentProfile(Long userId, StudentProfileDto profileDto) {
        log.info("Updating student profile for user ID: {}", userId);
        User user = getUserAndValidateRole(userId, 1); // 1 = STUDENT role
        
        updateCommonFields(user, profileDto);
        user.setEnrollmentDate(profileDto.getEnrollmentDate());
        
        User updatedUser = userRepository.save(user);
        log.info("Student profile updated successfully for user ID: {}", userId);
        return UserMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto updateTeacherProfile(Long userId, TeacherProfileDto profileDto) {
        log.info("Updating teacher profile for user ID: {}", userId);
        User user = getUserAndValidateRole(userId, 2); // 2 = TEACHER role
        
        updateCommonFields(user, profileDto);
        // Update teacher specific fields
        if (profileDto.getDepartment() != null) {
            user.setDepartment(profileDto.getDepartment());
        }
        if (profileDto.getHireDate() != null) {
            user.setHireDate(profileDto.getHireDate());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Teacher profile updated successfully for user ID: {}", userId);
        return UserMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto updateManagerProfile(Long userId, ManagerProfileDto profileDto) {
        log.info("Updating manager profile for user ID: {}", userId);
        User user = getUserAndValidateRole(userId, 3); // 3 = MANAGER role
        
        updateCommonFields(user, profileDto);
        // Update manager specific fields
        if (profileDto.getDepartment() != null) {
            user.setDepartment(profileDto.getDepartment());
        }
        if (profileDto.getHireDate() != null) {
            user.setHireDate(profileDto.getHireDate());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Manager profile updated successfully for user ID: {}", userId);
        return UserMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return UserMapper.toDto(user);
    }

    @Override
    public boolean validatePassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    // Helper methods
    
    private User getUserAndValidateRole(Long userId, Integer expectedRoleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Validate that the user has the expected role
        if (!expectedRoleId.equals(user.getRoleId())) {
            throw new UnauthorizedException("User does not have the required role");
        }
        
        return user;
    }
    
    private void updateCommonFields(User user, ProfileUpdateDto profileDto) {
        // Update email if provided and different
        if (profileDto.getEmail() != null && !profileDto.getEmail().equals(user.getEmail())) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmailAndIdNot(profileDto.getEmail(), user.getId())) {
                throw new IllegalArgumentException("Email already registered to another user");
            }
            user.setEmail(profileDto.getEmail());
        }
        
        // Update full name if provided
        if (profileDto.getFullName() != null) {
            user.setFullName(profileDto.getFullName());
        }
        
        // Update password if provided
        if (profileDto.getNewPassword() != null && !profileDto.getNewPassword().isEmpty()) {
            // Validate current password if provided
            if (profileDto.getCurrentPassword() == null || !passwordEncoder.matches(profileDto.getCurrentPassword(), user.getPassword())) {
                throw new UnauthorizedException("Current password is incorrect");
            }
            
            user.setPassword(passwordEncoder.encode(profileDto.getNewPassword()));
        }
        
        // Update timestamp
        user.setUpdatedAt(LocalDateTime.now());
    }
}
