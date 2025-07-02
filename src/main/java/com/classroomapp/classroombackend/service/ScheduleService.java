package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.model.Schedule;

public interface ScheduleService {

    // Basic CRUD operations
    Schedule createSchedule(Schedule schedule);
    Schedule getScheduleById(Long id);
    Schedule updateSchedule(Long id, Schedule schedule);
    void deleteSchedule(Long id);
    
    // Business logic operations
    List<ScheduleDto> getSchedulesByTeacher(Long teacherId);
    List<ScheduleDto> getSchedulesByClassroom(Long classroomId);
    List<ScheduleDto> getSchedulesByDay(Integer dayOfWeek);
    List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek);
    
    // Create a schedule entry
    ScheduleDto createScheduleEntry(ScheduleDto scheduleDto);
    
    // Update a schedule entry
    ScheduleDto updateScheduleEntry(Long id, ScheduleDto scheduleDto);
} 