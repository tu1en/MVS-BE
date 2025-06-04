package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ManagerProfileDto;
import com.classroomapp.classroombackend.dto.StudentProfileDto;
import com.classroomapp.classroombackend.dto.TeacherProfileDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling profile updates for different user types
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserProfileService userProfileService;

    /**
     * Get current user's profile
     * @return user profile information
     */
    @GetMapping
    public ResponseEntity<UserDto> getCurrentProfile() {
        log.info("Fetching current user profile");
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile());
    }

    /**
     * Update student profile
     * @param userId student ID
     * @param profileDto profile update information
     * @return updated user information
     */
    @PutMapping("/student/{userId}")
    @PreAuthorize("hasRole('STUDENT') and authentication.principal == #userId.toString() or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateStudentProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StudentProfileDto profileDto) {
        log.info("Updating student profile for user ID: {}", userId);
        return ResponseEntity.ok(userProfileService.updateStudentProfile(userId, profileDto));
    }

    /**
     * Update teacher profile
     * @param userId teacher ID
     * @param profileDto profile update information
     * @return updated user information
     */
    @PutMapping("/teacher/{userId}")
    @PreAuthorize("hasRole('TEACHER') and authentication.principal == #userId.toString() or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateTeacherProfile(
            @PathVariable Long userId,
            @Valid @RequestBody TeacherProfileDto profileDto) {
        log.info("Updating teacher profile for user ID: {}", userId);
        return ResponseEntity.ok(userProfileService.updateTeacherProfile(userId, profileDto));
    }

    /**
     * Update manager profile
     * @param userId manager ID
     * @param profileDto profile update information
     * @return updated user information
     */
    @PutMapping("/manager/{userId}")
    @PreAuthorize("hasRole('MANAGER') and authentication.principal == #userId.toString() or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateManagerProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ManagerProfileDto profileDto) {
        log.info("Updating manager profile for user ID: {}", userId);
        return ResponseEntity.ok(userProfileService.updateManagerProfile(userId, profileDto));
    }
}
