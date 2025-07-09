package com.classroomapp.classroombackend.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.classroomapp.classroombackend.dto.AccomplishmentDto;

public interface AccomplishmentService {

    List<AccomplishmentDto> getAccomplishmentsByOwner(UserDetails userDetails);

    AccomplishmentDto createAccomplishment(AccomplishmentDto accomplishmentDto, UserDetails userDetails);

    AccomplishmentDto updateAccomplishment(Long id, AccomplishmentDto accomplishmentDto);

    void deleteAccomplishment(Long id);
    
    List<AccomplishmentDto> getStudentAccomplishments(Long userId);

}