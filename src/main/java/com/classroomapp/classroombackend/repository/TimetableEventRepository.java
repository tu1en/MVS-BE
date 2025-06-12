package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.TimetableEvent;

@Repository
public interface TimetableEventRepository extends JpaRepository<TimetableEvent, Long> {
    
    /**
     * Find events for a classroom within a date range
     */
    @Query("SELECT te FROM TimetableEvent te WHERE te.classroomId = :classroomId " +
           "AND te.startDatetime >= :startDate AND te.endDatetime <= :endDate " +
           "AND te.isCancelled = false ORDER BY te.startDatetime")
    List<TimetableEvent> findByClassroomAndDateRange(@Param("classroomId") Long classroomId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find upcoming events for a classroom
     */
    @Query("SELECT te FROM TimetableEvent te WHERE te.classroomId = :classroomId " +
           "AND te.startDatetime > :currentTime AND te.isCancelled = false " +
           "ORDER BY te.startDatetime")
    List<TimetableEvent> findUpcomingByClassroom(@Param("classroomId") Long classroomId,
                                               @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find events by type
     */
    List<TimetableEvent> findByClassroomIdAndEventTypeAndIsCancelledFalseOrderByStartDatetime(
            Long classroomId, TimetableEvent.EventType eventType);
    
    /**
     * Find all-day events
     */
    List<TimetableEvent> findByClassroomIdAndIsAllDayTrueAndIsCancelledFalseOrderByStartDatetime(
            Long classroomId);
    
    /**
     * Find recurring events
     */
    List<TimetableEvent> findByClassroomIdAndRecurringRuleIsNotNullAndIsCancelledFalseOrderByStartDatetime(
            Long classroomId);
    
    /**
     * Find events created by a user
     */
    List<TimetableEvent> findByCreatedByOrderByStartDatetimeDesc(Long createdBy);
    
    /**
     * Find conflicting events (overlapping times)
     */
    @Query("SELECT te FROM TimetableEvent te WHERE te.classroomId = :classroomId " +
           "AND te.isCancelled = false " +
           "AND ((te.startDatetime <= :endTime AND te.endDatetime >= :startTime)) " +
           "AND (:eventId IS NULL OR te.id != :eventId)")
    List<TimetableEvent> findConflictingEvents(@Param("classroomId") Long classroomId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("eventId") Long eventId);
    
    /**
     * Find events for multiple classrooms (for teachers with multiple classes)
     */
    @Query("SELECT te FROM TimetableEvent te WHERE te.classroomId IN :classroomIds " +
           "AND te.startDatetime >= :startDate AND te.endDatetime <= :endDate " +
           "AND te.isCancelled = false ORDER BY te.startDatetime")
    List<TimetableEvent> findByClassroomsAndDateRange(@Param("classroomIds") List<Long> classroomIds,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // Find events within a date range
    @Query("SELECT e FROM TimetableEvent e WHERE " +
           "(e.startDatetime BETWEEN :startDate AND :endDate) OR " +
           "(e.endDatetime BETWEEN :startDate AND :endDate) OR " +
           "(e.startDatetime <= :startDate AND e.endDatetime >= :endDate)")
    List<TimetableEvent> findEventsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find events for a specific classroom within a date range
    @Query("SELECT e FROM TimetableEvent e WHERE e.classroomId = :classroomId AND " +
           "((e.startDatetime BETWEEN :startDate AND :endDate) OR " +
           "(e.endDatetime BETWEEN :startDate AND :endDate) OR " +
           "(e.startDatetime <= :startDate AND e.endDatetime >= :endDate))")
    List<TimetableEvent> findEventsByClassroomAndDateRange(
            @Param("classroomId") Long classroomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find upcoming events (events starting from now)
    @Query("SELECT e FROM TimetableEvent e WHERE e.startDatetime >= :now " +
           "ORDER BY e.startDatetime ASC")
    List<TimetableEvent> findUpcomingEvents(@Param("now") LocalDateTime now);
    
    // Find events created by a specific user
    List<TimetableEvent> findByCreatedBy(Long createdBy);
    
    // Find events by event type
    List<TimetableEvent> findByEventType(TimetableEvent.EventType eventType);
    
    // Find all-day events within a date range
    @Query("SELECT e FROM TimetableEvent e WHERE e.isAllDay = true AND " +
           "((e.startDatetime BETWEEN :startDate AND :endDate) OR " +
           "(e.endDatetime BETWEEN :startDate AND :endDate))")
    List<TimetableEvent> findAllDayEventsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
