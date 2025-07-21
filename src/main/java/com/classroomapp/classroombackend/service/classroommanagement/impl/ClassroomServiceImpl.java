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
import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ClassroomService
 * Handles classroom CRUD and related queries
 */
@Service("classroomManagementService")
@Transactional
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FirebaseClassroomService firebaseClassroomService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClassroomDto> getAllClassrooms(Pageable pageable) {
        Page<Classroom> classrooms = classroomRepository.findAll(pageable);
        List<ClassroomDto> dtoList = classrooms.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, classrooms.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDto getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + id));
        return convertToDto(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClassroomDto> findClassroomById(Long id) {
        return classroomRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getClassroomsByTeacherId(Long teacherId) {
        if (teacherId == null) throw new ValidationException("Teacher ID cannot be null");
        return classroomRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> getClassroomsByTeacher(Long teacherId) {
        return getClassroomsByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassroomDto> searchClassrooms(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllClassrooms(pageable);
        }
        Page<Classroom> classrooms = classroomRepository.findByClassroomNameContainingIgnoreCase(keyword.trim(), pageable);
        List<ClassroomDto> dtoList = classrooms.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, classrooms.getTotalElements());
    }

    @Override
    public ClassroomDto createClassroom(CreateClassroomDto createDto) {
        validateCreateDto(createDto);

        // Check unique name for teacher
        if (classroomRepository.existsByClassroomNameAndTeacherId(createDto.getName(), createDto.getTeacherId())) {
            throw new ValidationException("Tên classroom đã tồn tại cho giáo viên này");
        }

        Classroom classroom = new Classroom();
        classroom.setName(createDto.getName());
        classroom.setDescription(createDto.getDescription());
        classroom.setTeacherId(createDto.getTeacherId());
        classroom.setCreatedAt(LocalDateTime.now());
        classroom.setUpdatedAt(LocalDateTime.now());

        Classroom saved = classroomRepository.save(classroom);
        ClassroomDto dto = convertToDto(saved);

        firebaseClassroomService.syncClassroom(dto);

        return dto;
    }

    @Override
    public ClassroomDto updateClassroom(Long id, UpdateClassroomDto updateDto) {
        if (id == null) throw new ValidationException("Classroom ID cannot be null");
        validateUpdateDto(updateDto);

        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

        if (updateDto.getClassroomName() != null && !updateDto.getClassroomName().equals(classroom.getName())) {
            if (classroomRepository.existsByClassroomNameAndTeacherId(updateDto.getClassroomName(), classroom.getTeacherId())) {
                throw new ValidationException("Tên classroom đã tồn tại cho giáo viên này");
            }
            classroom.setName(updateDto.getClassroomName());
        }

        if (updateDto.getDescription() != null) {
            classroom.setDescription(updateDto.getDescription());
        }

        classroom.setUpdatedAt(LocalDateTime.now());

        Classroom saved = classroomRepository.save(classroom);
        ClassroomDto dto = convertToDto(saved);

        firebaseClassroomService.syncClassroom(dto);

        return dto;
    }

    @Override
    public void deleteClassroom(Long id) {
        if (id == null) throw new ValidationException("Classroom ID cannot be null");

        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

        classroomRepository.delete(classroom);

        firebaseClassroomService.removeClassroom(id);
    }

    @Override
    public boolean existsById(Long id) {
        return classroomRepository.existsById(id);
    }

    @Override
    public long countClassroomsByTeacherId(Long teacherId) {
        return classroomRepository.countByTeacherId(teacherId);
    }

    // ✅ NEW METHODS FOR STUDENT

    @Override
    @Transactional(readOnly = true)
    public ClassroomDto getClassroomDetails(Long id) {
        Classroom classroom = classroomRepository.findDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + id));
        return convertToDto(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getClassroomsByStudentId(Long studentId) {
        if (studentId == null) throw new ValidationException("Student ID không được null");
        return classroomRepository.findByStudents_Id(studentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getClassroomsByStudentUsername(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty())
            throw new ValidationException("Username hoặc email không được trống");

        // Check if input is email format
        User student;
        if (usernameOrEmail.contains("@")) {
            student = userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy student với email: " + usernameOrEmail));
        } else {
            student = userRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy student với username: " + usernameOrEmail));
        }

        return getClassroomsByStudentId(student.getId());
    }

    // ===== Private Helpers =====

    private ClassroomDto convertToDto(Classroom classroom) {
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setClassroomName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setTeacherId(classroom.getTeacherId());
        dto.setCreatedAt(classroom.getCreatedAt());
        dto.setUpdatedAt(classroom.getUpdatedAt());
        return dto;
    }

    private void validateCreateDto(CreateClassroomDto dto) {
        if (dto == null) throw new ValidationException("CreateClassroomDto không được null");
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Tên classroom là bắt buộc");
        }
        if (dto.getTeacherId() == null) {
            throw new ValidationException("Teacher ID là bắt buộc");
        }
    }

    private void validateUpdateDto(UpdateClassroomDto dto) {
        if (dto == null) throw new ValidationException("UpdateClassroomDto không được null");
        if (!dto.hasUpdates()) {
            throw new ValidationException("Không có thông tin cập nhật");
        }
    }
}
