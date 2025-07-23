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
    public void delete(Long id) {
        interviewRepo.deleteById(id);
    }

    private InterviewScheduleDto toDto(InterviewSchedule entity) {
        InterviewScheduleDto dto = modelMapper.map(entity, InterviewScheduleDto.class);
        if (entity.getApplication() != null) {
            dto.setApplicationId(entity.getApplication().getId());
            dto.setApplicantName(entity.getApplication().getFullName());
            dto.setApplicantEmail(entity.getApplication().getEmail());
            if (entity.getApplication().getJobPosition() != null) {
                dto.setJobTitle(entity.getApplication().getJobPosition().getTitle());
            }
        }
        return dto;
    }
} 