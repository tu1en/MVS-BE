package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.usermanagement.UserDto;

public interface UserService {
    
    /**
     * Find all users
     * @return list of all users
     */
    List<UserDto> FindAllUsers();
    
    /**
     * Find user by ID
     * @param id user ID
     * @return user DTO if found
     */
    UserDto FindUserById(Long id);
    
    /**
     * Find user by username
     * @param username username to search
     * @return user DTO if found
     */
    UserDto FindUserByUsername(String username);
    
    /**
     * Create a new user
     * @param userDto user information
     * @return created user DTO
     */
    UserDto CreateUser(UserDto userDto);
    
    /**
     * Update existing user
     * @param id user ID
     * @param userDto updated user information
     * @return updated user DTO
     */
    UserDto UpdateUser(Long id, UserDto userDto);
    
    /**
     * Delete user
     * @param id user ID
     */
    void DeleteUser(Long id);
    
    /**
     * Check if username exists
     * @param username username to check
     * @return true if username exists
     */
    boolean IsUsernameExists(String username);
    
    /**
     * Check if email exists
     * @param email email to check
     * @return true if email exists
     */
    boolean IsEmailExists(String email);
    
    /**
     * Find users by role ID
     * @param roleId role ID to filter by
     * @return list of users with specified role
     */
    List<UserDto> FindUsersByRole(Integer roleId);

    void sendPasswordResetEmail(String email, String resetLink);
}