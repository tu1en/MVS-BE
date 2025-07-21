package com.classroomapp.classroombackend.service.classroommanagement.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ClassroomService
 * Handles business logic for classroom management
 */
@Service("classroomManagementService")
@Transactional
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private FirebaseClassroomService firebaseClassroomService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClassroomDto> getAllClassrooms(Pageable pageable) {
        log.debug("Getting all classrooms with pagination: {}", pageable);
        
        Page<Classroom> classrooms = classroomRepository.findAll(pageable);
        List<ClassroomDto> classroomDtos = classrooms.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
            
        return new PageImpl<>(classroomDtos, pageable, classrooms.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDto getClassroomById(Long id) {
        log.debug("Getting classroom by id: {}", id);

        if (id == null) {
            throw new ValidationException("Classroom ID cannot be null");
        }

        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + id));
        return convertToDto(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClassroomDto> findClassroomById(Long id) {
        log.debug("Finding classroom by id: {}", id);

        if (id == null) {
            throw new ValidationException("Classroom ID cannot be null");
        }

        return classroomRepository.findById(id)
            .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getClassroomsByTeacherId(Long teacherId) {
        log.debug("Getting classrooms by teacher id: {}", teacherId);

        if (teacherId == null) {
            throw new ValidationException("Teacher ID cannot be null");
        }

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacherId);
        return classrooms.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getClassroomsByTeacher(Long teacherId) {
        return getClassroomsByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassroomDto> searchClassrooms(String keyword, Pageable pageable) {
        log.debug("Searching classrooms with keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllClassrooms(pageable);
        }
        
        Page<Classroom> classrooms = classroomRepository.findByClassroomNameContainingIgnoreCase(
            keyword.trim(), pageable);
        List<ClassroomDto> classroomDtos = classrooms.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
            
        return new PageImpl<>(classroomDtos, pageable, classrooms.getTotalElements());
    }

    @Override
    public ClassroomDto createClassroom(CreateClassroomDto createDto) {
        log.debug("Creating new classroom: {}", createDto);
        
        validateCreateClassroomDto(createDto);
        
        // Check if classroom name already exists for the teacher
        if (classroomRepository.existsByClassroomNameAndTeacherId(
                createDto.getClassroomName(), createDto.getTeacherId())) {
            throw new ValidationException("Classroom name already exists for this teacher");
        }
        
        Classroom classroom = new Classroom();
        classroom.setClassroomName(createDto.getClassroomName());
        classroom.setDescription(createDto.getDescription());
        classroom.setTeacherId(createDto.getTeacherId());
        classroom.setCreatedAt(LocalDateTime.now());
        classroom.setUpdatedAt(LocalDateTime.now());
        
        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Created classroom with id: {}", savedClassroom.getId());

        ClassroomDto result = convertToDto(savedClassroom);

        // Sync to Firebase asynchronously
        firebaseClassroomService.syncClassroom(result);

        return result;
    }

    @Override
    public ClassroomDto updateClassroom(Long id, UpdateClassroomDto updateDto) {
        log.debug("Updating classroom id: {} with data: {}", id, updateDto);
        
        if (id == null) {
            throw new ValidationException("Classroom ID cannot be null");
        }
        
        validateUpdateClassroomDto(updateDto);
        
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));
        
        // Check if new name conflicts with existing classroom for the same teacher
        if (updateDto.getClassroomName() != null && 
            !updateDto.getClassroomName().equals(classroom.getClassroomName())) {
            if (classroomRepository.existsByClassroomNameAndTeacherId(
                    updateDto.getClassroomName(), classroom.getTeacherId())) {
                throw new ValidationException("Classroom name already exists for this teacher");
            }
            classroom.setClassroomName(updateDto.getClassroomName());
        }
        
        if (updateDto.getDescription() != null) {
            classroom.setDescription(updateDto.getDescription());
        }
        
        classroom.setUpdatedAt(LocalDateTime.now());
        
        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Updated classroom with id: {}", savedClassroom.getId());

        ClassroomDto result = convertToDto(savedClassroom);

        // Sync to Firebase asynchronously
        firebaseClassroomService.syncClassroom(result);

        return result;
    }

    @Override
    public void deleteClassroom(Long id) {
        log.debug("Deleting classroom id: {}", id);
        
        if (id == null) {
            throw new ValidationException("Classroom ID cannot be null");
        }
        
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));
        
        // Check if classroom has sessions - if so, cannot delete
        // This will be implemented when SessionRepository is available
        
        classroomRepository.delete(classroom);
        log.info("Deleted classroom with id: {}", id);

        // Remove from Firebase asynchronously
        firebaseClassroomService.removeClassroom(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) return false;
        return classroomRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countClassroomsByTeacherId(Long teacherId) {
        if (teacherId == null) return 0;
        return classroomRepository.countByTeacherId(teacherId);
    }

    /**
     * Convert Classroom entity to ClassroomDto
     */
    private ClassroomDto convertToDto(Classroom classroom) {
        if (classroom == null) return null;
        
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setClassroomName(classroom.getClassroomName());
        dto.setDescription(classroom.getDescription());
        dto.setTeacherId(classroom.getTeacherId());
        dto.setCreatedAt(classroom.getCreatedAt());
        dto.setUpdatedAt(classroom.getUpdatedAt());
        
        // Additional calculations can be added here
        // e.g., session count, student count, etc.
        
        return dto;
    }

    /**
     * Validate CreateClassroomDto
     */
    private void validateCreateClassroomDto(CreateClassroomDto dto) {
        if (dto == null) {
            throw new ValidationException("Create classroom data cannot be null");
        }
        
        if (dto.getClassroomName() == null || dto.getClassroomName().trim().isEmpty()) {
            throw new ValidationException("Classroom name is required");
        }
        
        if (dto.getClassroomName().length() > 255) {
            throw new ValidationException("Classroom name cannot exceed 255 characters");
        }
        
        if (dto.getTeacherId() == null) {
            throw new ValidationException("Teacher ID is required");
        }
        
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
    }

    /**
     * Validate UpdateClassroomDto
     */
    private void validateUpdateClassroomDto(UpdateClassroomDto dto) {
        if (dto == null) {
            throw new ValidationException("Update classroom data cannot be null");
        }
        
        if (!dto.hasUpdates()) {
            throw new ValidationException("At least one field must be provided for update");
        }
        
        if (dto.getClassroomName() != null) {
            if (dto.getClassroomName().trim().isEmpty()) {
                throw new ValidationException("Classroom name cannot be empty");
            }
            if (dto.getClassroomName().length() > 255) {
                throw new ValidationException("Classroom name cannot exceed 255 characters");
            }
        }
        
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
    }
}
