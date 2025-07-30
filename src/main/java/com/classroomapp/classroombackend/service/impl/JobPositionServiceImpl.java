package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.JobPositionDto;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.service.JobPositionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPositionServiceImpl implements JobPositionService {
    private final JobPositionRepository jobPositionRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public JobPositionDto createJobPosition(JobPositionDto dto) {
        JobPosition entity = modelMapper.map(dto, JobPosition.class);
        JobPosition saved = jobPositionRepository.save(entity);
        JobPositionDto result = modelMapper.map(saved, JobPositionDto.class);
        if (saved.getRecruitmentPlan() != null) {
            result.setRecruitmentPlanId(saved.getRecruitmentPlan().getId());
            if (saved.getRecruitmentPlan().getStatus() != null) {
                result.setRecruitmentPlanStatus(saved.getRecruitmentPlan().getStatus().name());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public JobPositionDto updateJobPosition(Long id, JobPositionDto dto) {
        JobPosition entity = jobPositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobPosition not found"));
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setSalaryRange(dto.getSalaryRange());
        entity.setQuantity(dto.getQuantity());
        JobPosition saved = jobPositionRepository.save(entity);
        JobPositionDto result = modelMapper.map(saved, JobPositionDto.class);
        if (saved.getRecruitmentPlan() != null) {
            result.setRecruitmentPlanId(saved.getRecruitmentPlan().getId());
            if (saved.getRecruitmentPlan().getStatus() != null) {
                result.setRecruitmentPlanStatus(saved.getRecruitmentPlan().getStatus().name());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteJobPosition(Long id) {
        jobPositionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPositionDto getJobPosition(Long id) {
        JobPosition entity = jobPositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobPosition not found"));
        JobPositionDto result = modelMapper.map(entity, JobPositionDto.class);
        if (entity.getRecruitmentPlan() != null) {
            result.setRecruitmentPlanId(entity.getRecruitmentPlan().getId());
            if (entity.getRecruitmentPlan().getStatus() != null) {
                result.setRecruitmentPlanStatus(entity.getRecruitmentPlan().getStatus().name());
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPositionDto> getAllJobPositions() {
        return jobPositionRepository.findAll().stream()
                .filter(job -> job.getRecruitmentPlan() != null && 
                              job.getRecruitmentPlan().getStatus() == com.classroomapp.classroombackend.model.RecruitmentPlan.Status.OPEN)
                .map(entity -> {
                    JobPositionDto dto = modelMapper.map(entity, JobPositionDto.class);
                    if (entity.getRecruitmentPlan() != null) {
                        dto.setRecruitmentPlanId(entity.getRecruitmentPlan().getId());
                        if (entity.getRecruitmentPlan().getStatus() != null) {
                            dto.setRecruitmentPlanStatus(entity.getRecruitmentPlan().getStatus().name());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPositionDto> getAllJobPositionsWithoutFilter() {
        return jobPositionRepository.findAll().stream()
                .map(entity -> {
                    JobPositionDto dto = modelMapper.map(entity, JobPositionDto.class);
                    if (entity.getRecruitmentPlan() != null) {
                        dto.setRecruitmentPlanId(entity.getRecruitmentPlan().getId());
                        if (entity.getRecruitmentPlan().getStatus() != null) {
                            dto.setRecruitmentPlanStatus(entity.getRecruitmentPlan().getStatus().name());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPositionDto> getJobPositionsByRecruitmentPlan(Long recruitmentPlanId) {
        return jobPositionRepository.findByRecruitmentPlanId(recruitmentPlanId).stream()
                .map(entity -> {
                    JobPositionDto dto = modelMapper.map(entity, JobPositionDto.class);
                    if (entity.getRecruitmentPlan() != null) {
                        dto.setRecruitmentPlanId(entity.getRecruitmentPlan().getId());
                        if (entity.getRecruitmentPlan().getStatus() != null) {
                            dto.setRecruitmentPlanStatus(entity.getRecruitmentPlan().getStatus().name());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 