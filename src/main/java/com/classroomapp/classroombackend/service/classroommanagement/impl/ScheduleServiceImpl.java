package com.classroomapp.classroombackend.service.classroommanagement.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateScheduleDto;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomSchedule;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomScheduleRepository;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleServiceImpl implements ClassroomScheduleService {

    private final ClassroomScheduleRepository scheduleRepository;
    private final ClassroomRepository classroomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByClassroomId(Long classroomId) {
        log.info("📅 Getting schedules for classroom ID: {}", classroomId);

        List<ClassroomSchedule> schedules =
                scheduleRepository.findByClassroomIdOrderByDayOfWeekAscStartTimeAsc(classroomId);

        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDto getScheduleById(Long id) {
        ClassroomSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + id));
        return convertToDto(schedule);
    }

    @Override
    public ScheduleDto createSchedule(CreateScheduleDto createDto) {
        validateSchedule(createDto);

        Classroom classroom = classroomRepository.findById(createDto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found with ID: " + createDto.getClassroomId()));

        ClassroomSchedule schedule = new ClassroomSchedule();
        schedule.setClassroom(classroom);
        schedule.setDayOfWeek(createDto.getDayOfWeek());
        schedule.setStartTime(createDto.getStartTime());
        schedule.setEndTime(createDto.getEndTime());
        schedule.setLocation(createDto.getLocation());
        schedule.setNotes(createDto.getNotes());
        schedule.setRecurring(createDto.isRecurring());

        ClassroomSchedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    @Override
    public ScheduleDto updateSchedule(Long id, UpdateScheduleDto updateDto) {
        validateScheduleUpdate(id, updateDto);

        ClassroomSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + id));

        schedule.setDayOfWeek(updateDto.getDayOfWeek());
        schedule.setStartTime(updateDto.getStartTime());
        schedule.setEndTime(updateDto.getEndTime());
        schedule.setLocation(updateDto.getLocation());
        schedule.setNotes(updateDto.getNotes());
        schedule.setRecurring(updateDto.isRecurring());

        ClassroomSchedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Schedule not found with ID: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasScheduleConflict(Long classroomId, DayOfWeek dayOfWeek, LocalTime startTime,
                                       LocalTime endTime, Long excludeScheduleId) {
        List<ClassroomSchedule> existingSchedules;

        if (excludeScheduleId != null) {
            existingSchedules = scheduleRepository.findConflictingSchedulesExcluding(
                    classroomId, dayOfWeek, startTime, endTime, excludeScheduleId);
        } else {
            existingSchedules = scheduleRepository.findConflictingSchedules(
                    classroomId, dayOfWeek, startTime, endTime);
        }

        return !existingSchedules.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByDayOfWeek(DayOfWeek dayOfWeek) {
        List<ClassroomSchedule> schedules = scheduleRepository.findByDayOfWeekOrderByStartTimeAsc(dayOfWeek);
        return schedules.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByLocation(String location) {
        List<ClassroomSchedule> schedules =
                scheduleRepository.findByLocationContainingIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(location);
        return schedules.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean classroomExists(Long classroomId) {
        return classroomRepository.existsById(classroomId);
    }

    @Override
    public void validateSchedule(CreateScheduleDto createDto) {
        if (!createDto.isValidTimeRange()) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (!createDto.isWithinWorkingHours()) {
            throw new IllegalArgumentException("Schedule must be within working hours (07:00 - 22:00)");
        }
        if (createDto.getDurationInMinutes() < 15) {
            throw new IllegalArgumentException("Schedule duration must be at least 15 minutes");
        }
        if (createDto.getDurationInMinutes() > 480) {
            throw new IllegalArgumentException("Schedule duration cannot exceed 8 hours");
        }
        if (!classroomExists(createDto.getClassroomId())) {
            throw new IllegalArgumentException("Classroom not found with ID: " + createDto.getClassroomId());
        }
    }

    @Override
    public void validateScheduleUpdate(Long scheduleId, UpdateScheduleDto updateDto) {
        if (!updateDto.isValidTimeRange()) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (!updateDto.isWithinWorkingHours()) {
            throw new IllegalArgumentException("Schedule must be within working hours (07:00 - 22:00)");
        }
        long duration = java.time.Duration.between(updateDto.getStartTime(), updateDto.getEndTime()).toMinutes();
        if (duration < 15) {
            throw new IllegalArgumentException("Schedule duration must be at least 15 minutes");
        }
        if (duration > 480) {
            throw new IllegalArgumentException("Schedule duration cannot exceed 8 hours");
        }
    }

    /**
     * Convert entity → DTO (Full fields for frontend)
     */
    private ScheduleDto convertToDto(ClassroomSchedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setLectureId(schedule.getId()); // hiện tại dùng scheduleId làm lectureId
        dto.setClassroomId(schedule.getClassroom().getId());
        dto.setClassroomName(schedule.getClassroom().getName());
        dto.setTeacherId(schedule.getClassroom().getTeacher().getId());
        dto.setTeacherName(schedule.getClassroom().getTeacher().getFullName());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setSubject(schedule.getSubject());
        dto.setRoom(schedule.getRoom());
        dto.setLocation(schedule.getLocation());
        dto.setMaterialsUrl(schedule.getMaterialsUrl());
        dto.setMeetUrl(schedule.getMeetUrl());
        dto.setStudentCount(schedule.getClassroom().getStudents() != null ?
                schedule.getClassroom().getStudents().size() : 0);
        dto.setNotes(schedule.getNotes());
        return dto;
    }
}
