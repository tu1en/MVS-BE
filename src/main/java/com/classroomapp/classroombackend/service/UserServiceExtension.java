package com.classroomapp.classroombackend.service;

public interface UserServiceExtension {
    /**
     * Update user role
     * @param email User's email
     * @param role New role to set
     * @return true if successful, false otherwise
     */
    boolean updateUserRole(String email, String role);
    
    /**
     * Check if a user exists
     * @param email User's email
     * @return true if exists, false otherwise
     */
    boolean userExists(String email);
    
    /**
     * Create a new user with the given role if they don't exist
     * @param email User's email
     * @param fullName User's full name
     * @param role Role to assign
     * @return true if created or updated, false otherwise
     */
    boolean createOrUpdateUser(String email, String fullName, String role);
} 