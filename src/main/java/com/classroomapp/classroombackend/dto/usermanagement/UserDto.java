package com.classroomapp.classroombackend.dto.usermanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Integer roleId;
    
    private String roleName; // Computed field
    
    private LocalDate enrollmentDate;
    
    private LocalDate hireDate;
    
    private String department;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String status;

    // Password is not included in response DTO for security
    // When needed, a separate DTO should be used for password changes
    
    // Explicit getters to resolve compilation issues
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Integer getRoleId() { return roleId; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public LocalDate getHireDate() { return hireDate; }
    public String getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }
}
