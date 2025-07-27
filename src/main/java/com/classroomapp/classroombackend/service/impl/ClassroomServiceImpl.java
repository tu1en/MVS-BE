package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.UserMapper;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.mapper.ClassroomMapper;
import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("classroomService")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomMapper classroomMapper;
    private final ClassroomEnrollmentRepository enrollmentRepository;
    private final StudentProgressRepository studentProgressRepository;

    @Override
    public Page<ClassroomDto> getAllClassrooms(Pageable pageable) {
        log.info("Getting all classrooms with pagination");
        return classroomRepository.findAll(pageable).map(classroomMapper::toDto);
    }

    @Override
    public ClassroomDto getClassroomById(Long id) {
        log.info("Getting classroom by id: {}", id);
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));
        ClassroomDto dto = classroomMapper.toDto(classroom);
        
        // Add progress calculation for current user if student
        try {
            Long currentUserId = getCurrentUserId();
            dto.setProgressPercentage(calculateStudentProgress(currentUserId, id));
        } catch (Exception e) {
            log.warn("Could not calculate progress for classroom {}: {}", id, e.getMessage());
            dto.setProgressPercentage(0.0);
        }
        
        return dto;
    }

    @Override
    public ClassroomDto createClassroom(CreateClassroomDto createDto) {
        log.info("Creating new classroom with name: {}", createDto.getName());
        User teacher = userRepository.findById(createDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDto.getTeacherId()));

        Classroom classroom = new Classroom();
        classroom.setName(createDto.getName());
        classroom.setDescription(createDto.getDescription());
        classroom.setSection(createDto.getSection());
        classroom.setSubject(createDto.getSubject());
        classroom.setTeacher(teacher);

        Classroom saved = classroomRepository.save(classroom);
        log.info("Created classroom with id: {}", saved.getId());
        return classroomMapper.toDto(saved);
    }

    @Override
    public ClassroomDto updateClassroom(Long id, UpdateClassroomDto updateDto) {
        log.info("Updating classroom with id: {}", id);
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));

        if (updateDto.hasUpdates()) {
            classroom.setName(updateDto.getName());
            classroom.setDescription(updateDto.getDescription());
        }

        Classroom updated = classroomRepository.save(classroom);
        log.info("Updated classroom: {}", updated.getId());
        return classroomMapper.toDto(updated);
    }

    @Override
    public ClassroomDto getClassroomDetails(Long id) {
        log.info("Getting detailed information for classroom: {}", id);
        Classroom classroom = classroomRepository.findDetailsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

        ClassroomDto dto = classroomMapper.toDto(classroom);
        
        // Calculate additional statistics
        if (dto.getEnrolledStudents() != null) {
            dto.setStudentCount(dto.getEnrolledStudents().size());
        }
        if (dto.getAssignments() != null) {
            dto.setAssignmentCount(dto.getAssignments().size());
        }
        
        // Calculate progress if current user is a student
        try {
            Long currentUserId = getCurrentUserId();
            dto.setProgressPercentage(calculateStudentProgress(currentUserId, id));
        } catch (Exception e) {
            log.warn("Could not calculate progress for classroom {}: {}", id, e.getMessage());
            dto.setProgressPercentage(0.0);
        }

        return dto;
    }

    @Override
    public void deleteClassroom(Long id) {
        log.info("Deleting classroom with id: {}", id);
        if (!classroomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classroom", "id", id);
        }
        classroomRepository.deleteById(id);
        log.info("Deleted classroom: {}", id);
    }

    @Override
    public void enrollStudent(Long classroomId, Long studentId) {
        log.info("Enrolling student {} in classroom {}", studentId, classroomId);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        // Check if enrollment already exists using composite key approach (more robust)
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroomId, studentId);
        boolean alreadyEnrolled = enrollmentRepository.existsById(enrollmentId);
        if (alreadyEnrolled) {
            log.warn("Student {} is already enrolled in classroom {}", studentId, classroomId);
            return;
        }

        // Create new enrollment with composite key for proper relationship handling
        ClassroomEnrollment enrollment = new ClassroomEnrollment();
        enrollment.setId(enrollmentId);
        enrollment.setClassroom(classroom);
        enrollment.setUser(student);
        enrollment.setStatus(ClassroomEnrollment.EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercentage(0.0);
        
        enrollmentRepository.save(enrollment);
        log.info("Successfully enrolled student {} in classroom {}", studentId, classroomId);
    }

    @Override
    public void unenrollStudent(Long classroomId, Long studentId) {
        log.info("Unenrolling student {} from classroom {}", studentId, classroomId);
        
        // Verify classroom and student exist
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        // Delete enrollment using composite key for proper relationship handling
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroomId, studentId);
        if (enrollmentRepository.existsById(enrollmentId)) {
            enrollmentRepository.deleteById(enrollmentId);
            log.info("Successfully unenrolled student {} from classroom {}", studentId, classroomId);
        } else {
            log.warn("Student {} was not enrolled in classroom {}", studentId, classroomId);
        }
    }

    @Override
    public Page<ClassroomDto> searchClassrooms(String name, Pageable pageable) {
        log.info("Searching classrooms with name containing: {}", name);
        return classroomRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(classroomMapper::toDto);
    }

    @Override
    public List<ClassroomDto> getClassroomsByCurrentTeacher() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting classrooms for current teacher: {}", currentUsername);
        
        User teacher = getCurrentUser();
        
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        List<ClassroomDto> result = classrooms.stream()
                .map(classroomMapper::toDto)
                .collect(Collectors.toList());
        
        log.info("Found {} classrooms for teacher {}", result.size(), currentUsername);
        return result;
    }

    @Override
    public List<ClassroomDto> getClassroomsByCurrentStudent() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting classrooms for current student: {}", currentUsername);
        
        User student = getCurrentUser();
        
        List<Classroom> classrooms = classroomRepository.findByStudents_Id(student.getId());
        if (classrooms.isEmpty()) {
            log.warn("No classrooms found for student {}. Checking enrollment table...", currentUsername);
            // Try alternative query using enrollment repository
            classrooms = enrollmentRepository.findByUserId(student.getId())
                    .stream()
                    .map(ClassroomEnrollment::getClassroom)
                    .collect(Collectors.toList());
        }
        
        List<ClassroomDto> result = classrooms.stream()
                .map(classroom -> {
                    ClassroomDto dto = classroomMapper.toDto(classroom);
                    dto.setProgressPercentage(calculateStudentProgress(student.getId(), classroom.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        
        log.info("Found {} classrooms for student {}", result.size(), currentUsername);
        return result;
    }

    @Override
    public List<UserDto> getStudentsInClassroom(Long classroomId) {
        log.info("Getting students in classroom: {}", classroomId);
        
        // Verify classroom exists
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        return enrollmentRepository.findByClassroomId(classroomId)
                .stream()
                .map(enrollment -> UserMapper.toDto(enrollment.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> getClassroomsByStudentId(Long studentId) {
        log.info("Getting classrooms for student: {}", studentId);
        
        // Verify student exists
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        List<Classroom> classrooms = classroomRepository.findClassroomsByStudentId(studentId);
        return classrooms.stream()
                .map(classroom -> {
                    ClassroomDto dto = classroomMapper.toDto(classroom);
                    dto.setProgressPercentage(calculateStudentProgress(studentId, classroom.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(currentUsername)
                .or(() -> userRepository.findByEmail(currentUsername))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username/email", currentUsername));
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private Double calculateStudentProgress(Long studentId, Long classroomId) {
        try {
            // Try to get overall progress first
            java.util.Optional<StudentProgress> overallProgress = studentProgressRepository
                .findOverallProgress(studentId, classroomId);
            
            if (overallProgress.isPresent()) {
                return overallProgress.get().getProgressPercentage().doubleValue();
            }
            
            // Fallback to assignment progress
            List<StudentProgress> assignmentProgress = studentProgressRepository
                .findByStudentIdAndClassroomIdAndProgressType(studentId, classroomId,
                    StudentProgress.ProgressType.ASSIGNMENT);
            
            if (!assignmentProgress.isEmpty()) {
                double averageProgress = assignmentProgress.stream()
                    .mapToDouble(progress -> progress.getProgressPercentage().doubleValue())
                    .average()
                    .orElse(0.0);
                return averageProgress;
            }
            
            // Return 0 if no progress data available
            return 0.0;
        } catch (Exception e) {
            log.warn("Error calculating progress for student {} in classroom {}: {}", 
                studentId, classroomId, e.getMessage());
            return 0.0;
        }
    }
}