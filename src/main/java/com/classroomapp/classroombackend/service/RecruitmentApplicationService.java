package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RecruitmentApplicationService {
    RecruitmentApplicationDto apply(Long jobPositionId, String fullName, String email, String phoneNumber, String address, MultipartFile cvFile);
    List<RecruitmentApplicationDto> getAllApplications();
    List<RecruitmentApplicationDto> getApplicationsByJob(Long jobPositionId);
    RecruitmentApplicationDto getApplication(Long id);
    void updateStatus(Long id, String status, String rejectReason);
} 