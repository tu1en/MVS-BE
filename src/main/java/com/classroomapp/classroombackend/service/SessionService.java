package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionStatisticsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSessionDto;
import com.classroomapp.classroombackend.model.classroommanagement.Session;

/**
 * Service interface for Session management
 * Provides business logic for session operations with comprehensive validation
 */
public interface SessionService {

    /**
     * Create a new session with business logic validation
     * 
     * @param createSessionDto Session creation data
     * @param userEmail Email of the user creating the session
     * @return Created session DTO
     * @throws IllegalArgumentException if validation fails
     * @throws SecurityException if user doesn't have permission
     */
    SessionDto createSession(CreateSessionDto createSessionDto, String userEmail);

    /**
     * Update an existing session with validation
     * 
     * @param sessionId ID of the session to update
     * @param updateSessionDto Session update data
     * @param userEmail Email of the user updating the session
     * @return Updated session DTO
     * @throws IllegalArgumentException if validation fails
     * @throws SecurityException if user doesn't have permission
     */
    SessionDto updateSession(Long sessionId, UpdateSessionDto updateSessionDto, String userEmail);

    /**
     * Delete a session with business logic validation
     * 
     * @param sessionId ID of the session to delete
     * @param userEmail Email of the user deleting the session
     * @throws IllegalArgumentException if session cannot be deleted
     * @throws SecurityException if user doesn't have permission
     */
    void deleteSession(Long sessionId, String userEmail);

    /**
     * Get session by ID with detailed information
     * 
     * @param sessionId ID of the session
     * @param userEmail Email of the requesting user
     * @return Session DTO with details
     * @throws SecurityException if user doesn't have access
     */
    Optional<SessionDto> getSessionById(Long sessionId, String userEmail);

    /**
     * Get all sessions for a classroom with pagination
     * 
     * @param classroomId ID of the classroom
     * @param pageable Pagination information
     * @param userEmail Email of the requesting user
     * @return Page of session DTOs
     * @throws SecurityException if user doesn't have access
     */
    Page<SessionDto> getSessionsByClassroom(Long classroomId, Pageable pageable, String userEmail);

    /**
     * Get sessions by status with pagination
     * 
     * @param status Session status
     * @param pageable Pagination information
     * @param userEmail Email of the requesting user
     * @return Page of session DTOs
     */
    Page<SessionDto> getSessionsByStatus(Session.SessionStatus status, Pageable pageable, String userEmail);

    /**
     * Get sessions by date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param userEmail Email of the requesting user
     * @return List of session DTOs
     */
    List<SessionDto> getSessionsByDateRange(LocalDate startDate, LocalDate endDate, String userEmail);

    /**
     * Get upcoming sessions for a user
     * 
     * @param userEmail Email of the requesting user
     * @return List of upcoming session DTOs
     */
    List<SessionDto> getUpcomingSessions(String userEmail);

    /**
     * Get today's sessions for a user
     * 
     * @param userEmail Email of the requesting user
     * @return List of today's session DTOs
     */
    List<SessionDto> getTodaySessions(String userEmail);

    /**
     * Update session status with workflow validation
     * 
     * @param sessionId ID of the session
     * @param newStatus New status to set
     * @param userEmail Email of the user updating status
     * @return Updated session DTO
     * @throws IllegalArgumentException if status transition is invalid
     * @throws SecurityException if user doesn't have permission
     */
    SessionDto updateSessionStatus(Long sessionId, Session.SessionStatus newStatus, String userEmail);

    /**
     * Get session statistics for a classroom
     * 
     * @param classroomId ID of the classroom
     * @param userEmail Email of the requesting user
     * @return Session statistics DTO
     * @throws SecurityException if user doesn't have access
     */
    SessionStatisticsDto getSessionStatistics(Long classroomId, String userEmail);

    /**
     * Validate session date against classroom date range
     * 
     * @param classroomId ID of the classroom
     * @param sessionDate Date of the session
     * @return true if date is valid, false otherwise
     */
    boolean validateSessionDate(Long classroomId, LocalDate sessionDate);

    /**
     * Check if session date conflicts with existing sessions
     * 
     * @param classroomId ID of the classroom
     * @param sessionDate Date to check
     * @param excludeSessionId Session ID to exclude from check (for updates)
     * @return true if date conflicts, false otherwise
     */
    boolean hasDateConflict(Long classroomId, LocalDate sessionDate, Long excludeSessionId);

    /**
     * Get sessions for teacher with pagination
     * 
     * @param teacherId ID of the teacher
     * @param pageable Pagination information
     * @param userEmail Email of the requesting user
     * @return Page of session DTOs
     * @throws SecurityException if user doesn't have access
     */
    Page<SessionDto> getSessionsByTeacher(Long teacherId, Pageable pageable, String userEmail);

    /**
     * Get sessions for student with pagination
     * 
     * @param studentId ID of the student
     * @param pageable Pagination information
     * @param userEmail Email of the requesting user
     * @return Page of session DTOs
     * @throws SecurityException if user doesn't have access
     */
    Page<SessionDto> getSessionsByStudent(Long studentId, Pageable pageable, String userEmail);

    /**
     * Search sessions by description
     * 
     * @param keyword Search keyword
     * @param userEmail Email of the requesting user
     * @return List of matching session DTOs
     */
    List<SessionDto> searchSessions(String keyword, String userEmail);

    /**
     * Get sessions that need status update (automated maintenance)
     * 
     * @return List of sessions needing status update
     */
    List<Session> getSessionsNeedingStatusUpdate();

    /**
     * Automatically update session statuses based on current date/time
     * 
     * @return Number of sessions updated
     */
    int updateSessionStatuses();

    /**
     * Check if user has permission to access session
     * 
     * @param sessionId ID of the session
     * @param userEmail Email of the user
     * @return true if user has access, false otherwise
     */
    boolean hasSessionAccess(Long sessionId, String userEmail);

    /**
     * Check if user has permission to modify session
     * 
     * @param sessionId ID of the session
     * @param userEmail Email of the user
     * @return true if user can modify, false otherwise
     */
    boolean canModifySession(Long sessionId, String userEmail);

    /**
     * Get session progress percentage
     * 
     * @param sessionId ID of the session
     * @return Progress percentage (0-100)
     */
    double getSessionProgress(Long sessionId);

    /**
     * Clone session to another date
     * 
     * @param sessionId ID of the session to clone
     * @param newDate New date for the cloned session
     * @param userEmail Email of the user performing the clone
     * @return Cloned session DTO
     * @throws IllegalArgumentException if cloning is not allowed
     * @throws SecurityException if user doesn't have permission
     */
    SessionDto cloneSession(Long sessionId, LocalDate newDate, String userEmail);

    /**
     * Bulk create sessions for a date range
     * 
     * @param classroomId ID of the classroom
     * @param startDate Start date
     * @param endDate End date
     * @param description Description for all sessions
     * @param userEmail Email of the user creating sessions
     * @return List of created session DTOs
     * @throws IllegalArgumentException if validation fails
     * @throws SecurityException if user doesn't have permission
     */
    List<SessionDto> bulkCreateSessions(Long classroomId, LocalDate startDate, LocalDate endDate, 
                                       String description, String userEmail);

    /**
     * Get session calendar data for a classroom
     * 
     * @param classroomId ID of the classroom
     * @param year Year for calendar
     * @param month Month for calendar (1-12)
     * @param userEmail Email of the requesting user
     * @return Calendar data with sessions
     */
    Object getSessionCalendar(Long classroomId, int year, int month, String userEmail);
}
