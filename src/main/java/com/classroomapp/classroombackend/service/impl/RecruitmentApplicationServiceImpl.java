package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.RecruitmentApplicationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentApplicationServiceImpl implements RecruitmentApplicationService {
    private final RecruitmentApplicationRepository recruitmentRepo;
    private final JobPositionRepository jobPositionRepo;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public RecruitmentApplicationDto apply(Long jobPositionId, String fullName, String email, MultipartFile cvFile) {
        JobPosition job = jobPositionRepo.findById(jobPositionId)
                .orElseThrow(() -> new RuntimeException("Job position not found"));
        String cvUrl = null;
        if (cvFile != null && !cvFile.isEmpty()) {
            cvUrl = fileStorageService.save(cvFile, "cv-files").getFileUrl();
        }
        RecruitmentApplication entity = new RecruitmentApplication();
        entity.setJobPosition(job);
        entity.setFullName(fullName);
        entity.setEmail(email);
        entity.setCvUrl(cvUrl);
        entity.setStatus("PENDING");
        RecruitmentApplication saved = recruitmentRepo.save(entity);
        RecruitmentApplicationDto dto = modelMapper.map(saved, RecruitmentApplicationDto.class);
        dto.setJobPositionId(job.getId());
        dto.setJobTitle(job.getTitle());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentApplicationDto> getAllApplications() {
        return recruitmentRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentApplicationDto> getApplicationsByJob(Long jobPositionId) {
        return recruitmentRepo.findAll().stream()
                .filter(a -> a.getJobPosition() != null && a.getJobPosition().getId().equals(jobPositionId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RecruitmentApplicationDto getApplication(Long id) {
        return recruitmentRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, String rejectReason) {
        RecruitmentApplication app = recruitmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        app.setRejectReason(rejectReason);
        recruitmentRepo.save(app);
    }

    private RecruitmentApplicationDto toDto(RecruitmentApplication entity) {
        RecruitmentApplicationDto dto = modelMapper.map(entity, RecruitmentApplicationDto.class);
        if (entity.getJobPosition() != null) {
            dto.setJobPositionId(entity.getJobPosition().getId());
            dto.setJobTitle(entity.getJobPosition().getTitle());
        }
        return dto;
    }
} 