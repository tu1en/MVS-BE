package com.classroomapp.classroombackend;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.service.impl.RecruitmentApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RecruitmentApplicationServiceImplValidationTest {
    
    @Mock
    private RecruitmentApplicationRepository recruitmentRepo;
    @Mock
    private JobPositionRepository jobPositionRepo;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private RecruitmentApplicationServiceImpl recruitmentService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testValidApplication() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        
        MockMultipartFile cvFile = new MockMultipartFile(
            "cv", 
            "test-cv.pdf", 
            "application/pdf", 
            "test cv content".getBytes()
        );
        
        JobPosition jobPosition = new JobPosition();
        jobPosition.setId(jobPositionId);
        jobPosition.setTitle("Giáo viên Toán");
        
        when(jobPositionRepo.findById(jobPositionId)).thenReturn(Optional.of(jobPosition));
        FileUploadResponse mockResponse = new FileUploadResponse();
        mockResponse.setFileName("test-cv.pdf");
        mockResponse.setFileUrl("https://example.com/cv.pdf");
        mockResponse.setFileType("application/pdf");
        mockResponse.setSize(1024L);
        when(fileStorageService.save(any(), anyString())).thenReturn(mockResponse);
        when(recruitmentRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        RecruitmentApplicationDto result = recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, cvFile);
        
        // Assert
        assertNotNull(result);
        assertEquals(jobPositionId, result.getJobPositionId());
        assertEquals("Giáo viên Toán", result.getJobTitle());
        assertEquals(fullName, result.getFullName());
        assertEquals(email, result.getEmail());
        assertEquals(phoneNumber, result.getPhoneNumber());
        assertEquals(address, result.getAddress());
        assertEquals("PENDING", result.getStatus());
    }
    
    @Test
    void testInvalidApplication_NullJobPositionId() {
        // Arrange
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(null, fullName, email, phoneNumber, address, cvFile));
        assertEquals("Job position ID is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_NullFullName() {
        // Arrange
        Long jobPositionId = 1L;
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, null, email, phoneNumber, address, cvFile));
        assertEquals("Full name is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_EmptyFullName() {
        // Arrange
        Long jobPositionId = 1L;
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, "", email, phoneNumber, address, cvFile));
        assertEquals("Full name is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_WhitespaceFullName() {
        // Arrange
        Long jobPositionId = 1L;
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, "   ", email, phoneNumber, address, cvFile));
        assertEquals("Full name is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_NullEmail() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, null, phoneNumber, address, cvFile));
        assertEquals("Email is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_EmptyEmail() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, "", phoneNumber, address, cvFile));
        assertEquals("Email is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_NullPhoneNumber() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, null, address, cvFile));
        assertEquals("Phone number is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_EmptyPhoneNumber() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, "", address, cvFile));
        assertEquals("Phone number is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_NullAddress() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, null, cvFile));
        assertEquals("Address is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_EmptyAddress() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, "", cvFile));
        assertEquals("Address is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_NullCvFile() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, null));
        assertEquals("CV file is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_EmptyCvFile() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", new byte[0]);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, cvFile));
        assertEquals("CV file is required", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_LargeCvFile() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        byte[] largeFile = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", largeFile);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, cvFile));
        assertEquals("CV file size must be less than 10MB", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_NonPdfFile() {
        // Arrange
        Long jobPositionId = 1L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.doc", "application/msword", "test".getBytes());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, cvFile));
        assertEquals("Chỉ hỗ trợ file PDF !", exception.getMessage());
    }
    
    @Test
    void testInvalidApplication_JobPositionNotFound() {
        // Arrange
        Long jobPositionId = 999L;
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@example.com";
        String phoneNumber = "0123456789";
        String address = "123 Đường ABC, Quận 1, TP.HCM";
        MockMultipartFile cvFile = new MockMultipartFile("cv", "test.pdf", "application/pdf", "test".getBytes());
        
        when(jobPositionRepo.findById(jobPositionId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, cvFile));
        assertEquals("Job position not found", exception.getMessage());
    }
}
