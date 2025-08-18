package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
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
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public RecruitmentApplicationDto apply(Long jobPositionId, String fullName, String email, String phoneNumber, String address, MultipartFile cvFile) {
        // Validation
        if (jobPositionId == null) {
            throw new RuntimeException("Thiếu mã vị trí tuyển dụng");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập họ và tên");
        }
        
        // Validate full name length
        if (fullName.trim().length() < 2 || fullName.trim().length() > 100) {
            throw new RuntimeException("Họ và tên phải từ 2 đến 100 ký tự");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập email");
        }
        
        // Validate email format
        if (!email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("Định dạng email không hợp lệ");
        }
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số điện thoại");
        }
        
        // Validate phone number format (Vietnamese format)
        if (!phoneNumber.trim().matches("^0[3|5|7|8|9][0-9]{8}$")) {
            throw new RuntimeException("Số điện thoại phải bắt đầu bằng 03, 05, 07, 08 hoặc 09 và có 10 chữ số");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập địa chỉ");
        }
        
        // Validate address length
        if (address.trim().length() < 10 || address.trim().length() > 500) {
            throw new RuntimeException("Địa chỉ phải từ 10 đến 500 ký tự");
        }
        
        // Validate CV file is required
        if (cvFile == null || cvFile.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên CV (PDF)");
        }
        
        // Validate file size
        if (cvFile.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new RuntimeException("File CV không được lớn hơn 10MB!");
        }
        
        // Validate file format
        String fileName = cvFile.getOriginalFilename();
        if (fileName != null && !fileName.toLowerCase().matches(".*\\.pdf$")) {
            throw new RuntimeException("Chỉ hỗ trợ file PDF !");
        }
        
        JobPosition job = jobPositionRepo.findById(jobPositionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí tuyển dụng"));
        
        // Kiểm tra email có trùng với email đã có sẵn trong hệ thống hoặc đã nộp đơn ứng tuyển
        boolean userEmailExists = userRepository.existsByEmail(email.trim());
        boolean recruitmentEmailExists = recruitmentRepo.existsByEmail(email.trim());
        
        if (userEmailExists || recruitmentEmailExists) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng sử dụng email khác để nộp đơn ứng tuyển!");
        }
        
        // Kiểm tra số điện thoại có trùng với số điện thoại đã có sẵn trong hệ thống hoặc đã nộp đơn ứng tuyển
        boolean userPhoneExists = userRepository.existsByPhoneNumber(phoneNumber.trim());
        boolean recruitmentPhoneExists = recruitmentRepo.existsByPhoneNumber(phoneNumber.trim());
        
        if (userPhoneExists || recruitmentPhoneExists) {
            throw new RuntimeException("Số điện thoại này đã được sử dụng. Vui lòng sử dụng số điện thoại khác để nộp đơn ứng tuyển!");
        }
        
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
        
        entity.setCreatedAt(java.time.LocalDateTime.now());
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển"));
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, String rejectReason) {
        RecruitmentApplication app = recruitmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển"));
        app.setStatus(status);
        app.setRejectReason(rejectReason);
        recruitmentRepo.save(app);
    }

    @Override
    @Transactional
    public void deleteApplication(Long id) {
        if (!recruitmentRepo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đơn ứng tuyển");
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