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
        return modelMapper.map(saved, JobPositionDto.class);
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
        return modelMapper.map(saved, JobPositionDto.class);
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
        return modelMapper.map(entity, JobPositionDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPositionDto> getAllJobPositions() {
        return jobPositionRepository.findAll().stream()
                .map(entity -> modelMapper.map(entity, JobPositionDto.class))
                .collect(Collectors.toList());
    }
} 