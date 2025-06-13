package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
// import com.classroomapp.classroombackend.model.assignmentmanagement.Submission; // Unused import
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.impl.ClassroomServiceImpl;
import com.classroomapp.classroombackend.util.ModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
// import java.util.List; // Unused import
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyLong; // Unused import
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseDetailsServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AssignmentRepository assignmentRepository;
    
    @Mock
    private SubmissionRepository submissionRepository;
    
    @Mock
    private ModelMapper modelMapper;
    
    @InjectMocks
    private ClassroomServiceImpl classroomService;
    
    private Classroom testClassroom;
    private User testTeacher;
    private User testStudent1;
    private User testStudent2;
    private Assignment testAssignment1;
    private Assignment testAssignment2;
    private Syllabus testSyllabus;
    private Schedule testSchedule;
    
    @BeforeEach
    void setUp() {
        // Create test teacher
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setUsername("teacher1");
        testTeacher.setEmail("teacher@test.com");
        testTeacher.setFullName("Test Teacher");
        testTeacher.setRoleId(2); // Teacher role
        
        // Create test students
        testStudent1 = new User();
        testStudent1.setId(2L);
        testStudent1.setUsername("student1");
        testStudent1.setEmail("student1@test.com");
        testStudent1.setFullName("Test Student 1");
        testStudent1.setRoleId(3); // Student role
        
        testStudent2 = new User();
        testStudent2.setId(3L);
        testStudent2.setUsername("student2");
        testStudent2.setEmail("student2@test.com");
        testStudent2.setFullName("Test Student 2");
        testStudent2.setRoleId(3); // Student role
        
        // Create test classroom
        testClassroom = new Classroom();
        testClassroom.setId(1L);
        testClassroom.setName("Introduction to Java");
        testClassroom.setDescription("Learn Java programming fundamentals");
        testClassroom.setSection("CS101-A");
        testClassroom.setSubject("Computer Science");
        testClassroom.setTeacher(testTeacher);
        
        Set<User> students = new HashSet<>();
        students.add(testStudent1);
        students.add(testStudent2);
        testClassroom.setStudents(students);
        
        // Create test syllabus
        testSyllabus = new Syllabus();
        testSyllabus.setId(1L);
        testSyllabus.setTitle("Java Programming Course");
        testSyllabus.setContent("Comprehensive Java programming course");
        testSyllabus.setLearningObjectives("Understand OOP concepts");
        testSyllabus.setRequiredMaterials("Java textbook");
        testSyllabus.setGradingCriteria("Assignments 50%, Exams 50%");
        testSyllabus.setClassroom(testClassroom);
        testClassroom.setSyllabus(testSyllabus);
        
        // Create test schedule
        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        testSchedule.setStartTime(LocalTime.of(9, 0));
        testSchedule.setEndTime(LocalTime.of(10, 30));
        testSchedule.setLocation("Room 101");
        testSchedule.setNotes("Regular lecture");
        testSchedule.setRecurring(true);
        testSchedule.setClassroom(testClassroom);
        testClassroom.setSchedules(Arrays.asList(testSchedule));
        
        // Create test assignments
        testAssignment1 = new Assignment();
        testAssignment1.setId(1L);
        testAssignment1.setTitle("Assignment 1: Variables");
        testAssignment1.setDescription("Practice with Java variables");
        testAssignment1.setDueDate(LocalDateTime.now().plusDays(7)); // Active assignment
        testAssignment1.setPoints(100);
        testAssignment1.setClassroom(testClassroom);
        
        testAssignment2 = new Assignment();
        testAssignment2.setId(2L);
        testAssignment2.setTitle("Assignment 2: Methods");
        testAssignment2.setDescription("Practice with Java methods");
        testAssignment2.setDueDate(LocalDateTime.now().minusDays(1)); // Completed assignment
        testAssignment2.setPoints(100);
        testAssignment2.setClassroom(testClassroom);
    }
    
    @Test
    void testGetCourseDetails_Success() {
        // Arrange
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        when(assignmentRepository.findByClassroom(testClassroom))
            .thenReturn(Arrays.asList(testAssignment1, testAssignment2));
        when(submissionRepository.countByAssignment(any(Assignment.class))).thenReturn(2L);
        when(submissionRepository.countByAssignmentAndScoreIsNotNull(any(Assignment.class))).thenReturn(1L);
        when(submissionRepository.findByAssignmentAndScoreIsNotNull(any(Assignment.class)))
            .thenReturn(Arrays.asList()); // Empty for simplicity
        
        // Mock ModelMapper returns
        when(modelMapper.MapToUserDto(any(User.class))).thenReturn(any());
        when(modelMapper.MapToSyllabusDto(any(Syllabus.class))).thenReturn(any());
        when(modelMapper.MapToScheduleDto(any(Schedule.class))).thenReturn(any());
        when(modelMapper.MapToAssignmentDto(any(Assignment.class))).thenReturn(any());
        
        // Act
        CourseDetailsDto result = classroomService.GetCourseDetails(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Introduction to Java", result.getName());
        assertEquals("Learn Java programming fundamentals", result.getDescription());
        assertEquals("CS101-A", result.getSection());
        assertEquals("Computer Science", result.getSubject());
        assertEquals(2, result.getTotalStudents());
        assertEquals(2, result.getTotalAssignments());
        assertEquals(1, result.getActiveAssignments());
        
        // Verify statistics are calculated
        assertNotNull(result.getStatistics());
        assertEquals(2, result.getStatistics().getTotalStudents());
        assertEquals(2, result.getStatistics().getTotalAssignments());
        assertEquals(1, result.getStatistics().getActiveAssignments());
        assertEquals(1, result.getStatistics().getCompletedAssignments());
        
        // Verify repository calls
        verify(classroomRepository).findById(1L);
        verify(assignmentRepository).findByClassroom(testClassroom);
        verify(submissionRepository, times(2)).countByAssignment(any(Assignment.class));
        verify(submissionRepository, times(2)).countByAssignmentAndScoreIsNotNull(any(Assignment.class));
    }
    
    @Test
    void testGetCourseDetails_ClassroomNotFound() {
        // Arrange
        when(classroomRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            classroomService.GetCourseDetails(999L);
        });
        
        verify(classroomRepository).findById(999L);
        verifyNoInteractions(assignmentRepository, submissionRepository);
    }
    
    @Test
    void testGetCourseDetails_NoAssignments() {
        // Arrange
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        when(assignmentRepository.findByClassroom(testClassroom)).thenReturn(Arrays.asList());
        
        // Mock ModelMapper returns
        when(modelMapper.MapToUserDto(any(User.class))).thenReturn(any());
        when(modelMapper.MapToSyllabusDto(any(Syllabus.class))).thenReturn(any());
        when(modelMapper.MapToScheduleDto(any(Schedule.class))).thenReturn(any());
        
        // Act
        CourseDetailsDto result = classroomService.GetCourseDetails(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalAssignments());
        assertEquals(0, result.getActiveAssignments());
        assertEquals(0, result.getStatistics().getTotalAssignments());
        assertEquals(0, result.getStatistics().getActiveAssignments());
        assertEquals(0, result.getStatistics().getCompletedAssignments());
        assertEquals(0.0, result.getStatistics().getCompletionRate());
    }
    
    @Test
    void testGetCourseDetails_NoStudents() {
        // Arrange
        testClassroom.setStudents(new HashSet<>());
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        when(assignmentRepository.findByClassroom(testClassroom)).thenReturn(Arrays.asList());
        
        // Mock ModelMapper returns
        when(modelMapper.MapToUserDto(any(User.class))).thenReturn(any());
        when(modelMapper.MapToSyllabusDto(any(Syllabus.class))).thenReturn(any());
        when(modelMapper.MapToScheduleDto(any(Schedule.class))).thenReturn(any());
        
        // Act
        CourseDetailsDto result = classroomService.GetCourseDetails(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalStudents());
        assertEquals(0, result.getStatistics().getTotalStudents());
        assertEquals(0.0, result.getStatistics().getCompletionRate());
    }
}
