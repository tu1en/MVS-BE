package com.classroomapp.classroombackend.dto;

import java.util.Collections;

import com.classroomapp.classroombackend.model.enums.UserRole;
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
        
        // Add department and roleEnum mapping
        dto.setDepartment(user.getDepartment());
        
        // Add missing fields from User entity
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setHireDate(user.getHireDate());
        dto.setAnnualLeaveBalance(user.getAnnualLeaveBalance());
        dto.setLeaveResetDate(user.getLeaveResetDate());
        
        // Convert User.RoleEnum to UserRole
        User.RoleEnum roleEnum = user.getRoleEnum();
        if (roleEnum != null) {
            switch (roleEnum) {
                case STUDENT: dto.setRoleEnum(UserRole.STUDENT); break;
                case TEACHER: dto.setRoleEnum(UserRole.TEACHER); break;
                case MANAGER: dto.setRoleEnum(UserRole.MANAGER); break;
                case ADMIN: dto.setRoleEnum(UserRole.ADMIN); break;
                case ACCOUNTANT: dto.setRoleEnum(UserRole.ACCOUNTANT); break;
            }
        }

        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setRoleId(dto.getRoleId());
        user.setStatus(dto.getStatus());
        user.setDepartment(dto.getDepartment());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setHireDate(dto.getHireDate());
        user.setAnnualLeaveBalance(dto.getAnnualLeaveBalance());
        user.setLeaveResetDate(dto.getLeaveResetDate());
        user.setCreatedAt(dto.getCreatedAt());

        return user;
    }
}