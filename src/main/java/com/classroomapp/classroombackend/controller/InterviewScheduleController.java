package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import com.classroomapp.classroombackend.service.UserServiceExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/interview-schedules")
@RequiredArgsConstructor
public class InterviewScheduleController {
    private final InterviewScheduleService interviewService;
    private final EmailService emailService;
    private final UserServiceExtension userService;

    @PostMapping
    public ResponseEntity<InterviewScheduleDto> create(@RequestParam Long applicationId,
                                                       @RequestParam String startTime,
                                                       @RequestParam String endTime) {
        // startTime, endTime dạng ISO string, có thể có 'Z'
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        InterviewScheduleDto dto = interviewService.create(applicationId, start, end);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<InterviewScheduleDto>> getAll() {
        return ResponseEntity.ok(interviewService.getAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InterviewScheduleDto>> getPending() {
        List<InterviewScheduleDto> all = interviewService.getAll();
        LocalDateTime now = LocalDateTime.now();
        List<InterviewScheduleDto> pending = all.stream()
            .filter(i -> i.getEndTime().isBefore(now) && (i.getStatus() == null || i.getStatus().equals("PENDING") || i.getStatus().equals("SCHEDULED")))
            .toList();
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/by-job/{jobPositionId}")
    public ResponseEntity<List<InterviewScheduleDto>> getByJob(@PathVariable Long jobPositionId) {
        return ResponseEntity.ok(interviewService.getByJobPosition(jobPositionId));
    }

    @GetMapping("/by-application/{applicationId}")
    public ResponseEntity<List<InterviewScheduleDto>> getByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getByApplication(applicationId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String result) {
        interviewService.updateStatus(id, status, result);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<?> setResult(@PathVariable Long id, @RequestBody InterviewResultDto body) {
        interviewService.updateStatus(id, body.getStatus(), body.getResult());
        InterviewScheduleDto interview = interviewService.getAll().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        if (interview == null) return ResponseEntity.notFound().build();
        // Gửi mail kết quả
        if ("ACCEPTED".equals(body.getStatus())) {
            emailService.sendEmail(interview.getApplicantEmail(), "Kết quả phỏng vấn", "Chúc mừng bạn đã vượt qua phỏng vấn cho vị trí: " + interview.getJobTitle());
            // Tạo user mới với trạng thái chưa có hợp đồng
            userService.createUserWithoutContract(interview.getApplicantEmail(), interview.getApplicantName(), "TEACHER");
        } else if ("REJECTED".equals(body.getStatus())) {
            emailService.sendEmail(interview.getApplicantEmail(), "Kết quả phỏng vấn", "Rất tiếc, bạn đã không vượt qua phỏng vấn. Lý do: " + (body.getResult() != null ? body.getResult() : "Không có"));
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

class InterviewResultDto {
    private String status;
    private String result;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
} 