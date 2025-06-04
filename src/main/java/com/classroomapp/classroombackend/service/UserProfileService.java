package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ManagerProfileDto;
import com.classroomapp.classroombackend.dto.StudentProfileDto;
import com.classroomapp.classroombackend.dto.TeacherProfileDto;
import com.classroomapp.classroombackend.dto.UserDto;

/**
 * Service interface for user profile operations
 */
public interface UserProfileService {

    /**
     * Update a student's profile
     * @param userId ID of the student
     * @param profileDto profile update information
     * @return updated user information
     */
    UserDto updateStudentProfile(Long userId, StudentProfileDto profileDto);
    
    /**
     * Update a teacher's profile
     * @param userId ID of the teacher
     * @param profileDto profile update information
     * @return updated user information
     */
    UserDto updateTeacherProfile(Long userId, TeacherProfileDto profileDto);
    
    /**
     * Update a manager's profile
     * @param userId ID of the manager
     * @param profileDto profile update information
     * @return updated user information
     */
    UserDto updateManagerProfile(Long userId, ManagerProfileDto profileDto);
    
    /**
     * Get the current user's profile based on authentication
     * @return user information
     */
    UserDto getCurrentUserProfile();
    
    /**
     * Validate if the provided password matches the user's current password
     * @param userId user ID
     * @param password password to validate
     * @return true if password matches
     */
    boolean validatePassword(Long userId, String password);
}
