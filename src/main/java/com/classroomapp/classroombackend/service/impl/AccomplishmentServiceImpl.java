package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.AccomplishmentDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AccomplishmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccomplishmentServiceImpl implements AccomplishmentService {

    private final AccomplishmentRepository accomplishmentRepository;
    private final UserRepository userRepository;

    @Override
    public List<AccomplishmentDto> getAccomplishmentsByOwner(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return accomplishmentRepository.findByStudentOrderByCompletionDateDesc(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccomplishmentDto createAccomplishment(AccomplishmentDto accomplishmentDto, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        Accomplishment accomplishment = new Accomplishment();
        accomplishment.setTitle(accomplishmentDto.getTitle());
        accomplishment.setDescription(accomplishmentDto.getDescription());
        accomplishment.setIssueDate(accomplishmentDto.getIssueDate());
        accomplishment.setStudent(user);

        Accomplishment saved = accomplishmentRepository.save(accomplishment);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public AccomplishmentDto updateAccomplishment(Long id, AccomplishmentDto accomplishmentDto) {
        Accomplishment accomplishment = accomplishmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Accomplishment", "id", id));
        
        // Update fields
        accomplishment.setTitle(accomplishmentDto.getTitle());
        accomplishment.setDescription(accomplishmentDto.getDescription());
        accomplishment.setIssueDate(accomplishmentDto.getIssueDate());

        Accomplishment updated = accomplishmentRepository.save(accomplishment);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteAccomplishment(Long id) {
        accomplishmentRepository.deleteById(id);
    }

    @Override
    public List<AccomplishmentDto> getStudentAccomplishments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return accomplishmentRepository.findByStudentOrderByCompletionDateDesc(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AccomplishmentDto convertToDto(Accomplishment accomplishment) {
        AccomplishmentDto dto = new AccomplishmentDto();
        dto.setId(accomplishment.getId());
        dto.setTitle(accomplishment.getTitle());
        dto.setDescription(accomplishment.getDescription());
        dto.setIssueDate(accomplishment.getIssueDate());
        if(accomplishment.getStudent() != null) {
            dto.setUserId(accomplishment.getStudent().getId());
            dto.setUserName(accomplishment.getStudent().getFullName());
        }
        // Also map legacy fields
        dto.setCourseTitle(accomplishment.getCourseTitle());
        dto.setSubject(accomplishment.getSubject());
        dto.setTeacherName(accomplishment.getTeacherName());
        dto.setGrade(accomplishment.getGrade());
        dto.setCompletionDate(accomplishment.getCompletionDate());
        return dto;
    }
} 