package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.RecruitmentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecruitmentApplicationController {
    private final RecruitmentApplicationService recruitmentService;
    private final EmailService emailService;

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

    @GetMapping
    public ResponseEntity<?> getAllApplications() {
        return ResponseEntity.ok(recruitmentService.getAllApplications());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        recruitmentService.updateStatus(id, "APPROVED", null);
        // Gửi mail thông báo duyệt
        RecruitmentApplicationDto app = recruitmentService.getApplication(id);
        emailService.sendInterviewInvitationEmail(app.getEmail(), app.getFullName(), app.getJobTitle());
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