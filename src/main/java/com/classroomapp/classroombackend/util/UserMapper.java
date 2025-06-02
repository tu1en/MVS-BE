package com.classroomapp.classroombackend.util;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoleId(),
                user.getCreatedAt(),
                user.getStatus()
        );
    }

    public static User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setRoleId(userDto.getRoleId());
        user.setCreatedAt(userDto.getCreatedAt());
        user.setStatus(userDto.getStatus());
        return user;
    }
}