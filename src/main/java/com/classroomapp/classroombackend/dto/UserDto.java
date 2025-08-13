package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consolidated UserDto that combines features from both previous versions
 * Supports both detailed user information and simple role-based representation
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

    // Alternative name field for compatibility with usermanagement version
    private String name;

    // Contact and personal info
    private String phoneNumber;
    private String gender; // MALE, FEMALE, OTHER
    private LocalDate birthDate; // from active contract if available

    private Integer roleId;

    // Role names as Set<String> for compatibility with usermanagement version
    private Set<String> roles;

    private LocalDateTime createdAt;

    private String status;

    // Department info (for display in teacher profile)
    private String department;
    private Long departmentId;

    // Enabled flag for compatibility with usermanagement version
    private boolean enabled;

    // Password is not included in response DTO for security
    // When needed, a separate DTO should be used for password changes

    // Additional constructor for backwards compatibility
    public UserDto(Long id, String username, String email, String fullName, Integer roleId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.name = fullName; // Map fullName to name for compatibility
        this.roleId = roleId;
    }

    // Constructor for usermanagement version compatibility
    public UserDto(Long id, String name, String email, boolean enabled, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.fullName = name; // Map name to fullName for compatibility
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }

    // Constructor từ entity User
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.name = user.getFullName(); // fallback cho name
        this.roleId = user.getRoleId();
        this.createdAt = user.getCreatedAt();
        this.status = user.getStatus();
        this.enabled = true; // bạn có thể map từ status nếu cần: status.equals("active")
        this.phoneNumber = user.getPhoneNumber();
        this.gender = user.getGender();
        // birthDate không thể map trực tiếp từ User (nằm trong Contract)
        // sẽ được điền ở tầng controller/service khi cần
        // Nếu cần map role dạng Set<String> thì bổ sung:
        this.roles = Set.of(user.getRole()); // ví dụ: ["TEACHER"]
    }

    // Helper method to get name (prioritizes fullName, falls back to name)
    public String getDisplayName() {
        return fullName != null ? fullName : name;
    }
}