package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ScheduleDTO;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.service.ScheduleService;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {
    
    private final ScheduleRepository scheduleRepository;

    private ScheduleDTO mapToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setClassId(schedule.getClassId());
        dto.setClassName(schedule.getClassName());
        dto.setSubject(schedule.getSubject());
        dto.setDay(schedule.getDay());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setTeacherId(schedule.getTeacherId());
        dto.setTeacherName(schedule.getTeacherName());
        dto.setStudentIds(schedule.getStudentIds());
        dto.setMaterialsUrl(schedule.getMaterialsUrl());
        dto.setMeetUrl(schedule.getMeetUrl());
        dto.setRoom(schedule.getRoom());
        return dto;
    }

    private Schedule mapToEntity(ScheduleDTO dto) {
        Schedule schedule = new Schedule();
        schedule.setId(dto.getId());
        schedule.setClassId(dto.getClassId());
        schedule.setClassName(dto.getClassName());
        schedule.setSubject(dto.getSubject());
        schedule.setDay(dto.getDay());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setTeacherId(dto.getTeacherId());
        schedule.setTeacherName(dto.getTeacherName());
        schedule.setStudentIds(dto.getStudentIds());
        schedule.setMaterialsUrl(dto.getMaterialsUrl());
        schedule.setMeetUrl(dto.getMeetUrl());
        schedule.setRoom(dto.getRoom());
        return schedule;
    }

    @Override
    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ScheduleDTO getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return mapToDTO(schedule);
    }

    @Override
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
        Schedule schedule = mapToEntity(scheduleDTO);
        schedule.setId(null); // Ensure we're creating a new entity
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return mapToDTO(savedSchedule);
    }

    @Override
    public ScheduleDTO updateSchedule(Long id, ScheduleDTO scheduleDTO) {
        Schedule existingSchedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        Schedule updatedSchedule = mapToEntity(scheduleDTO);
        updatedSchedule.setId(id);
        Schedule savedSchedule = scheduleRepository.save(updatedSchedule);
        return mapToDTO(savedSchedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    @Override
    public List<ScheduleDTO> getSchedulesByTeacherId(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDTO> getSchedulesByStudentId(Long studentId) {
        return scheduleRepository.findByStudentIdsContaining(studentId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDTO> getSchedulesByClassId(String classId) {
        return scheduleRepository.findByClassId(classId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
}
