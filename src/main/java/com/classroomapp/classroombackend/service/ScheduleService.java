package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ScheduleDTO;
import java.util.List;

public interface ScheduleService {
    List<ScheduleDTO> getAllSchedules();
    ScheduleDTO getScheduleById(Long id);
    ScheduleDTO createSchedule(ScheduleDTO scheduleDTO);
    ScheduleDTO updateSchedule(Long id, ScheduleDTO scheduleDTO);
    void deleteSchedule(Long id);
    List<ScheduleDTO> getSchedulesByTeacherId(Long teacherId);
    List<ScheduleDTO> getSchedulesByStudentId(Long studentId);
    List<ScheduleDTO> getSchedulesByClassId(String classId);
}
