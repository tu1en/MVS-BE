package com.classroomapp.classroombackend.dto.usermanagement;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Integer roleId;
    private LocalDate hireDate;
    private String department;
    private String status;
}
