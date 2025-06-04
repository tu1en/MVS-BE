package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ManagerProfileDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface ManagerService {

    ManagerProfileDto getManagerProfile(@NotBlank @Email String email);

    ManagerProfileDto updateManagerProfile(@NotBlank @Email String email, ManagerProfileDto profileDto);

}
