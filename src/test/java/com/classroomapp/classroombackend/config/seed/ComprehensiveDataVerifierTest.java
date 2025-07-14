package com.classroomapp.classroombackend.config.seed;

import static org.junit.jupiter.api.Assertions.*;
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
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@ExtendWith(MockitoExtension.class)
class ComprehensiveDataVerifierTest {
    
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private ClassroomRepository classroomRepository;
    @Mock private ClassroomEnrollmentRepository enrollmentRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private LectureRepository lectureRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    
    @InjectMocks
    private ComprehensiveDataVerifier dataVerifier;
    
    @BeforeEach
    void setUp() {
        // Setup default mock behaviors
        when(userRepository.count()).thenReturn(10L);
        when(courseRepository.count()).thenReturn(5L);
        when(classroomRepository.count()).thenReturn(3L);
    }
    
    @Test
    void testRunComprehensiveVerification_WithHealthyData() {
        // Arrange
        setupHealthyData();
        
        // Act
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        
        // Assert
        assertNotNull(report);
        assertFalse(report.hasCriticalIssues());
        assertEquals(0, report.getTotalIssues());
    }
    
    @Test
    void testRunComprehensiveVerification_WithNoUsers() {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        
        // Act
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        
        // Assert
        assertNotNull(report);
        assertTrue(report.hasCriticalIssues());
        assertTrue(report.getTotalIssues() > 0);
        
        // Should have USER_EMPTY critical issue
        boolean hasUserEmptyIssue = report.getCriticalIssuesList().stream()
            .anyMatch(issue -> "USER_EMPTY".equals(issue.getCode()));
        assertTrue(hasUserEmptyIssue);
    }
    
    @Test
    void testRunComprehensiveVerification_WithOrphanedSubmissions() {
        // Arrange
        setupHealthyData();
        
        Submission orphanedSubmission = new Submission();
        orphanedSubmission.setId(1L);
        orphanedSubmission.setAssignment(null); // Orphaned
        orphanedSubmission.setStudent(null);
        
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(orphanedSubmission));
        
        // Act
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        
        // Assert
        assertNotNull(report);
        assertTrue(report.hasCriticalIssues());
        
        // Should have ORPHANED_SUBMISSIONS critical issue
        boolean hasOrphanedSubmissionsIssue = report.getCriticalIssuesList().stream()
            .anyMatch(issue -> "ORPHANED_SUBMISSIONS".equals(issue.getCode()));
        assertTrue(hasOrphanedSubmissionsIssue);
    }
    
    @Test
    void testRunComprehensiveVerification_WithDuplicateEmails() {
        // Arrange
        setupHealthyData();
        
        User user1 = createUser(1L, "user1", "test@example.com", RoleConstants.STUDENT);
        User user2 = createUser(2L, "user2", "test@example.com", RoleConstants.STUDENT); // Duplicate email
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // Act
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        
        // Assert
        assertNotNull(report);
        assertTrue(report.hasCriticalIssues());
        
        // Should have DUPLICATE_USER_EMAIL critical issue
        boolean hasDuplicateEmailIssue = report.getCriticalIssuesList().stream()
            .anyMatch(issue -> "DUPLICATE_USER_EMAIL".equals(issue.getCode()));
        assertTrue(hasDuplicateEmailIssue);
    }
    
    @Test
    void testRunComprehensiveVerification_WithClassroomWithoutTeacher() {
        // Arrange
        setupHealthyData();
        
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setName("Test Classroom");
        classroom.setTeacher(null); // No teacher
        
        when(classroomRepository.findAll()).thenReturn(Arrays.asList(classroom));
        
        // Act
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        
        // Assert
        assertNotNull(report);
        assertTrue(report.hasCriticalIssues());
        
        // Should have CLASSROOM_NO_TEACHER critical issue
        boolean hasNoTeacherIssue = report.getCriticalIssuesList().stream()
            .anyMatch(issue -> "CLASSROOM_NO_TEACHER".equals(issue.getCode()));
        assertTrue(hasNoTeacherIssue);
    }
    
    @Test
    void testRunComprehensiveVerification_WithInvalidAssignmentPoints() {
        // Arrange
        setupHealthyData();
        
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Test Assignment");
        assignment.setPoints(-10); // Invalid negative points
        
        when(assignmentRepository.findAll()).thenReturn(Arrays.asList(assignment));
        
        // Act
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        
        // Assert
        assertNotNull(report);
        assertTrue(report.hasWarningIssues());
        
        // Should have ASSIGNMENT_INVALID_POINTS warning issue
        boolean hasInvalidPointsIssue = report.getWarningIssuesList().stream()
            .anyMatch(issue -> "ASSIGNMENT_INVALID_POINTS".equals(issue.getCode()));
        assertTrue(hasInvalidPointsIssue);
    }
    
    private void setupHealthyData() {
        // Setup healthy users with all required roles
        User student = createUser(1L, "student", "student@test.com", RoleConstants.STUDENT);
        User teacher = createUser(2L, "teacher", "teacher@test.com", RoleConstants.TEACHER);
        User admin = createUser(3L, "admin", "admin@test.com", RoleConstants.ADMIN);
        when(userRepository.findAll()).thenReturn(Arrays.asList(student, teacher, admin));
        
        // Setup healthy classroom
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setName("Test Classroom");
        classroom.setTeacher(teacher);
        when(classroomRepository.findAll()).thenReturn(Arrays.asList(classroom));
        
        // Setup healthy enrollment
        ClassroomEnrollment enrollment = new ClassroomEnrollment();
        enrollment.setUser(student);
        enrollment.setClassroom(classroom);
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(enrollment));
        
        // Setup healthy assignment
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Test Assignment");
        assignment.setPoints(100);
        assignment.setClassroom(classroom); // Fix: assign classroom to assignment
        assignment.setDueDate(java.time.LocalDateTime.now().plusDays(7)); // Fix: add due date
        when(assignmentRepository.findAll()).thenReturn(Arrays.asList(assignment));
        
        // Setup healthy submission
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(submission));
        
        // Empty collections for other repositories
        when(scheduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(lectureRepository.findAll()).thenReturn(Collections.emptyList());
    }
    
    private User createUser(Long id, String username, String email, int roleId) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRoleId(roleId);
        user.setFullName("Test User " + id);
        user.setPassword("password");
        user.setStatus("active");
        return user;
    }
}
