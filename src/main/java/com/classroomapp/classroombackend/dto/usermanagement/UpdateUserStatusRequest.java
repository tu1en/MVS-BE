package com.classroomapp.classroombackend.dto.usermanagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull
    private Boolean enabled;
} 