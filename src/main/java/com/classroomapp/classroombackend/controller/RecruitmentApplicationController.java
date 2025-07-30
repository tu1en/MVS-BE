package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import com.classroomapp.classroombackend.service.RecruitmentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/recruitment-applications")
@RequiredArgsConstructor
@Slf4j
public class RecruitmentApplicationController {
    private final RecruitmentApplicationService recruitmentService;
    private final EmailService emailService;
    private final InterviewScheduleService interviewService;

    @PostMapping("/apply")
    public ResponseEntity<RecruitmentApplicationDto> apply(
            @RequestParam Long jobPositionId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            @RequestParam("cv") MultipartFile cvFile
    ) {
        RecruitmentApplicationDto dto = recruitmentService.apply(jobPositionId, fullName, email, phoneNumber, address, cvFile);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<RecruitmentApplicationDto> createApplication(@RequestBody CreateApplicationRequest request) {
        // Tạo đơn ứng tuyển mới không có file CV
        RecruitmentApplicationDto dto = recruitmentService.apply(
            request.getJobPositionId(), 
            request.getFullName(), 
            request.getEmail(), 
            request.getPhoneNumber(), 
            request.getAddress(), 
            null
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<RecruitmentApplicationDto>> getAllApplications() {
        return ResponseEntity.ok(recruitmentService.getAllApplications());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<RecruitmentApplicationDto>> getApprovedApplications() {
        return ResponseEntity.ok(recruitmentService.getApprovedApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentApplicationDto> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentService.getApplication(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        recruitmentService.updateStatus(id, request.getStatus(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id) {
        recruitmentService.deleteApplication(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        recruitmentService.updateStatus(id, "APPROVED", null);
        // Chỉ gửi mail thông báo duyệt, không tự động tạo lịch phỏng vấn
        RecruitmentApplicationDto app = recruitmentService.getApplication(id);
        try {
            emailService.sendInterviewInvitationEmail(app.getEmail(), app.getFullName(), app.getJobTitle());
        } catch (Exception e) {
            log.error("Failed to send approval email for application {}: {}", id, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody(required = false) RejectReasonDto body) {
        String reason = body != null ? body.getReason() : null;
        recruitmentService.updateStatus(id, "REJECTED", reason);
        // Gửi mail thông báo từ chối
        RecruitmentApplicationDto app = recruitmentService.getApplication(id);
        emailService.sendInterviewRejectionEmail(app.getEmail(), app.getFullName(), app.getJobTitle(), reason != null ? reason : "Không có");
        return ResponseEntity.ok().build();
    }
}

// DTO phụ trợ
class RejectReasonDto {
    private String reason;
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

class StatusUpdateRequest {
    private String status;
    private String reason;
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

class CreateApplicationRequest {
    private Long jobPositionId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    
    public Long getJobPositionId() { return jobPositionId; }
    public void setJobPositionId(Long jobPositionId) { this.jobPositionId = jobPositionId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
} 