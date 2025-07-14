package com.classroomapp.classroombackend.config.seed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@ExtendWith(MockitoExtension.class)
class DataFixUtilityTest {

    @Mock private UserRepository userRepository;
    @Mock private ClassroomRepository classroomRepository;
    @Mock private ClassroomEnrollmentRepository enrollmentRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private LectureRepository lectureRepository;

    @InjectMocks
    private DataFixUtility dataFixUtility;
    
    @BeforeEach
    void setUp() {
        // Default empty collections - using lenient to avoid unnecessary stubbing warnings
        lenient().when(submissionRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(enrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(userRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(assignmentRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(classroomRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(lectureRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(userRepository.findByRoleId(anyInt())).thenReturn(Collections.emptyList());
    }
    
    @Test
    void testFixOrphanedSubmissions_WithOrphanedData() {
        // Arrange
        Submission orphanedSubmission = new Submission();
        orphanedSubmission.setId(1L);
        orphanedSubmission.setAssignment(null); // Orphaned
        orphanedSubmission.setStudent(null);
        
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(orphanedSubmission));
        
        // Act
        int result = dataFixUtility.fixOrphanedSubmissions();
        
        // Assert
        assertEquals(1, result);
        verify(submissionRepository).delete(orphanedSubmission);
    }
    
    @Test
    void testFixOrphanedSubmissions_WithNoOrphanedData() {
        // Arrange
        Submission validSubmission = new Submission();
        validSubmission.setId(1L);
        validSubmission.setAssignment(new Assignment());
        validSubmission.setStudent(new User());
        
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(validSubmission));
        
        // Act
        int result = dataFixUtility.fixOrphanedSubmissions();
        
        // Assert
        assertEquals(0, result);
        verify(submissionRepository, never()).delete(any());
    }
    
    @Test
    void testFixOrphanedEnrollments_WithOrphanedData() {
        // Arrange
        ClassroomEnrollment orphanedEnrollment = new ClassroomEnrollment();
        orphanedEnrollment.setUser(null); // Orphaned
        orphanedEnrollment.setClassroom(null);
        
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(orphanedEnrollment));
        
        // Act
        int result = dataFixUtility.fixOrphanedEnrollments();
        
        // Assert
        assertEquals(1, result);
        verify(enrollmentRepository).delete(orphanedEnrollment);
    }
    
    @Test
    void testFixDuplicateUserEmails_WithDuplicates() {
        // Arrange
        User user1 = createUser(1L, "user1", "test@example.com");
        User user2 = createUser(2L, "user2", "test@example.com"); // Duplicate email
        User user3 = createUser(3L, "user3", "unique@example.com");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));
        
        // Act
        int result = dataFixUtility.fixDuplicateUserEmails();
        
        // Assert
        assertEquals(1, result); // Only one duplicate fixed
        verify(userRepository).save(user2); // Second user should be updated
        assertEquals("test.1@example.com", user2.getEmail()); // Email should be changed
    }
    
    @Test
    void testFixDuplicateUserEmails_WithNoDuplicates() {
        // Arrange
        User user1 = createUser(1L, "user1", "test1@example.com");
        User user2 = createUser(2L, "user2", "test2@example.com");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // Act
        int result = dataFixUtility.fixDuplicateUserEmails();
        
        // Assert
        assertEquals(0, result);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void testFixInvalidAssignmentPoints_WithInvalidPoints() {
        // Arrange
        Assignment assignment1 = new Assignment();
        assignment1.setId(1L);
        assignment1.setPoints(null); // Invalid
        
        Assignment assignment2 = new Assignment();
        assignment2.setId(2L);
        assignment2.setPoints(-10); // Invalid negative
        
        Assignment assignment3 = new Assignment();
        assignment3.setId(3L);
        assignment3.setPoints(100); // Valid
        
        when(assignmentRepository.findAll()).thenReturn(Arrays.asList(assignment1, assignment2, assignment3));
        
        // Act
        int result = dataFixUtility.fixInvalidAssignmentPoints();
        
        // Assert
        assertEquals(2, result);
        verify(assignmentRepository, times(2)).save(any(Assignment.class));
        assertEquals(Integer.valueOf(100), assignment1.getPoints());
        assertEquals(Integer.valueOf(100), assignment2.getPoints());
    }
    
    @Test
    void testFixClassroomTeacherReferences_WithMissingTeachers() {
        // Arrange
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setName("Test Classroom");
        classroom.setTeacher(null); // Missing teacher
        
        User teacher = createUser(1L, "teacher", "teacher@test.com");
        teacher.setRoleId(RoleConstants.TEACHER);
        
        when(classroomRepository.findAll()).thenReturn(Arrays.asList(classroom));
        when(userRepository.findByRoleId(2)).thenReturn(Arrays.asList(teacher));
        
        // Act
        int result = dataFixUtility.fixClassroomTeacherReferences();
        
        // Assert
        assertEquals(1, result);
        verify(classroomRepository).save(classroom);
        assertEquals(teacher, classroom.getTeacher());
    }
    
    @Test
    void testFixClassroomTeacherReferences_WithNoTeachersAvailable() {
        // Arrange
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setTeacher(null);
        
        when(classroomRepository.findAll()).thenReturn(Arrays.asList(classroom));
        when(userRepository.findByRoleId(2)).thenReturn(Collections.emptyList()); // No teachers
        
        // Act
        int result = dataFixUtility.fixClassroomTeacherReferences();
        
        // Assert
        assertEquals(0, result);
        verify(classroomRepository, never()).save(any());
    }
    
    @Test
    void testFixAllKnownIssues() {
        // Arrange - setup some issues to fix
        Submission orphanedSubmission = new Submission();
        orphanedSubmission.setId(1L);
        orphanedSubmission.setAssignment(null);
        
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(orphanedSubmission));
        
        // Act
        DataFixReport report = dataFixUtility.fixAllKnownIssues();
        
        // Assert
        assertNotNull(report);
        assertTrue(report.getTotalFixedItems() > 0);
        assertFalse(report.hasErrors());
    }
    
    @Test
    void testCleanupAllData() {
        // Act
        dataFixUtility.cleanupAllData();
        
        // Assert
        verify(submissionRepository).deleteAll();
        verify(assignmentRepository).deleteAll();
        verify(enrollmentRepository).deleteAll();
        verify(classroomRepository).deleteAll();
        verify(userRepository).deleteAll();
    }
    
    private User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName("Test User " + id);
        user.setRoleId(RoleConstants.STUDENT);
        return user;
    }
}
