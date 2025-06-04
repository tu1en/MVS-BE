package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base DTO for profile updates - common fields for all user types
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDto {
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    // Optional current password for validation when changing sensitive information
    private String currentPassword;
    
    // Optional new password if user wants to change it
    private String newPassword;
}
