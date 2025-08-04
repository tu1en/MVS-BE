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
    public RecruitmentApplicationDto apply(Long jobPositionId, String fullName, String email, String phoneNumber, String address, MultipartFile cvFile) {
        // Validation
        if (jobPositionId == null) {
            throw new RuntimeException("Job position ID is required");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Address is required");
        }
        
        // Validate CV file is required
        if (cvFile == null || cvFile.isEmpty()) {
            throw new RuntimeException("CV file is required");
        }
        
        // Validate file size
        if (cvFile.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new RuntimeException("CV file size must be less than 10MB");
        }
        
        // Validate file format
        String fileName = cvFile.getOriginalFilename();
        if (fileName != null && !fileName.toLowerCase().matches(".*\\.pdf$")) {
            throw new RuntimeException("Chỉ hỗ trợ file PDF !");
        }
        
        JobPosition job = jobPositionRepo.findById(jobPositionId)
                .orElseThrow(() -> new RuntimeException("Job position not found"));
        
        // Save CV file to Firebase Storage
        String cvUrl = fileStorageService.save(cvFile, "recruit-cv").getFileUrl();
        
        RecruitmentApplication entity = new RecruitmentApplication();
        entity.setJobPosition(job);
        entity.setFullName(fullName.trim());
        entity.setEmail(email.trim());
        entity.setPhoneNumber(phoneNumber.trim());
        entity.setAddress(address.trim());
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
    public List<RecruitmentApplicationDto> getApprovedApplications() {
        return recruitmentRepo.findAll().stream()
                .filter(app -> "APPROVED".equals(app.getStatus()))
                .map(this::toDto)
                .collect(Collectors.toList());
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

    @Override
    @Transactional
    public void deleteApplication(Long id) {
        if (!recruitmentRepo.existsById(id)) {
            throw new RuntimeException("Application not found");
        }
        recruitmentRepo.deleteById(id);
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