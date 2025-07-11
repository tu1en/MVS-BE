package com.classroomapp.classroombackend.dto.usermanagement;

import java.util.Collections;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * UserMapper updated to work with consolidated UserDto
 * Maps User entity to the new consolidated UserDto structure
 */
public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setName(user.getFullName()); // Map fullName to name for compatibility
        dto.setRoleId(user.getRoleId());
        dto.setEnabled("active".equalsIgnoreCase(user.getStatus()));
        dto.setStatus(user.getStatus());
        dto.setRoles(Collections.singleton(user.getRole()));
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }
}