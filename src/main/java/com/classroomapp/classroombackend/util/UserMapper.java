package com.classroomapp.classroombackend.util;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.User;

/**
 * Utility class for mapping between User entity and UserDto
 */
public class UserMapper {
    
    /**
     * Convert User entity to UserDto
     * @param user User entity
     * @return UserDto
     */
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
    
    /**
     * Convert UserDto to User entity
     * @param userDto UserDto
     * @return User entity
     */
    public static User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setRole(userDto.getRole());
        
        // Note: Password is not set here for security reasons
        // It should be handled separately with proper encoding
        
        return user;
    }
} 