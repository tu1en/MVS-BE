package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.List;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;

public interface ScheduleService {
    /**
     * Chuyển đổi lịch học thành sự kiện lịch trình cho người dùng
     */
    List<TimetableEventDto> getTimetableForUser(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy tất cả lịch học
     */
    List<ScheduleDto> getAllSchedules();
    
    /**
     * Lấy lịch học theo ID
     */
    ScheduleDto getScheduleById(Long id);
    
    /**
     * Lấy lịch học theo ID của giáo viên
     */
    List<ScheduleDto> getSchedulesByTeacherId(Long teacherId);
    
    /**
     * Lấy lịch học theo ID của học sinh
     */
    List<ScheduleDto> getSchedulesByStudentId(Long studentId);
    
    /**
     * Lấy lịch học theo ID của lớp học
     */
    List<ScheduleDto> getSchedulesByClassroomId(Long classroomId);
    
    /**
     * Tạo lịch học mới
     */
    ScheduleDto createSchedule(ScheduleDto scheduleDto);
    
    /**
     * Cập nhật lịch học
     */
    ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto);
    
    /**
     * Xóa lịch học
     */
    void deleteSchedule(Long id);
    
    /**
     * Tạo lịch học mẫu cho một lớp học
     */
    void createSampleSchedules(Long classroomId);

    /**
     * Lấy lịch học theo giáo viên và ngày trong tuần
     */
    List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek);

    /**
     * Tạo một entry lịch học mới
     */
    ScheduleDto createScheduleEntry(ScheduleDto scheduleDto);

    /**
     * Get lectures associated with a specific schedule
     */
    List<LectureDto> getLecturesByScheduleId(Long scheduleId);
}