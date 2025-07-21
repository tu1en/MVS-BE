package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomService;

import lombok.extern.slf4j.Slf4j;

/**
 * Main implementation of ClassroomService interface
 * This service acts as a bridge between the main ClassroomService interface
 * and the ClassroomManagementService implementation
 */
@Service
@Transactional
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getAllClassrooms() {
        log.info("Retrieving all classrooms");
        try {
            List<Classroom> classrooms = classroomRepository.findAll();
            return classrooms.stream()
                    .map(this::convertToMainDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving all classrooms: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving classrooms", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDto getClassroomById(Long id) {
        log.info("Retrieving classroom by ID: {}", id);
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + id));
        return convertToMainDto(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDto GetClassroomById(Long id) {
        return getClassroomById(id);
    }

    @Override
    public ClassroomDetailsDto createClassroom(CreateClassroomDto dto) {
        log.info("Creating classroom: {}", dto.getName());
        
        // Get current user as teacher
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmailOrUsername = auth.getName();
        User currentUser = userRepository.findByEmail(currentEmailOrUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user hiện tại"));
        
        // Create classroom entity
        Classroom classroom = new Classroom();
        classroom.setName(dto.getName());
        classroom.setDescription(dto.getDescription());
        classroom.setTeacher(currentUser);
        classroom.setCreatedBy(currentUser);
        classroom.setCreatedAt(LocalDateTime.now());
        classroom.setUpdatedAt(LocalDateTime.now());
        
        Classroom savedClassroom = classroomRepository.save(classroom);
        
        // Convert to ClassroomDetailsDto
        ClassroomDetailsDto detailsDto = new ClassroomDetailsDto();
        detailsDto.setId(savedClassroom.getId());
        detailsDto.setName(savedClassroom.getName());
        detailsDto.setDescription(savedClassroom.getDescription());
        // Teacher information will be set as nested object
        if (savedClassroom.getTeacher() != null) {
            UserDetailsDto teacherDto = new UserDetailsDto();
            teacherDto.setId(savedClassroom.getTeacher().getId());
            teacherDto.setFullName(savedClassroom.getTeacher().getFullName());
            detailsDto.setTeacher(teacherDto);
        }
        
        return detailsDto;
    }

    @Override
    public ClassroomDto UpdateClassroom(Long id, UpdateClassroomDto updateClassroomDto, UserDetails userDetails) {
        log.info("Updating classroom: {}", id);
        
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + id));
        
        if (updateClassroomDto.getName() != null) {
            classroom.setName(updateClassroomDto.getName());
        }
        if (updateClassroomDto.getDescription() != null) {
            classroom.setDescription(updateClassroomDto.getDescription());
        }
        classroom.setUpdatedAt(LocalDateTime.now());
        
        Classroom updatedClassroom = classroomRepository.save(classroom);
        return convertToMainDto(updatedClassroom);
    }

    @Override
    public void DeleteClassroom(Long id) {
        log.info("Deleting classroom: {}", id);
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + id));
        classroomRepository.delete(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> GetClassroomsByTeacher(Long teacherId) {
        log.info("Retrieving classrooms by teacher ID: {}", teacherId);
        return classroomRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToMainDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> GetClassroomsByCurrentTeacher() {
        log.info("Retrieving classrooms for current teacher");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }
        
        String currentEmailOrUsername = auth.getName();
        User currentUser = userRepository.findByEmail(currentEmailOrUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user hiện tại"));
        
        return GetClassroomsByTeacher(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> GetClassroomsByStudent(Long studentId) {
        log.info("Retrieving classrooms by student ID: {}", studentId);
        return classroomRepository.findByStudents_Id(studentId).stream()
                .map(this::convertToMainDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> getClassroomsByCurrentStudent() {
        log.info("Retrieving classrooms for current student");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }
        
        String currentEmailOrUsername = auth.getName();
        User currentUser = userRepository.findByEmail(currentEmailOrUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user hiện tại"));
        
        return GetClassroomsByStudent(currentUser.getId());
    }

    @Override
    public void EnrollStudent(Long classroomId, Long studentId) {
        log.info("Enrolling student {} in classroom {}", studentId, classroomId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + classroomId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy student với ID: " + studentId));
        
        // Note: Student enrollment should be managed through ClassroomEnrollment entity
        // This is a simplified implementation - in production, use enrollment service
        log.warn("Student enrollment through ClassroomEnrollment not implemented yet");
    }

    @Override
    public void UnenrollStudent(Long classroomId, Long studentId) {
        log.info("Unenrolling student {} from classroom {}", studentId, classroomId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + classroomId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy student với ID: " + studentId));
        
        // Note: Student unenrollment should be managed through ClassroomEnrollment entity
        // This is a simplified implementation - in production, use enrollment service
        log.warn("Student unenrollment through ClassroomEnrollment not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> SearchClassroomsByName(String name) {
        log.info("Searching classrooms by name: {}", name);
        return classroomRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToMainDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDto> GetClassroomsBySubject(String subject) {
        log.info("Retrieving classrooms by subject: {}", subject);
        return classroomRepository.findBySubject(subject).stream()
                .map(this::convertToMainDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailsDto GetCourseDetails(Long classroomId) {
        log.info("Retrieving course details for classroom: {}", classroomId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + classroomId));
        
        CourseDetailsDto courseDetails = new CourseDetailsDto();
        courseDetails.setId(classroom.getId());
        courseDetails.setName(classroom.getName());
        courseDetails.setDescription(classroom.getDescription());
        // Teacher ID will be accessed through teacher relationship
        
        // Set teacher information
        if (classroom.getTeacher() != null) {
            UserDto teacherDto = new UserDto();
            teacherDto.setId(classroom.getTeacher().getId());
            teacherDto.setName(classroom.getTeacher().getFullName());
            teacherDto.setEmail(classroom.getTeacher().getEmail());
            courseDetails.setTeacher(teacherDto);
        }
        
        // Get student count
        int studentCount = classroom.getStudents() != null ? classroom.getStudents().size() : 0;
        courseDetails.setTotalStudents(studentCount);
        
        return courseDetails;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getStudentsInClassroom(Long classroomId) {
        log.info("Retrieving students in classroom: {}", classroomId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + classroomId));
        
        return classroom.getStudents() != null ? new ArrayList<>(classroom.getStudents()) : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDetailsDto findClassroomDetailsById(Long classroomId) {
        log.info("Finding classroom details by ID: {}", classroomId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy classroom với ID: " + classroomId));
        
        ClassroomDetailsDto detailsDto = new ClassroomDetailsDto();
        detailsDto.setId(classroom.getId());
        detailsDto.setName(classroom.getName());
        detailsDto.setDescription(classroom.getDescription());
        // Teacher information will be set as nested object
        if (classroom.getTeacher() != null) {
            UserDetailsDto teacherDto = new UserDetailsDto();
            teacherDto.setId(classroom.getTeacher().getId());
            teacherDto.setFullName(classroom.getTeacher().getFullName());
            detailsDto.setTeacher(teacherDto);
        }
        
        return detailsDto;
    }

    // Helper methods for DTO conversion
    private ClassroomDto convertToMainDto(Classroom classroom) {
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setTeacherId(classroom.getTeacher() != null ? classroom.getTeacher().getId() : null);
        dto.setSubject(classroom.getSubject());
        
        // Get teacher name if available
        if (classroom.getTeacher() != null) {
            dto.setTeacherName(classroom.getTeacher().getFullName());
        }
        
        // Set student count
        int studentCount = classroom.getStudents() != null ? classroom.getStudents().size() : 0;
        dto.setStudentCount(studentCount);
        
        return dto;
    }
}