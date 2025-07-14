package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.service.AttendanceExplanationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance-explanations")
public class AttendanceExplanationController {

    @Autowired
    private AttendanceExplanationService service;

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('TEACHER', 'ACCOUNTANT', 'ADMIN')")
    public ResponseEntity<AttendanceExplanation> submitExplanation(@RequestBody AttendanceExplanation explanation) {
        AttendanceExplanation savedExplanation = service.submitExplanation(explanation);
        return new ResponseEntity<>(savedExplanation, HttpStatus.CREATED);
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<AttendanceExplanation>> getReports(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) ExplanationStatus status,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceExplanation> reports = service.getReports(startDate, endDate, status, department, pageable);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AttendanceExplanation> approveExplanation(@PathVariable Long id, @RequestParam String approverId) {
        AttendanceExplanation updatedExplanation = service.approveExplanation(id, approverId);
        return new ResponseEntity<>(updatedExplanation, HttpStatus.OK);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AttendanceExplanation> rejectExplanation(@PathVariable Long id, @RequestParam String approverId) {
        AttendanceExplanation updatedExplanation = service.rejectExplanation(id, approverId);
        return new ResponseEntity<>(updatedExplanation, HttpStatus.OK);
    }

    @GetMapping("/statistics/reason")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Long>> getReasonStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Map<String, Long> statistics = service.getReasonStatistics(startDate, endDate);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/statistics/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Long>> getStatusStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Map<String, Long> statistics = service.getStatusStatistics(startDate, endDate);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) ExplanationStatus status,
            @RequestParam(required = false) String department) {
        byte[] excelData = service.exportExcel(startDate, endDate, status, department);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "attendance_explanations.xlsx");
        headers.setContentLength(excelData.length);
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}
