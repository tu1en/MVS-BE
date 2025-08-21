package com.classroomapp.classroombackend.recruitment;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.impl.RecruitmentApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Week 1: Easy Recruitment Methods Unit Tests
 * 
 * These tests cover the simplest methods that are perfect for beginners:
 * - getAllApplications() - Basic list retrieval
 * - getApplication(Long id) - Single item retrieval  
 * - deleteApplication(Long id) - Simple deletion
 * 
 * Difficulty Level: ⭐⭐⭐⭐⭐ (Very Easy)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Week 1: Easy Recruitment Methods")
class RecruitmentGetApplicationTest {

    @Mock
    private RecruitmentApplicationRepository recruitmentRepo;
    
    @Mock
    private JobPositionRepository jobPositionRepo;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private RecruitmentApplicationServiceImpl recruitmentService;

    private JobPosition mockJobPosition;
    private RecruitmentApplication mockApplication;
    private RecruitmentApplicationDto mockApplicationDto;

    @BeforeEach
    void setUp() {
        // Setup simple mock data
        mockJobPosition = new JobPosition();
        mockJobPosition.setId(1L);
        mockJobPosition.setTitle("Math Teacher");

        mockApplication = new RecruitmentApplication();
        mockApplication.setId(1L);
        mockApplication.setJobPosition(mockJobPosition);
        mockApplication.setFullName("John Doe");
        mockApplication.setEmail("john@example.com");
        mockApplication.setPhoneNumber("0987654321");
        mockApplication.setAddress("123 Main Street");
        mockApplication.setStatus("PENDING");
        mockApplication.setCreatedAt(LocalDateTime.now());

        mockApplicationDto = new RecruitmentApplicationDto();
        mockApplicationDto.setId(1L);
        mockApplicationDto.setJobPositionId(1L);
        mockApplicationDto.setJobTitle("Math Teacher");
        mockApplicationDto.setFullName("John Doe");
        mockApplicationDto.setEmail("john@example.com");
        mockApplicationDto.setStatus("PENDING");
    }

    // ==================== TEST 1: getAllApplications() ====================
    
    @Test
    @DisplayName("✅ Test getAllApplications - Returns list of applications")
    void testGetAllApplications_ReturnsList() {
        // Setup - Mock repository to return a list
        when(recruitmentRepo.findAll()).thenReturn(Arrays.asList(mockApplication));

        // Run - Call the method
        List<RecruitmentApplicationDto> result = recruitmentService.getAllApplications();

        // Assert - Check the result
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 application");
        assertEquals("John Doe", result.get(0).getFullName(), "First application should be John Doe");
        
        // Verify - Check that repository method was called
        verify(recruitmentRepo).findAll();
    }

    @Test
    @DisplayName("✅ Test getAllApplications - Returns empty list when no data")
    void testGetAllApplications_EmptyList() {
        // Setup - Mock repository to return empty list
        when(recruitmentRepo.findAll()).thenReturn(Collections.emptyList());

        // Run - Call the method
        List<RecruitmentApplicationDto> result = recruitmentService.getAllApplications();

        // Assert - Check the result
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        
        // Verify - Check that repository method was called
        verify(recruitmentRepo).findAll();
    }

    @Test
    @DisplayName("✅ Test getAllApplications - Returns multiple applications")
    void testGetAllApplications_MultipleApplications() {
        // Setup - Create second application
        RecruitmentApplication secondApp = new RecruitmentApplication();
        secondApp.setId(2L);
        secondApp.setJobPosition(mockJobPosition);
        secondApp.setFullName("Jane Smith");
        secondApp.setEmail("jane@example.com");
        secondApp.setStatus("APPROVED");

        when(recruitmentRepo.findAll()).thenReturn(Arrays.asList(mockApplication, secondApp));

        // Run - Call the method
        List<RecruitmentApplicationDto> result = recruitmentService.getAllApplications();

        // Assert - Check the result
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 applications");
        assertEquals("John Doe", result.get(0).getFullName(), "First should be John Doe");
        assertEquals("Jane Smith", result.get(1).getFullName(), "Second should be Jane Smith");
    }

