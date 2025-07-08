package com.classroomapp.classroombackend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.service.ClassroomService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final StudentProgressRepository studentProgressRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ClassroomDto> getAllClassrooms() {
        return classroomRepository.findAll().stream()
                .map(classroom -> modelMapper.map(classroom, ClassroomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ClassroomDto getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found with id: " + id));
        return modelMapper.map(classroom, ClassroomDto.class);
    }

    @Override
    public ClassroomDto GetClassroomById(Long id) {
        return getClassroomById(id);
    }

    @Override
    public ClassroomDetailsDto createClassroom(CreateClassroomDto dto) {
        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + dto.getTeacherId()));
        Classroom classroom = new Classroom();
        classroom.setName(dto.getName());
        classroom.setDescription(dto.getDescription());
        classroom.setTeacher(teacher);
        classroom.setCourseId(dto.getCourseId());
        Classroom savedClassroom = classroomRepository.save(classroom);
        return modelMapper.map(savedClassroom, ClassroomDetailsDto.class);
    }

    @Override
    @Transactional
    public ClassroomDto UpdateClassroom(Long id, UpdateClassroomDto updateClassroomDto, UserDetails userDetails) {
        // 1. Tìm classroom
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

        if (!(userDetails instanceof CustomUserDetails)) {
            throw new InsufficientAuthenticationException("User details not of expected type");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User currentUser = customUserDetails.getUser();
            
        boolean isAdminOrManager = customUserDetails.getAuthorities().stream()
            .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || role.getAuthority().equals("ROLE_MANAGER"));
        
        // Người dùng phải là Admin, Manager, hoặc là giáo viên của chính lớp đó
        if (!isAdminOrManager && (classroom.getTeacher() == null || !classroom.getTeacher().getId().equals(currentUser.getId()))) {
            throw new BusinessLogicException("You are not authorized to update this classroom.");
        }
    
        // 3. Cập nhật các trường được phép
        classroom.setName(updateClassroomDto.getName());
        classroom.setDescription(updateClassroomDto.getDescription());
    
        Classroom savedClassroom = classroomRepository.save(classroom);
        
        return convertToDto(savedClassroom);
    }
    
    @Override
    public void DeleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }

    @Override
    public List<ClassroomDto> GetClassroomsByTeacher(Long teacherId) {
        // Try fast path: query by teacher ID directly (avoids User lookup)
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacherId);

        if (classrooms.isEmpty()) {
            // Fallback: look up User entity; might not exist if ID mismatch
            User teacher = userRepository.findById(teacherId).orElse(null);
            if (teacher != null) {
                classrooms = classroomRepository.findByTeacher(teacher);
            }
        }

        // Map to DTOs – could be empty list if still nothing found
        return classrooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> GetClassroomsByStudent(Long studentId) {
         return classroomEnrollmentRepository.findById_UserId(studentId).stream()
                .map(ClassroomEnrollment::getClassroom)
                .map(classroom -> {
                    ClassroomDto dto = modelMapper.map(classroom, ClassroomDto.class);
                    // Calculate progress percentage for this student in this classroom
                    dto.setProgressPercentage(calculateStudentProgress(studentId, classroom.getId()));
                    // Ensure teacher name is set
                    if (classroom.getTeacher() != null) {
                        dto.setTeacherName(classroom.getTeacher().getFullName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void EnrollStudent(Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found with id: " + classroomId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));
        
        // Check if student is already enrolled
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroomId, studentId);
        boolean alreadyEnrolled = classroomEnrollmentRepository.existsById(enrollmentId);
        if (alreadyEnrolled) {
            log.info("Student {} is already enrolled in classroom {}", studentId, classroomId);
            return;
        }
        
        // Create enrollment
        ClassroomEnrollment enrollment = new ClassroomEnrollment();
        enrollment.setId(enrollmentId);
        enrollment.setClassroom(classroom);
        enrollment.setUser(student);
        classroomEnrollmentRepository.save(enrollment);
        log.info("Student {} successfully enrolled in classroom {}", studentId, classroomId);
    }

    @Override
    public void UnenrollStudent(Long classroomId, Long studentId) {
        // Create the composite ID
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroomId, studentId);
        
        // Check if enrollment exists
        if (!classroomEnrollmentRepository.existsById(enrollmentId)) {
            log.warn("Student {} is not enrolled in classroom {}", studentId, classroomId);
            return;
        }
        
        // Delete the enrollment
        classroomEnrollmentRepository.deleteById(enrollmentId);
        log.info("Student {} successfully unenrolled from classroom {}", studentId, classroomId);
    }

    @Override
    public List<ClassroomDto> SearchClassroomsByName(String name) {
        return classroomRepository.findByNameContainingIgnoreCase(name).stream()
                .map(classroom -> modelMapper.map(classroom, ClassroomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> GetClassroomsBySubject(String subject) {
        // Implementation needed
        return new ArrayList<>();
    }

    @Override
    public CourseDetailsDto GetCourseDetails(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found with id: " + classroomId));
        return modelMapper.map(classroom, CourseDetailsDto.class);
    }

    @Override
    public List<User> getStudentsInClassroom(Long classroomId) {
        return classroomEnrollmentRepository.findById_ClassroomId(classroomId).stream()
            .map(ClassroomEnrollment::getUser)
            .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> GetClassroomsByCurrentTeacher() {
        try {
            log.info("GetClassroomsByCurrentTeacher: Getting classrooms for current teacher");
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails)principal).getUsername();
                log.info("GetClassroomsByCurrentTeacher: Found username from UserDetails: {}", username);
            } else {
                username = principal.toString();
                log.info("GetClassroomsByCurrentTeacher: Found username from toString: {}", username);
            }
            
            // Try to find user by username first, then by email
            User teacher = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                    .orElse(null));
                    
            if (teacher == null) {
                log.warn("GetClassroomsByCurrentTeacher: Teacher not found with username/email: {}", username);
                return new ArrayList<>();
            }
            
            log.info("GetClassroomsByCurrentTeacher: Found teacher: {} (ID: {})", teacher.getFullName(), teacher.getId());
            
            List<ClassroomDto> classrooms = classroomRepository.findByTeacher(teacher).stream()
                .map(classroom -> modelMapper.map(classroom, ClassroomDto.class))
                .collect(Collectors.toList());
                
            log.info("GetClassroomsByCurrentTeacher: Found {} classrooms for teacher", classrooms.size());
            
            return classrooms;
        } catch (Exception e) {
            log.error("GetClassroomsByCurrentTeacher: Error getting classrooms for current teacher", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ClassroomDto> getClassroomsByCurrentStudent() {
        log.info("getClassroomsByCurrentStudent: Getting classrooms for current student");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new InsufficientAuthenticationException("User is not properly authenticated");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long studentId = userDetails.getId();
        log.info("getClassroomsByCurrentStudent: authenticated user id: {}", studentId);

        return GetClassroomsByStudent(studentId);
    }

    @Override
    public ClassroomDetailsDto findClassroomDetailsById(Long classroomId) {
        Classroom classroom = classroomRepository.findDetailsById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + classroomId));
        return convertToClassroomDetailsDto(classroom);
    }

    private ClassroomDetailsDto convertToClassroomDetailsDto(Classroom classroom) {
        ClassroomDetailsDto detailsDto = new ClassroomDetailsDto();
        detailsDto.setId(classroom.getId());
        detailsDto.setName(classroom.getName());
        detailsDto.setDescription(classroom.getDescription());

        if (classroom.getTeacher() != null) {
            detailsDto.setTeacher(modelMapper.map(classroom.getTeacher(), UserDetailsDto.class));
        }

        if (classroom.getLectures() != null) {
            List<LectureDto> lectureDtos = classroom.getLectures().stream()
                    .map(lecture -> modelMapper.map(lecture, LectureDto.class))
                    .collect(Collectors.toList());
            detailsDto.setLectures(lectureDtos);
        }

        // Assuming CourseDetailsDto can be mapped from Classroom
        detailsDto.setCourse(modelMapper.map(classroom, CourseDetailsDto.class));

        return detailsDto;
    }
    
    private ClassroomDto convertToDto(Classroom classroom) {
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setSubject(classroom.getSubject());
        dto.setSection(classroom.getSection());
        if (classroom.getTeacher() != null) {
            dto.setTeacherName(classroom.getTeacher().getFullName());
        }
        // We explicitly avoid mapping the syllabus here to prevent circular dependency issues
        // If syllabus summary is needed, it should be a separate lightweight DTO.
        return dto;
    }
    
    /**
     * Calculate student progress percentage for a specific classroom
     * @param studentId The student ID
     * @param classroomId The classroom ID
     * @return Progress percentage as Double (0.0 to 100.0)
     */
    private Double calculateStudentProgress(Long studentId, Long classroomId) {
        try {
            // First, try to find overall progress record
            java.util.Optional<StudentProgress> overallProgress = studentProgressRepository
                .findOverallProgress(studentId, classroomId);
            
            if (overallProgress.isPresent()) {
                return overallProgress.get().getProgressPercentage().doubleValue();
            }
            
            // If no overall progress, calculate based on assignment completion
            List<StudentProgress> assignmentProgress = studentProgressRepository
                .findByStudentIdAndClassroomIdAndProgressType(studentId, classroomId,
                    StudentProgress.ProgressType.ASSIGNMENT);
            
            if (!assignmentProgress.isEmpty()) {
                // Calculate average progress from assignments
                double averageProgress = assignmentProgress.stream()
                    .mapToDouble(progress -> progress.getProgressPercentage().doubleValue())
                    .average()
                    .orElse(0.0);
                return averageProgress;
            }
            
            // If no progress records exist, return 0%
            return 0.0;
            
        } catch (Exception e) {
            log.warn("Error calculating progress for student {} in classroom {}: {}", 
                studentId, classroomId, e.getMessage());
            return 0.0;
        }
    }
}