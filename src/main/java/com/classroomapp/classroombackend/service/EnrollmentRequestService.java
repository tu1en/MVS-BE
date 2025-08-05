package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.request.CreateEnrollmentRequestDto;
import com.classroomapp.classroombackend.dto.response.EnrollmentRequestDto;
import com.classroomapp.classroombackend.entity.EnrollmentRequest.EnrollmentStatus;

/**
 * Service interface for enrollment request operations
 */
public interface EnrollmentRequestService {
    
    /**
     * Create a new enrollment request
     * @param dto The enrollment request data
     * @param studentId The student making the request
     * @return The created enrollment request
     */
    EnrollmentRequestDto createEnrollmentRequest(CreateEnrollmentRequestDto dto, Long studentId);
    
    /**
     * Approve an enrollment request
     * @param requestId The request ID to approve
     * @param managerId The manager approving the request
     * @return The updated enrollment request
     */
    EnrollmentRequestDto approveRequest(Long requestId, Long managerId);
    
    /**
     * Reject an enrollment request
     * @param requestId The request ID to reject
     * @param managerId The manager rejecting the request
     * @param reason The reason for rejection
     * @return The updated enrollment request
     */
    EnrollmentRequestDto rejectRequest(Long requestId, Long managerId, String reason);
    
    /**
     * Get enrollment requests by status
     * @param status The status to filter by
     * @return List of enrollment requests
     */
    List<EnrollmentRequestDto> getRequestsByStatus(EnrollmentStatus status);
    
    /**
     * Get enrollment requests for a specific student
     * @param studentId The student ID
     * @return List of enrollment requests for the student
     */
    List<EnrollmentRequestDto> getStudentRequests(Long studentId);
    
    /**
     * Get an enrollment request by ID
     * @param requestId The request ID
     * @return The enrollment request
     */
    EnrollmentRequestDto getRequestById(Long requestId);
    
    /**
     * Check if a student has already requested enrollment for a course template
     * @param studentId The student ID
     * @param courseTemplateId The course template ID
     * @return True if request exists, false otherwise
     */
    boolean hasExistingRequest(Long studentId, Long courseTemplateId);
}