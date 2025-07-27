package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.UserScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserScheduleServiceImpl implements UserScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Override
    public List<TimetableEventDto> getTimetableForUser(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found: {}", userId);
                return new ArrayList<>();
            }
    
            List<Schedule> schedules = new ArrayList<>();
            String userRole = user.getRole();
    
            if ("TEACHER".equals(userRole)) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
                schedules = scheduleRepository.findByTeacherIdAndDateRange(userId, startDateTime, endDateTime);
            } else if ("STUDENT".equals(userRole)) {
                log.warn("Student schedule lookup not fully implemented - enrollment relationship needed");
            }
    
            return schedules.stream()
                .map(this::convertToTimetableEvent)
                .collect(Collectors.toList());
    
        } catch (Exception e) {
            log.error("Error getting timetable for user {}: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    

    @Override
    public List<ScheduleDto> getAllSchedules() {
        try {
            return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all schedules: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ScheduleDto getScheduleById(Long id) {
        try {
            return scheduleRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
        } catch (Exception e) {
            log.error("Error getting schedule by id {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherId(Long teacherId) {
        try {
            return scheduleRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting schedules for teacher {}: {}", teacherId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ScheduleDto> getSchedulesByStudentId(Long studentId) {
        try {
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null) {
                return new ArrayList<>();
            }
            
            log.warn("Student schedule lookup not fully implemented - enrollment relationship needed");
            return new ArrayList<>();
                
        } catch (Exception e) {
            log.error("Error getting schedules for student {}: {}", studentId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ScheduleDto> getSchedulesByClassroomId(Long classroomId) {
        try {
            return scheduleRepository.findByClassroomId(classroomId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting schedules for classroom {}: {}", classroomId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        return scheduleDto;
    }

    @Override
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        return scheduleDto;
    }

    @Override
    public void deleteSchedule(Long id) {
        // Implementation for deleting schedule
    }

    @Override
    public void createSampleSchedules(Long classroomId) {
        // Implementation for creating sample schedules
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek) {
        try {
            // ✅ Fixed: Use new repository method or alternative approach
            // Since findByTeacherIdAndDayOfWeek doesn't exist, we'll filter by teacher and process datetime
            List<Schedule> teacherSchedules = scheduleRepository.findByTeacherId(teacherId);
            
            return teacherSchedules.stream()
                .filter(schedule -> {
                    if (schedule.getStartDatetime() != null) {
                        // Convert dayOfWeek parameter (0=Monday) to Java DayOfWeek
                        int scheduleDayOfWeek = schedule.getStartDatetime().getDayOfWeek().getValue() - 1; // Java uses 1=Monday, we use 0=Monday
                        return scheduleDayOfWeek == dayOfWeek;
                    }
                    return false;
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting schedules for teacher {} on day {}: {}", teacherId, dayOfWeek, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ScheduleDto createScheduleEntry(ScheduleDto scheduleDto) {
        return createSchedule(scheduleDto);
    }

    @Override
    public List<LectureDto> getLecturesByScheduleId(Long scheduleId) {
        return new ArrayList<>();
    }
    
    // ✅ Fixed: Helper methods using new Schedule fields
    private TimetableEventDto convertToTimetableEvent(Schedule schedule) {
        TimetableEventDto event = new TimetableEventDto();
        event.setId(schedule.getId());
        
        // ✅ Use new fields
        event.setTitle(schedule.getTitle() != null ? schedule.getTitle() : "Untitled Event");
        event.setDescription(schedule.getDescription());
        event.setStartDatetime(schedule.getStartDatetime());
        event.setEndDatetime(schedule.getEndDatetime());
        event.setLocation(schedule.getLocation());
        event.setColor(schedule.getColor());
        
        if (schedule.getClassroom() != null) {
            event.setClassroomId(schedule.getClassroom().getId());
            event.setClassroomName(schedule.getClassroom().getName());
        }
        
        return event;
    }
    
    private ScheduleDto convertToDto(Schedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        
        if (schedule.getTeacher() != null) {
            dto.setTeacherId(schedule.getTeacher().getId());
        }
        if (schedule.getClassroom() != null) {
            dto.setClassroomId(schedule.getClassroom().getId());
        }
        
        // ✅ Fixed: Convert new datetime fields to legacy fields for DTO compatibility
        if (schedule.getStartDatetime() != null) {
            // Convert to day of week (0=Monday, 1=Tuesday, etc.)
            int dayOfWeekValue = schedule.getStartDatetime().getDayOfWeek().getValue() - 1;
            dto.setDayOfWeek(convertIntegerToDayOfWeek(dayOfWeekValue));
            dto.setStartTime(schedule.getStartDatetime().toLocalTime());
        }
        
        if (schedule.getEndDatetime() != null) {
            dto.setEndTime(schedule.getEndDatetime().toLocalTime());
        }
        
        // ✅ Use new fields or provide defaults
        dto.setRoom(schedule.getLocation() != null ? schedule.getLocation() : "");
        dto.setLocation(schedule.getLocation());
        dto.setSubject(schedule.getTitle() != null ? schedule.getTitle() : "");
        dto.setMaterialsUrl(schedule.getMaterialsUrl());
        dto.setMeetUrl(schedule.getMeetUrl());
        
        return dto;
    }
    
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