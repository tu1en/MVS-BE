package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.model.InterviewSchedule;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.repository.InterviewScheduleRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl implements InterviewScheduleService {
    private final InterviewScheduleRepository interviewRepo;
    private final RecruitmentApplicationRepository appRepo;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public InterviewScheduleDto create(Long applicationId, LocalDateTime startTime, LocalDateTime endTime) {
        RecruitmentApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        InterviewSchedule entity = new InterviewSchedule();
        entity.setApplication(app);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setStatus("SCHEDULED");
        InterviewSchedule saved = interviewRepo.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getByJobPosition(Long jobPositionId) {
        return interviewRepo.findByApplication_JobPosition_Id(jobPositionId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getByApplication(Long applicationId) {
        return interviewRepo.findByApplication_Id(applicationId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getAll() {
        return interviewRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, String result) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setStatus(status);
        entity.setResult(result);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateOffer(Long id, String offer) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setOffer(offer);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateEvaluation(Long id, String evaluation) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setEvaluation(evaluation);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        interviewRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewScheduleDto getById(Long id) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflict(LocalDateTime startTime, LocalDateTime endTime) {
        // Kiểm tra xem có lịch phỏng vấn nào trùng thời gian không
        List<InterviewSchedule> existingSchedules = interviewRepo.findAll();
        
        for (InterviewSchedule schedule : existingSchedules) {
            // Kiểm tra overlap: (start1 < end2) && (end1 > start2)
            if (startTime.isBefore(schedule.getEndTime()) && endTime.isAfter(schedule.getStartTime())) {
                return true; // Có conflict
            }
        }
        return false; // Không có conflict
    }

    private InterviewScheduleDto toDto(InterviewSchedule entity) {
        InterviewScheduleDto dto = new InterviewScheduleDto();
        dto.setId(entity.getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStatus(entity.getStatus());
        dto.setResult(entity.getResult());
        dto.setOffer(entity.getOffer());
        dto.setEvaluation(entity.getEvaluation());
        
        if (entity.getApplication() != null) {
            dto.setApplicationId(entity.getApplication().getId());
            dto.setApplicantName(entity.getApplication().getFullName());
            dto.setApplicantEmail(entity.getApplication().getEmail());
            dto.setApplicantPhone(entity.getApplication().getPhoneNumber());
            
            if (entity.getApplication().getJobPosition() != null) {
                dto.setJobTitle(entity.getApplication().getJobPosition().getTitle());
                dto.setSalaryRange(entity.getApplication().getJobPosition().getSalaryRange());
            }
        }
        return dto;
    }
} 