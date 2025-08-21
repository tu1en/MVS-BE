package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.attendancemanagement.CreateMakeupAttendanceRequestDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MakeupAttendanceRequestDto;
import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest;
import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest.RequestStatus;

/**
 * Service interface for managing makeup attendance requests
 */
public interface MakeupAttendanceService {
    
    /**
     * Create a new makeup attendance request
     * @param dto Request data
     * @param teacherEmail Email of the requesting teacher
     * @return Created request DTO
     */
    MakeupAttendanceRequestDto createRequest(CreateMakeupAttendanceRequestDto dto, String teacherEmail);
    
    /**
     * Get all requests for a specific teacher
     * @param teacherEmail Teacher's email
     * @return List of requests
     */
    List<MakeupAttendanceRequestDto> getMyRequests(String teacherEmail);
    
    /**
     * Get all requests for a specific teacher with pagination
     * @param teacherEmail Teacher's email
     * @param pageable Pagination parameters
     * @return Page of requests
     */
    Page<MakeupAttendanceRequestDto> getMyRequests(String teacherEmail, Pageable pageable);
    
    /**
     * Get all pending requests (for manager acknowledgment)
     * @return List of pending requests
     */
    List<MakeupAttendanceRequestDto> getPendingRequests();

    /**
     * Get all pending requests with pagination
     * @param pageable Pagination parameters
     * @return Page of pending requests
     */
    Page<MakeupAttendanceRequestDto> getPendingRequests(Pageable pageable);
    
    /**
     * Get all requests by status
     * @param status Request status
     * @return List of requests
     */
    List<MakeupAttendanceRequestDto> getRequestsByStatus(RequestStatus status);
    
    /**
     * Acknowledge a makeup attendance request
     * @param requestId Request ID
     * @param managerEmail Email of the acknowledging manager
     * @param managerNotes Optional notes from manager
     * @return Updated request DTO
     */
    MakeupAttendanceRequestDto acknowledgeRequest(Long requestId, String managerEmail, String managerNotes);
    
    /**
     * Get a specific request by ID
     * @param requestId Request ID
     * @return Request DTO
     */
    MakeupAttendanceRequestDto getRequestById(Long requestId);
    
    /**
     * Check if a teacher can create a makeup request for a specific lecture
     * @param lectureId Lecture ID
     * @param teacherEmail Teacher's email
     * @return True if request can be created
     */
    boolean canCreateMakeupRequest(Long lectureId, String teacherEmail);
    
    /**
     * Get acknowledged requests for a teacher that haven't been completed yet
     * @param teacherEmail Teacher's email
     * @return List of acknowledged requests
     */
    List<MakeupAttendanceRequestDto> getAcknowledgedRequestsForTeacher(String teacherEmail);
    
    /**
     * Mark a request as completed after makeup attendance is taken
     * @param requestId Request ID
     */
    void markRequestAsCompleted(Long requestId);
    
    /**
     * Get statistics for a teacher
     * @param teacherEmail Teacher's email
     * @return Statistics map
     */
    Map<String, Long> getTeacherStatistics(String teacherEmail);
    
    /**
     * Get overall statistics
     * @return Statistics map
     */
    Map<String, Long> getOverallStatistics();
    
    /**
     * Get recent requests for dashboard
     * @return List of recent requests
     */
    List<MakeupAttendanceRequestDto> getRecentRequests();
    
    /**
     * Check if there's an acknowledged makeup request for a specific lecture and teacher
     * @param lectureId Lecture ID
     * @param teacherEmail Teacher's email
     * @return The acknowledged request if exists, null otherwise
     */
    MakeupAttendanceRequest getAcknowledgedRequestForLecture(Long lectureId, String teacherEmail);
}
