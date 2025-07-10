package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.dto.CreateEventDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;

public interface TimetableService {
    
    /**
     * Create a new timetable event
     */
    TimetableEventDto createEvent(CreateEventDto createDto, Long createdBy);
    
    /**
     * Get event by ID
     */
    TimetableEventDto getEventById(Long eventId);
    
    /**
     * Get events within date range (for all classrooms)
     */
    List<TimetableEventDto> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get events for a classroom within date range
     */
    List<TimetableEventDto> getEventsByClassroomAndDateRange(Long classroomId, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate);
    
    /**
     * Get upcoming events for a classroom
     */
    List<TimetableEventDto> getUpcomingEvents(Long classroomId);
    
    /**
     * Get events by type
     */
    List<TimetableEventDto> getEventsByType(Long classroomId, String eventType);
    
    /**
     * Get all-day events
     */
    List<TimetableEventDto> getAllDayEvents(Long classroomId);
    
    /**
     * Get recurring events
     */
    List<TimetableEventDto> getRecurringEvents(Long classroomId);
    
    /**
     * Update event
     */
    TimetableEventDto updateEvent(Long eventId, CreateEventDto updateDto);
    
    /**
     * Delete event
     */
    void deleteEvent(Long eventId);
    
    /**
     * Cancel event
     */
    TimetableEventDto cancelEvent(Long eventId);
    
    /**
     * Check for conflicting events
     */
    List<TimetableEventDto> checkConflicts(Long classroomId, LocalDateTime startTime, 
                                         LocalDateTime endTime, Long excludeEventId);
    
    /**
     * Get events for multiple classrooms (teacher view)
     */
    List<TimetableEventDto> getEventsByClassrooms(List<Long> classroomIds, 
                                                LocalDateTime startDate, 
                                                LocalDateTime endDate);
    
    /**
     * Add attendee to event
     */
    void addAttendee(Long eventId, Long userId);
    
    /**
     * Remove attendee from event
     */
    void removeAttendee(Long eventId, Long userId);
    
    /**
     * Update attendance status
     */
    void updateAttendanceStatus(Long eventId, Long userId, String status);
    
    /**
     * Get event attendees
     */
    List<TimetableEventDto> getEventAttendees(Long eventId);
    
    /**
     * Create recurring event instances
     */
    List<TimetableEventDto> createRecurringInstances(Long parentEventId, 
                                                    LocalDateTime endDate);
    
    /**
     * Get events created by a user
     */
    List<TimetableEventDto> getEventsByCreator(Long createdBy);

    /**
     * Get timetable events for a specific user (student or teacher)
     * This method will get events from classrooms the user is enrolled in or teaching
     */
    List<TimetableEventDto> getEventsForUser(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
