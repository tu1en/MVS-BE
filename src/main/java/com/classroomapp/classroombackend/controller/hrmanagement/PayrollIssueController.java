package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollIssue;
import com.classroomapp.classroombackend.repository.hrmanagement.PayrollIssueRepository;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/my/payroll/issues")
@RequiredArgsConstructor
@Slf4j
public class PayrollIssueController {

    private final PayrollIssueRepository issueRepository;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ACCOUNTANT','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> createIssue(
            @RequestParam Long userId,
            @RequestParam String period,
            @RequestParam String subject,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile attachment) {
        try {
            String attachmentUrl = null;
            if (attachment != null && !attachment.isEmpty()) {
                var saved = fileStorageService.save(attachment, "payroll-issues");
                attachmentUrl = saved.getFileUrl();
            }

            PayrollIssue issue = new PayrollIssue();
            issue.setUserId(userId);
            issue.setPeriod(period);
            issue.setSubject(subject);
            issue.setDescription(description);
            issue.setAttachmentUrl(attachmentUrl);
            issue.setStatus("OPEN");
            issue.setCreatedAt(LocalDateTime.now());
            issue.setUpdatedAt(LocalDateTime.now());
            issueRepository.save(issue);

            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "CREATED");
            resp.put("id", issue.getId());
            resp.put("attachmentUrl", attachmentUrl);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Failed to create payroll issue", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ACCOUNTANT','MANAGER','ADMIN')")
    public ResponseEntity<List<PayrollIssue>> listMyIssues(@RequestParam Long userId) {
        List<PayrollIssue> list = issueRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','MANAGER','ADMIN')")
    public ResponseEntity<List<PayrollIssue>> listAllIssues() {
        return ResponseEntity.ok(issueRepository.findAll());
    }

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestParam Long id,
            @RequestParam String status) {
        PayrollIssue issue = issueRepository.findById(id).orElse(null);
        if (issue == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Issue not found"));
        }
        issue.setStatus(status);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
        return ResponseEntity.ok(Map.of("status", "UPDATED"));
    }
}


