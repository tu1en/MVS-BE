package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.util.ModelMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
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
        log.info("Created new classroom: {} with ID: {}", savedClassroom.getName(), savedClassroom.getId());
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
        log.info("Updated classroom with ID: {}", updatedClassroom.getId());
        return modelMapper.MapToClassroomDto(updatedClassroom);
    }

    @Override
    @Transactional
    public void DeleteClassroom(Long id) {
        Classroom classroom = FindClassroomById(id);
        classroomRepository.delete(classroom);
        log.info("Deleted classroom with ID: {}", id);
    }    @Override
    @Cacheable(value = "classroomsByTeacher", key = "#teacherId")
    public List<ClassroomDto> GetClassroomsByTeacher(Long teacherId) {
        // Use optimized query to fetch classrooms with students in one query
        List<Classroom> classrooms = classroomRepository.findByTeacherIdWithStudents(teacherId);
        log.info("Found {} classrooms for teacher ID: {}", classrooms.size(), teacherId);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassroomDto> GetClassroomsByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        List<Classroom> classrooms = classroomRepository.findByStudentsContaining(student);
        log.info("Found {} classrooms for student ID: {}", classrooms.size(), studentId);
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
        log.info("Enrolled student ID: {} in classroom ID: {}", studentId, classroomId);
    }

    @Override
    @Transactional
    public void UnenrollStudent(Long classroomId, Long studentId) {
        Classroom classroom = FindClassroomById(classroomId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        if (classroom.getStudents().remove(student)) {
            classroomRepository.save(classroom);
            log.info("Unenrolled student ID: {} from classroom ID: {}", studentId, classroomId);
        } else {
            log.warn("Student ID: {} was not enrolled in classroom ID: {}", studentId, classroomId);
            throw new IllegalArgumentException("Student is not enrolled in this classroom");
        }
    }

    @Override
    public List<ClassroomDto> SearchClassroomsByName(String name) {
        List<Classroom> classrooms = classroomRepository.findByNameContainingIgnoreCase(name);
        log.info("Found {} classrooms matching name: {}", classrooms.size(), name);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }    @Override
    public List<ClassroomDto> GetClassroomsBySubject(String subject) {
        List<Classroom> classrooms = classroomRepository.findBySubject(subject);
        log.info("Found {} classrooms for subject: {}", classrooms.size(), subject);
        return classrooms.stream()
                .map(modelMapper::MapToClassroomDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDetailsDto GetCourseDetails(Long classroomId) {
        Classroom classroom = FindClassroomById(classroomId);
        
        // Map basic classroom info
        CourseDetailsDto courseDetails = new CourseDetailsDto();
        courseDetails.setId(classroom.getId());
        courseDetails.setName(classroom.getName());
        courseDetails.setDescription(classroom.getDescription());
        courseDetails.setSection(classroom.getSection());
        courseDetails.setSubject(classroom.getSubject());
        
        // Map teacher info
        courseDetails.setTeacher(modelMapper.MapToUserDto(classroom.getTeacher()));
        
        // Map students info
        List<UserDto> students = classroom.getStudents().stream()
                .map(modelMapper::MapToUserDto)
                .collect(Collectors.toList());
        courseDetails.setStudents(students);
        courseDetails.setTotalStudents(students.size());
        
        // Map syllabus if exists
        if (classroom.getSyllabus() != null) {
            courseDetails.setSyllabus(modelMapper.MapToSyllabusDto(classroom.getSyllabus()));
        }
        
        // Map schedules - Convert to compatible ScheduleDto
        List<com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto> scheduleDtos = new ArrayList<>();
        if (classroom.getSchedules() != null && !classroom.getSchedules().isEmpty()) {
            // Classroom.getSchedules() returns List<com.classroomapp.classroombackend.model.Schedule>
            // But CourseDetailsDto expects List<com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto>
            // Need to convert manually since model types are incompatible
            classroom.getSchedules().forEach(schedule -> {
                // Create compatible ScheduleDto manually
                com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto dto = 
                    new com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto();
                
                dto.setId(schedule.getId());
                
                // Convert Integer dayOfWeek to DayOfWeek enum
                // 0=Monday, 1=Tuesday, 2=Wednesday, 3=Thursday, 4=Friday, 5=Saturday, 6=Sunday
                Integer day = schedule.getDayOfWeek();
                if (day != null) {
                    switch (day) {
                        case 0: dto.setDayOfWeek(java.time.DayOfWeek.MONDAY); break;
                        case 1: dto.setDayOfWeek(java.time.DayOfWeek.TUESDAY); break;
                        case 2: dto.setDayOfWeek(java.time.DayOfWeek.WEDNESDAY); break;
                        case 3: dto.setDayOfWeek(java.time.DayOfWeek.THURSDAY); break;
                        case 4: dto.setDayOfWeek(java.time.DayOfWeek.FRIDAY); break;
                        case 5: dto.setDayOfWeek(java.time.DayOfWeek.SATURDAY); break;
                        case 6: dto.setDayOfWeek(java.time.DayOfWeek.SUNDAY); break;
                        default: dto.setDayOfWeek(java.time.DayOfWeek.MONDAY);
                    }
                }
                
                dto.setStartTime(schedule.getStartTime());
                dto.setEndTime(schedule.getEndTime());
                dto.setLocation(schedule.getRoom()); // Map room to location
                dto.setNotes(schedule.getSubject()); // Map subject as notes
                dto.setRecurring(true); // Default to true
                
                if (schedule.getClassroom() != null) {
                    dto.setClassroomId(schedule.getClassroom().getId());
                }
                
                scheduleDtos.add(dto);
            });
        }
        courseDetails.setSchedules(scheduleDtos);
        
        // Get assignments for this classroom
        List<Assignment> assignments = assignmentRepository.findByClassroom(classroom);
        List<AssignmentDto> assignmentDtos = assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
        courseDetails.setAssignments(assignmentDtos);
        courseDetails.setTotalAssignments(assignments.size());
        
        // Count active assignments (due date in future)
        long activeAssignments = assignments.stream()
                .filter(assignment -> assignment.getDueDate().isAfter(LocalDateTime.now()))
                .count();
        courseDetails.setActiveAssignments((int) activeAssignments);
        
        // Calculate statistics
        CourseDetailsDto.CourseStatistics statistics = calculateCourseStatistics(classroom, assignments);
        courseDetails.setStatistics(statistics);
        
        return courseDetails;
    }
    
    private CourseDetailsDto.CourseStatistics calculateCourseStatistics(Classroom classroom, List<Assignment> assignments) {
        CourseDetailsDto.CourseStatistics stats = new CourseDetailsDto.CourseStatistics();
        
        stats.setTotalStudents(classroom.getStudents().size());
        stats.setTotalAssignments(assignments.size());
        
        // Count active and completed assignments
        LocalDateTime now = LocalDateTime.now();
        long activeCount = assignments.stream()
                .filter(assignment -> assignment.getDueDate().isAfter(now))
                .count();
        long completedCount = assignments.stream()
                .filter(assignment -> assignment.getDueDate().isBefore(now))
                .count();
        
        stats.setActiveAssignments((int) activeCount);
        stats.setCompletedAssignments((int) completedCount);
        
        // Calculate submission statistics
        int totalSubmissions = 0;
        int gradedSubmissions = 0;
        double totalScore = 0.0;
        int scoredSubmissions = 0;
        
        for (Assignment assignment : assignments) {
            long submissionCount = submissionRepository.countByAssignment(assignment);
            totalSubmissions += submissionCount;
            
            long gradedCount = submissionRepository.countByAssignmentAndScoreIsNotNull(assignment);
            gradedSubmissions += gradedCount;
            
            // Calculate average score for this assignment
            List<com.classroomapp.classroombackend.model.assignmentmanagement.Submission> gradedSubs = 
                submissionRepository.findByAssignmentAndScoreIsNotNull(assignment);
            
            for (var submission : gradedSubs) {
                if (submission.getScore() != null) {
                    totalScore += submission.getScore();
                    scoredSubmissions++;
                }
            }
        }
        
        stats.setTotalSubmissions(totalSubmissions);
        stats.setGradedSubmissions(gradedSubmissions);
        
        // Calculate average grade
        if (scoredSubmissions > 0) {
            stats.setAverageGrade(totalScore / scoredSubmissions);
        } else {
            stats.setAverageGrade(0.0);
        }
        
        // Calculate completion rate
        if (assignments.size() > 0 && classroom.getStudents().size() > 0) {
            int expectedSubmissions = assignments.size() * classroom.getStudents().size();
            stats.setCompletionRate((double) totalSubmissions / expectedSubmissions * 100);
        } else {
            stats.setCompletionRate(0.0);
        }
        
        return stats;
    }
    
    // Helper method to find classroom by ID or throw exception
    private Classroom FindClassroomById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));
    }
}