package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.List;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;

public interface UserScheduleService {
    /**
     * Chuyá»ƒn Ä‘á»•i lá»‹ch há»c thÃ nh sá»± kiá»‡n lá»‹ch trÃ¬nh cho ngÆ°á»i dÃ¹ng
     */
    List<TimetableEventDto> getTimetableForUser(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Láº¥y táº¥t cáº£ lá»‹ch há»c
     */
    List<ScheduleDto> getAllSchedules();
    
    /**
     * Láº¥y lá»‹ch há»c theo ID
     */
    ScheduleDto getScheduleById(Long id);
    
    /**
     * Láº¥y lá»‹ch há»c theo ID cá»§a giÃ¡o viÃªn
     */
    List<ScheduleDto> getSchedulesByTeacherId(Long teacherId);
    
    /**
     * Láº¥y lá»‹ch há»c theo ID cá»§a há»c sinh
     */
    List<ScheduleDto> getSchedulesByStudentId(Long studentId);
    
    /**
     * Láº¥y lá»‹ch há»c theo ID cá»§a lá»›p há»c
     */
    List<ScheduleDto> getSchedulesByClassroomId(Long classroomId);
    
    /**
     * Táº¡o lá»‹ch há»c má»›i
     */
    ScheduleDto createSchedule(ScheduleDto scheduleDto);
    
    /**
     * Cáº­p nháº­t lá»‹ch há»c
     */
    ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto);
    
    /**
     * XÃ³a lá»‹ch há»c
     */
    void deleteSchedule(Long id);
    
    /**
     * Táº¡o lá»‹ch há»c máº«u cho má»™t lá»›p há»c
     */
    void createSampleSchedules(Long classroomId);

    /**
     * Láº¥y lá»‹ch há»c theo giÃ¡o viÃªn vÃ  ngÃ y trong tuáº§n
     */
    List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek);

    /**
     * Táº¡o má»™t entry lá»‹ch há»c má»›i
     */
    ScheduleDto createScheduleEntry(ScheduleDto scheduleDto);

    /**
     * Get lectures associated with a specific schedule
     */
    List<LectureDto> getLecturesByScheduleId(Long scheduleId);
}
