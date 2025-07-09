package com.classroomapp.classroombackend.dto.usermanagement;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateUserRolesRequest {
    @NotEmpty
    private Set<String> roles;
} 