package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.classroomapp.classroombackend.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified UserDto for all contexts (manager, usermanagement, HR)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    // Alternative name field for compatibility
    private String name;

    private Integer roleId;
    
    // Role as string (single role)
    private String role;

    // Role names as Set<String> for compatibility
    private Set<String> roles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String status;

    private boolean enabled;

    // ✅ Bổ sung để tránh lỗi
    private String department; // Để search/filter
    private UserRole roleEnum; // Để code cũ dùng getRoleEnum()

    // Missing fields from User entity
    private String phoneNumber;
    private LocalDate hireDate;
    private Integer annualLeaveBalance;
    private LocalDate leaveResetDate;

    // ✅ Constructors
    public UserDto(Long id, String username, String email, String fullName, Integer roleId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.name = fullName;
        this.roleId = roleId;
        this.roleEnum = mapRoleIdToEnum();
    }

    public UserDto(Long id, String name, String email, boolean enabled, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.fullName = name;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
        this.roleEnum = mapRoleIdToEnum();
    }

    // Constructor needed by ClassroomMapper
    public UserDto(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.name = fullName;
        this.email = email;
    }

    // ✅ Helper
    public String getDisplayName() {
        return fullName != null ? fullName : name;
    }

    public UserRole getRoleEnum() {
        if (roleEnum == null) {
            roleEnum = mapRoleIdToEnum();
        }
        return roleEnum;
    }

    private UserRole mapRoleIdToEnum() {
        if (roleId == null) return null;
        switch (roleId) {
            case 1: return UserRole.STUDENT;
            case 2: return UserRole.TEACHER;
            case 3: return UserRole.MANAGER;
            case 4: return UserRole.ADMIN;
            case 5: return UserRole.ACCOUNTANT;
            default: return null;
        }
    }
}
