package com.classroomapp.classroombackend.accountant.controller;

import com.classroomapp.classroombackend.accountant.model.AttendanceExplanation;
import com.classroomapp.classroombackend.accountant.service.AttendanceExplanationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/attendance-explanations")
public class AttendanceExplanationController {
    @Autowired
    private AttendanceExplanationService service;

    // Gửi giải trình (có thể đính kèm file)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AttendanceExplanation> submitExplanation(
            @RequestPart("explanation") AttendanceExplanation explanation,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/attendance-explanations/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String filePath = uploadDir + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            file.transferTo(new File(filePath));
            // Lấy URL file trả về FE
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/" + filePath).toUriString();
            explanation.setAttachmentUrl(fileUrl);
        }
        AttendanceExplanation saved = service.submitExplanation(explanation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Lấy danh sách giải trình (cho quản lý/HR)
    @GetMapping
    public List<AttendanceExplanation> getAllExplanations() {
        return service.getAllExplanations();
    }

    // Duyệt giải trình
    @PutMapping("/{id}/approve")
    public ResponseEntity<AttendanceExplanation> approveExplanation(@PathVariable Long id) {
        AttendanceExplanation approved = service.approveExplanation(id);
        return ResponseEntity.ok(approved);
    }

    // Từ chối giải trình
    @PutMapping("/{id}/reject")
    public ResponseEntity<AttendanceExplanation> rejectExplanation(@PathVariable Long id) {
        AttendanceExplanation rejected = service.rejectExplanation(id);
        return ResponseEntity.ok(rejected);
    }

    // Lấy giải trình theo id (tuỳ chọn)
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceExplanation> getExplanationById(@PathVariable Long id) {
        return service.getExplanationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
