package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.ClassroomRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.util.ModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ClassroomDto> getAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClassroomDto getClassroomById(Long id) {
        // Delegate to the existing method
        return GetClassroomById(id);
    }

    @Override
    public ClassroomDto GetClassroomById(Long id) {
        Classroom classroom = FindClassroomById(id);
        return modelMapper.MapToClassroomDto(classroom);
    }

    @Override
    @Transactional
    public ClassroomDto CreateClassroom(CreateClassroomDto createClassroomDto, Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
        
        // Validate teacher role
        if (teacher.getRoleId() != 2 && teacher.getRoleId() != 4) {
            throw new IllegalArgumentException("Only teachers can create classrooms");
        }
        
        // Create and save the new classroom
        Classroom classroom = new Classroom();
        classroom.setName(createClassroomDto.getName());
        classroom.setDescription(createClassroomDto.getDescription());
        classroom.setSection(createClassroomDto.getSection());
        classroom.setSubject(createClassroomDto.getSubject());
        classroom.setTeacher(teacher);
        
        Classroom savedClassroom = classroomRepository.save(classroom);
        return modelMapper.MapToClassroomDto(savedClassroom);
    }

    @Override
    @Transactional
    public ClassroomDto UpdateClassroom(Long id, CreateClassroomDto updateClassroomDto) {
        Classroom classroom = FindClassroomById(id);
        
        // Update the classroom fields
        classroom.setName(updateClassroomDto.getName());
        classroom.setDescription(updateClassroomDto.getDescription());
        classroom.setSection(updateClassroomDto.getSection());
        classroom.setSubject(updateClassroomDto.getSubject());
        
        Classroom updatedClassroom = classroomRepository.save(classroom);
        return modelMapper.MapToClassroomDto(updatedClassroom);
    }

    @Override
    @Transactional
    public void DeleteClassroom(Long id) {
        Classroom classroom = FindClassroomById(id);
        classroomRepository.delete(classroom);
    }

    @Override
    public List<ClassroomDto> GetClassroomsByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
        
        List<Classroom> classrooms = classroomRepository.findByTeacher(teacher);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> GetClassroomsByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        List<Classroom> classrooms = classroomRepository.findByStudentsContaining(student);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void EnrollStudent(Long classroomId, Long studentId) {
        Classroom classroom = FindClassroomById(classroomId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Validate student role
        if (student.getRoleId() != 1 && student.getRoleId() != 4) {
            throw new IllegalArgumentException("Only teachers can create classrooms");
        }
        
        classroom.getStudents().add(student);
        classroomRepository.save(classroom);
    }

    @Override
    @Transactional
    public void UnenrollStudent(Long classroomId, Long studentId) {
        Classroom classroom = FindClassroomById(classroomId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        if (classroom.getStudents().remove(student)) {
            classroomRepository.save(classroom);
        } else {
            throw new IllegalArgumentException("Student is not enrolled in this classroom");
        }
    }

    @Override
    public List<ClassroomDto> SearchClassroomsByName(String name) {
        List<Classroom> classrooms = classroomRepository.findByNameContainingIgnoreCase(name);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> GetClassroomsBySubject(String subject) {
        List<Classroom> classrooms = classroomRepository.findBySubject(subject);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }
    
    // Helper method to find classroom by ID or throw exception
    private Classroom FindClassroomById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));
    }
}