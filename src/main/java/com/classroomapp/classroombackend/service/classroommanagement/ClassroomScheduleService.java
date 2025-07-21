package com.classroomapp.classroombackend.service.classroommanagement;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateScheduleDto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for Classroom Schedule management
 */
public interface ClassroomScheduleService {

    /**
     * Get all schedules for a classroom
     */
    List<ScheduleDto> getSchedulesByClassroomId(Long classroomId);

    /**
     * Get schedule by ID
     */
    ScheduleDto getScheduleById(Long id);

    /**
     * Create new schedule
     */
    ScheduleDto createSchedule(CreateScheduleDto createDto);

    /**
     * Update existing schedule
     */
    ScheduleDto updateSchedule(Long id, UpdateScheduleDto updateDto);

    /**
     * Delete schedule
     */
    void deleteSchedule(Long id);

    /**
     * Check for schedule conflicts
     */
    boolean hasScheduleConflict(Long classroomId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Long excludeScheduleId);

    /**
     * Get schedules by day of week
     */
    List<ScheduleDto> getSchedulesByDayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Get schedules by location
     */
    List<ScheduleDto> getSchedulesByLocation(String location);

    /**
     * Check if classroom exists
     */
    boolean classroomExists(Long classroomId);

    /**
     * Validate schedule business rules
     */
    void validateSchedule(CreateScheduleDto createDto);

    /**
     * Validate schedule business rules for update
     */
    void validateScheduleUpdate(Long scheduleId, UpdateScheduleDto updateDto);
}