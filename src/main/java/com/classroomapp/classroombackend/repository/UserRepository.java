package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email
     * @param email the email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a username already exists
     * @param username the username to check
     * @return true if a user with the username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email is already registered
     * @param email the email to check
     * @return true if a user with the email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if an email is already registered to another user
     * @param email the email to check
     * @param id the user ID to exclude from the check
     * @return true if a user with the email exists and has a different ID
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * Find users by role ID
     * @param roleId the role ID to search for
     * @return list of users with the specified role
     */
    List<User> findByRoleId(Integer roleId);
} 