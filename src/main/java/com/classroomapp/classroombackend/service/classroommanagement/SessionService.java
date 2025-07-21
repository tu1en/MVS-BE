package com.classroomapp.classroombackend.service.classroommanagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSessionDto;
import com.classroomapp.classroombackend.model.classroommanagement.Session;

/**
 * Service interface for Session management
 */
public interface SessionService {

    /**
     * Get all sessions with pagination
     */
    Page<SessionDto> getAllSessions(Pageable pageable);

    /**
     * Get session by ID (throws exception if not found)
     */
    SessionDto getSessionById(Long id);

    /**
     * Get sessions by classroom ID
     */
    List<SessionDto> getSessionsByClassroomId(Long classroomId);

    /**
     * Get sessions by classroom (alias for controller)
     */
    List<SessionDto> getSessionsByClassroom(Long classroomId);

    /**
     * Get sessions by date
     */
    List<SessionDto> getSessionsByDate(LocalDate date);

    /**
     * Get sessions by date range
     */
    List<SessionDto> getSessionsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get sessions by status
     */
    List<SessionDto> getSessionsByStatus(Session.SessionStatus status);

    /**
     * Create new session
     */
    SessionDto createSession(CreateSessionDto createDto);

    /**
     * Update existing session
     */
    SessionDto updateSession(Long id, UpdateSessionDto updateDto);

    /**
     * Delete session
     */
    void deleteSession(Long id);

    /**
     * Update session status
     */
    SessionDto updateSessionStatus(Long id, Session.SessionStatus status);

    /**
     * Check if session exists by ID
     */
    boolean existsById(Long id);

    /**
     * Count sessions by classroom ID
     */
    long countSessionsByClassroomId(Long classroomId);

    /**
     * Count sessions by status
     */
    long countSessionsByStatus(Session.SessionStatus status);
}
