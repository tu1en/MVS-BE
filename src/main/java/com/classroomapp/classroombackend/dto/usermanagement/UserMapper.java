package com.classroomapp.classroombackend.dto.usermanagement;

import java.util.Collections;

import com.classroomapp.classroombackend.model.usermanagement.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setEnabled("active".equalsIgnoreCase(user.getStatus()));
        dto.setRoles(Collections.singleton(user.getRole()));
        
        return dto;
    }
} 