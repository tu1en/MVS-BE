package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.AccomplishmentDto;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccomplishmentService {
    private final AccomplishmentRepository accomplishmentRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public AccomplishmentService(AccomplishmentRepository accomplishmentRepository, UserRepository userRepository) {
        this.accomplishmentRepository = accomplishmentRepository;
        this.userRepository = userRepository;
    }
    
    public List<AccomplishmentDto> getStudentAccomplishments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        return accomplishmentRepository.findByUserOrderByCompletionDateDesc(user)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private AccomplishmentDto convertToDto(Accomplishment accomplishment) {
        return new AccomplishmentDto(
            accomplishment.getId(),
            accomplishment.getUser().getId(),
            accomplishment.getCourseTitle(),
            accomplishment.getSubject(),
            accomplishment.getTeacherName(),
            accomplishment.getGrade(),
            accomplishment.getCompletionDate(),
            accomplishment.getUser().getFullName()
        );
    }
    
    @Transactional
    public AccomplishmentDto createAccomplishment(AccomplishmentDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));
        
        Accomplishment accomplishment = new Accomplishment();
        accomplishment.setUser(user);
        accomplishment.setCourseTitle(dto.getCourseTitle());
        accomplishment.setSubject(dto.getSubject());
        accomplishment.setTeacherName(dto.getTeacherName());
        accomplishment.setGrade(dto.getGrade());
        accomplishment.setCompletionDate(dto.getCompletionDate());
        
        Accomplishment saved = accomplishmentRepository.save(accomplishment);
        return convertToDto(saved);
    }
    
    @Transactional
    public AccomplishmentDto updateAccomplishment(Long id, AccomplishmentDto dto) {
        Accomplishment accomplishment = accomplishmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Accomplishment", "id", id));
        
        // Don't update the user - this is a fixed relationship
        accomplishment.setCourseTitle(dto.getCourseTitle());
        accomplishment.setSubject(dto.getSubject());
        accomplishment.setTeacherName(dto.getTeacherName());
        accomplishment.setGrade(dto.getGrade());
        accomplishment.setCompletionDate(dto.getCompletionDate());
        
        Accomplishment updated = accomplishmentRepository.save(accomplishment);
        return convertToDto(updated);
    }
    
    @Transactional
    public void deleteAccomplishment(Long id) {
        accomplishmentRepository.deleteById(id);
    }
}