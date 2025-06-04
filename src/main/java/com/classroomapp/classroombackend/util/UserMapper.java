package com.classroomapp.classroombackend.util;

import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.model.usermanagement.User;

public class UserMapper {    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleId(user.getRoleId());
        dto.setEnrollmentDate(user.getEnrollmentDate());
        dto.setHireDate(user.getHireDate());
        dto.setDepartment(user.getDepartment());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setStatus(user.getStatus());
        return dto;
    }    public static User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setRoleId(userDto.getRoleId());
        user.setEnrollmentDate(userDto.getEnrollmentDate());
        user.setHireDate(userDto.getHireDate());
        user.setDepartment(userDto.getDepartment());
        user.setCreatedAt(userDto.getCreatedAt());
        user.setUpdatedAt(userDto.getUpdatedAt());
        user.setStatus(userDto.getStatus());
        return user;
    }
}