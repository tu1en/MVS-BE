package com.classroomapp.classroombackend.service.classroommanagement.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDTO;
import com.classroomapp.classroombackend.dto.usermanagement.UserMapper;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.mapper.ClassroomMapper;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomMapper classroomMapper;
    private final ClassroomEnrollmentRepository enrollmentRepository;

    @Override
    public Page<ClassroomDto> getAllClassrooms(Pageable pageable) {
        return classroomRepository.findAll(pageable).map(classroomMapper::toDto);
    }

    @Override
    public ClassroomDto getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));
        return classroomMapper.toDto(classroom);
    }

    @Override
    public ClassroomDto createClassroom(CreateClassroomDto createDto) {
        User teacher = userRepository.findById(createDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDto.getTeacherId()));

        Classroom classroom = new Classroom();
        classroom.setName(createDto.getName());
        classroom.setDescription(createDto.getDescription());
        classroom.setSection(createDto.getSection());
        classroom.setSubject(createDto.getSubject());
        classroom.setTeacher(teacher);

        Classroom saved = classroomRepository.save(classroom);
        return classroomMapper.toDto(saved);
    }

    @Override
    public ClassroomDto updateClassroom(Long id, UpdateClassroomDto updateDto) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));

        if (updateDto.hasUpdates()) {
            classroom.setName(updateDto.getName());
            classroom.setDescription(updateDto.getDescription());
        }

        Classroom updated = classroomRepository.save(classroom);
        return classroomMapper.toDto(updated);
    }
@Override
public ClassroomDto getClassroomDetails(Long id) {
    Classroom classroom = classroomRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

    ClassroomDto dto = classroomMapper.toDto(classroom);

    // Calculate additional information
    dto.setStudentCount(dto.getEnrolledStudents() != null ? dto.getEnrolledStudents().size() : 0);
    dto.setAssignmentCount(dto.getAssignments() != null ? dto.getAssignments().size() : 0);
    dto.calculateProgress();

    return dto;
}

    @Override
    public void deleteClassroom(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classroom", "id", id);
        }
        classroomRepository.deleteById(id);
    }

    @Override
    public void enrollStudent(Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        classroom.getStudents().add(student);
        classroomRepository.save(classroom);
    }

    @Override
    public void unenrollStudent(Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        classroom.getStudents().remove(student);
        classroomRepository.save(classroom);
    }

    @Override
    public Page<ClassroomDto> searchClassrooms(String name, Pageable pageable) {
        return classroomRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(classroomMapper::toDto);
    }

    @Override
    public List<ClassroomDto> getClassroomsByCurrentTeacher() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // Try to find by username first, then by email for better compatibility
        User teacher = userRepository.findByUsername(currentUsername)
                .or(() -> userRepository.findByEmail(currentUsername))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username/email", currentUsername));
        return classroomRepository.findByTeacherId(teacher.getId())
                .stream().map(classroomMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> getClassroomsByCurrentStudent() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // Try to find by username first, then by email for better compatibility
        User student = userRepository.findByUsername(currentUsername)
                .or(() -> userRepository.findByEmail(currentUsername))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username/email", currentUsername));
        return classroomRepository.findByStudents_Id(student.getId())
                .stream().map(classroomMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getStudentsInClassroom(Long classroomId) {
        // Verify classroom exists
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        // Get enrolled students
        return enrollmentRepository.findByClassroomId(classroomId)
                .stream()
                .map(enrollment -> UserMapper.toUserDto(enrollment.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> getClassroomsByStudentId(Long studentId) {
        List<Classroom> classrooms = classroomRepository.findClassroomsByStudentId(studentId);
        return classrooms.stream()
                .map(classroomMapper::toDto)
                .collect(Collectors.toList());
    }
}