    // ==================== TEST 2: getApplication(Long id) ====================
    
    @Test
    @DisplayName("✅ Test getApplication - Returns application by ID")
    void testGetApplication_ReturnsApplication() {
        // Setup - Mock repository to return application
        when(recruitmentRepo.findById(1L)).thenReturn(Optional.of(mockApplication));

        // Run - Call the method
        RecruitmentApplicationDto result = recruitmentService.getApplication(1L);

        // Assert - Check the result
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "ID should match");
        assertEquals("John Doe", result.getFullName(), "Full name should match");
        assertEquals("john@example.com", result.getEmail(), "Email should match");
        assertEquals("Math Teacher", result.getJobTitle(), "Job title should match");
        
        // Verify - Check that repository method was called
        verify(recruitmentRepo).findById(1L);
    }

    @Test
    @DisplayName("❌ Test getApplication - Throws exception when ID not found")
    void testGetApplication_ThrowsExceptionWhenNotFound() {
        // Setup - Mock repository to return empty
        when(recruitmentRepo.findById(999L)).thenReturn(Optional.empty());

        // Run & Assert - Check that exception is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recruitmentService.getApplication(999L);
        });

        // Assert - Check exception message
        assertEquals("Không tìm thấy đơn ứng tuyển", exception.getMessage(), "Exception message should match");
        
        // Verify - Check that repository method was called
        verify(recruitmentRepo).findById(999L);
    }

    @Test
    @DisplayName("✅ Test getApplication - Returns application with different ID")
    void testGetApplication_DifferentId() {
        // Setup - Create application with ID 5
        RecruitmentApplication app5 = new RecruitmentApplication();
        app5.setId(5L);
        app5.setJobPosition(mockJobPosition);
        app5.setFullName("Bob Wilson");
        app5.setEmail("bob@example.com");
        app5.setStatus("REJECTED");

        when(recruitmentRepo.findById(5L)).thenReturn(Optional.of(app5));

        // Run - Call the method
        RecruitmentApplicationDto result = recruitmentService.getApplication(5L);

        // Assert - Check the result
        assertNotNull(result, "Result should not be null");
        assertEquals(5L, result.getId(), "ID should be 5");
        assertEquals("Bob Wilson", result.getFullName(), "Full name should be Bob Wilson");
        assertEquals("REJECTED", result.getStatus(), "Status should be REJECTED");
    }

    // ==================== TEST 3: deleteApplication(Long id) ====================
    
    @Test
    @DisplayName("✅ Test deleteApplication - Successfully deletes application")
    void testDeleteApplication_Success() {
        // Setup - Mock repository to return true for exists
        when(recruitmentRepo.existsById(1L)).thenReturn(true);

        // Run - Call the method
        recruitmentService.deleteApplication(1L);

        // Verify - Check that repository methods were called
        verify(recruitmentRepo).existsById(1L);
        verify(recruitmentRepo).deleteById(1L);
    }

    @Test
    @DisplayName("❌ Test deleteApplication - Throws exception when ID not found")
    void testDeleteApplication_ThrowsExceptionWhenNotFound() {
        // Setup - Mock repository to return false for exists
        when(recruitmentRepo.existsById(999L)).thenReturn(false);

        // Run & Assert - Check that exception is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recruitmentService.deleteApplication(999L);
        });

        // Assert - Check exception message
        assertEquals("Không tìm thấy đơn ứng tuyển", exception.getMessage(), "Exception message should match");
        
        // Verify - Check that repository method was called
        verify(recruitmentRepo).existsById(999L);
        verify(recruitmentRepo, never()).deleteById(any()); // Delete should never be called
    }

    @Test
    @DisplayName("✅ Test deleteApplication - Deletes application with different ID")
    void testDeleteApplication_DifferentId() {
        // Setup - Mock repository to return true for exists
        when(recruitmentRepo.existsById(10L)).thenReturn(true);

        // Run - Call the method
        recruitmentService.deleteApplication(10L);

        // Verify - Check that repository methods were called
        verify(recruitmentRepo).existsById(10L);
        verify(recruitmentRepo).deleteById(10L);
    }
}
