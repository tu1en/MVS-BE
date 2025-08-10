package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.dto.InterviewScheduleDto;

public interface InterviewScheduleService {
    InterviewScheduleDto create(Long applicationId, LocalDateTime startTime, LocalDateTime endTime);
    List<InterviewScheduleDto> getByJobPosition(Long jobPositionId);
    List<InterviewScheduleDto> getByApplication(Long applicationId);
    List<InterviewScheduleDto> getAll();
    void updateStatus(Long id, String status, String result);
    void updateOffer(Long id, String offer);
    void updateEvaluation(Long id, String evaluation);
    void updateHourlyRate(Long id, String hourlyRate);
    InterviewScheduleDto getById(Long id);
    void delete(Long id);
    boolean hasConflict(LocalDateTime startTime, LocalDateTime endTime, Long excludeApplicationId);
    InterviewScheduleDto update(Long id, LocalDateTime startTime, LocalDateTime endTime);
    List<InterviewScheduleDto> getAcceptedInterviews();
} 