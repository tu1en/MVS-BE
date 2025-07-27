package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Service("scheduleUserServiceImpl")
@RequiredArgsConstructor
public class ScheduleUserServiceImpl implements ScheduleService {
    
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleDto getScheduleById(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        return schedule.map(this::convertToDto).orElse(null);
    }

    @Override
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        // Simple implementation
        return scheduleDto;
    }

    @Override
    public ScheduleDto createScheduleEntry(ScheduleDto scheduleDto) {
        // Simple implementation
        return scheduleDto;
    }

    @Override
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        return scheduleDto;
    }

    @Override
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherId(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesByStudentId(Long studentId) {
        // Simple implementation - return empty list
        return List.of();
    }

    @Override
    public List<ScheduleDto> getSchedulesByClassroomId(Long classroomId) {
        return scheduleRepository.findByClassroomId(classroomId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LectureDto> getLecturesByScheduleId(Long scheduleId) {
        // Simple implementation - return empty list
        return List.of();
    }

    @Override
    public void createSampleSchedules(Long classroomId) {
        // Simple implementation - do nothing
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek) {
        // ✅ Fixed: Filter by teacher and day using new datetime fields
        try {
            List<Schedule> teacherSchedules = scheduleRepository.findByTeacherId(teacherId);
            
            return teacherSchedules.stream()
                .filter(schedule -> {
                    if (schedule.getStartDatetime() != null) {
                        // Convert dayOfWeek parameter (0=Monday) to Java DayOfWeek
                        int scheduleDayOfWeek = schedule.getStartDatetime().getDayOfWeek().getValue() - 1;
                        return scheduleDayOfWeek == dayOfWeek;
                    }
                    return false;
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            // Log error and return empty list
            return List.of();
        }
    }

    @Override
    public List<com.classroomapp.classroombackend.dto.TimetableEventDto> getTimetableForUser(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // Simple implementation - return empty list
        return List.of();
    }

    // ✅ Fixed: convertToDto method using new Schedule fields
    private ScheduleDto convertToDto(Schedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setClassroomId(schedule.getClassroom() != null ? schedule.getClassroom().getId() : null);
        dto.setTeacherId(schedule.getTeacher() != null ? schedule.getTeacher().getId() : null);
        
        // ✅ Fixed: Convert new datetime fields to legacy fields for DTO compatibility
        if (schedule.getStartDatetime() != null) {
            dto.setStartTime(schedule.getStartDatetime().toLocalTime());
            
            // Convert to day of week (0=Monday, 1=Tuesday, etc.)
            int dayOfWeekValue = schedule.getStartDatetime().getDayOfWeek().getValue() - 1;
            dto.setDayOfWeek(convertIntegerToDayOfWeek(dayOfWeekValue));
        }
        
        if (schedule.getEndDatetime() != null) {
            dto.setEndTime(schedule.getEndDatetime().toLocalTime());
        }
        
        // ✅ Fixed: Use new field getTitle() instead of getSubject()
        dto.setSubject(schedule.getTitle() != null ? schedule.getTitle() : "");
        
        // Set other fields from new Schedule entity
        dto.setLocation(schedule.getLocation());
        dto.setRoom(schedule.getLocation()); // Map location to room for backward compatibility
        dto.setMaterialsUrl(schedule.getMaterialsUrl());
        dto.setMeetUrl(schedule.getMeetUrl());
        
        return dto;
    }
    
    // Helper method to convert integer to DayOfWeek
    private java.time.DayOfWeek convertIntegerToDayOfWeek(Integer day) {
        if (day == null) return null;
        switch (day) {
            case 0: return java.time.DayOfWeek.MONDAY;
            case 1: return java.time.DayOfWeek.TUESDAY;
            case 2: return java.time.DayOfWeek.WEDNESDAY;
            case 3: return java.time.DayOfWeek.THURSDAY;
            case 4: return java.time.DayOfWeek.FRIDAY;
            case 5: return java.time.DayOfWeek.SATURDAY;
            case 6: return java.time.DayOfWeek.SUNDAY;
            default: return java.time.DayOfWeek.MONDAY;
        }
    }
}