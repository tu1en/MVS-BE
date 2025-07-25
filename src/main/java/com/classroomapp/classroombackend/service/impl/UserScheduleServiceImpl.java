package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.service.UserScheduleService;

@Service
public class UserScheduleServiceImpl implements UserScheduleService {

    @Override
    public List<TimetableEventDto> getTimetableForUser(Long userId, LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    @Override
    public List<ScheduleDto> getAllSchedules() {
        return new ArrayList<>();
    }

    @Override
    public ScheduleDto getScheduleById(Long id) {
        return null;
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherId(Long teacherId) {
        return new ArrayList<>();
    }

    @Override
    public List<ScheduleDto> getSchedulesByStudentId(Long studentId) {
        return new ArrayList<>();
    }

    @Override
    public List<ScheduleDto> getSchedulesByClassroomId(Long classroomId) {
        return new ArrayList<>();
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
        // No action for now
    }

    @Override
    public void createSampleSchedules(Long classroomId) {
        // No action for now
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek) {
        return new ArrayList<>();
    }

    @Override
    public ScheduleDto createScheduleEntry(ScheduleDto scheduleDto) {
        return scheduleDto;
    }

    @Override
    public List<LectureDto> getLecturesByScheduleId(Long scheduleId) {
        return new ArrayList<>();
    }
}
