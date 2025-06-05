package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.AccomplishmentDto;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccomplishmentService {
    private final AccomplishmentRepository accomplishmentRepository;
    private final UserRepository userRepository;
    
    public List<AccomplishmentDto> getStudentAccomplishments(Long userId) {
        log.info("Fetching accomplishments for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<Accomplishment> accomplishments = accomplishmentRepository.findByUserOrderByCompletionDateDesc(user);
        log.info("Found {} accomplishments for user ID: {}", accomplishments.size(), userId);
        
        return accomplishments.stream()
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
        log.info("Creating new accomplishment for user ID: {}", dto.getUserId());
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
        log.info("Created accomplishment with ID: {} for user ID: {}", saved.getId(), user.getId());
        return convertToDto(saved);
    }
    
    @Transactional
    public AccomplishmentDto updateAccomplishment(Long id, AccomplishmentDto dto) {
        log.info("Updating accomplishment with ID: {}", id);
        Accomplishment accomplishment = accomplishmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Accomplishment", "id", id));
        
        // Don't update the user - this is a fixed relationship
        accomplishment.setCourseTitle(dto.getCourseTitle());
        accomplishment.setSubject(dto.getSubject());
        accomplishment.setTeacherName(dto.getTeacherName());
        accomplishment.setGrade(dto.getGrade());
        accomplishment.setCompletionDate(dto.getCompletionDate());
        
        Accomplishment updated = accomplishmentRepository.save(accomplishment);
        log.info("Updated accomplishment with ID: {}", updated.getId());
        return convertToDto(updated);
    }
    
    @Transactional
    public void deleteAccomplishment(Long id) {
        log.info("Deleting accomplishment with ID: {}", id);
        accomplishmentRepository.deleteById(id);
        log.info("Deleted accomplishment with ID: {}", id);
    }
}