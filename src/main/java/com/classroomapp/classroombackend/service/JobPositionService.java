package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.JobPositionDto;
import java.util.List;

public interface JobPositionService {
    JobPositionDto createJobPosition(JobPositionDto dto);
    JobPositionDto updateJobPosition(Long id, JobPositionDto dto);
    void deleteJobPosition(Long id);
    JobPositionDto getJobPosition(Long id);
    List<JobPositionDto> getAllJobPositions();
} 