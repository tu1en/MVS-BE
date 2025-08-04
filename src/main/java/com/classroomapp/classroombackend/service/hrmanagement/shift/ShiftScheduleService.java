package com.classroomapp.classroombackend.service.hrmanagement.shift;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.ScheduleNotificationType;
import com.classroomapp.classroombackend.dto.hrmanagement.ScheduleConflictResult;
import com.classroomapp.classroombackend.dto.hrmanagement.ScheduleStatistics;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Service interface for Shift Schedule Management
 */
public interface ShiftScheduleService {
    
    ShiftSchedule createSchedule(ShiftSchedule schedule);
    ShiftSchedule updateSchedule(Long id, ShiftSchedule schedule);
    void deleteSchedule(Long id);
    Optional<ShiftSchedule> findById(Long id);
    List<ShiftSchedule> findByStatus(ShiftSchedule.ScheduleStatus status);
    List<ShiftSchedule> findByType(ShiftSchedule.ScheduleType scheduleType);
    List<ShiftSchedule> findActiveSchedules();
    Optional<ShiftSchedule> findActiveScheduleForDate(LocalDate date);
    Page<ShiftSchedule> searchSchedules(ShiftSchedule.ScheduleStatus status, ShiftSchedule.ScheduleType scheduleType, Long createdById, LocalDate startDate, LocalDate endDate, String search, Pageable pageable);
    ShiftSchedule publishSchedule(Long id, User publisher);
    ShiftSchedule archiveSchedule(Long id);
    void cancelSchedule(Long id, String reason);
    void validateSchedule(ShiftSchedule schedule);
    List<ShiftSchedule> findOverlappingSchedules(LocalDate startDate, LocalDate endDate, Long excludeId);
    List<ShiftSchedule> findSchedulesNeedingArchive(int daysAfterEnd);
    List<ShiftSchedule> findUpcomingSchedules(int daysAhead);
    int autoArchiveOldSchedules(int daysAfterEnd);
    int cleanupOldDrafts(int daysOld);
    ShiftSchedule copySchedule(Long sourceScheduleId, LocalDate newStartDate, String newName);
    ShiftSchedule generateWeeklySchedule(LocalDate startDate, String name, User creator);
    ShiftSchedule generateMonthlySchedule(LocalDate startDate, String name, User creator);
    ScheduleStatistics getScheduleStatistics(LocalDate startDate, LocalDate endDate);
    List<ShiftSchedule> findSchedulesWithMostAssignments(int limit);
    void updateAssignmentCount(Long scheduleId);
    void bulkUpdateStatus(List<Long> scheduleIds, ShiftSchedule.ScheduleStatus status);
    byte[] exportSchedule(Long scheduleId, String format);
    void sendScheduleNotifications(ShiftSchedule schedule, ScheduleNotificationType type);
   ScheduleConflictResult validateScheduleConflicts(ShiftSchedule schedule);
    List<ShiftSchedule> findByCreatedByUserId(Long userId);
}