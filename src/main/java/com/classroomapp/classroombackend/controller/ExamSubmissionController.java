package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.exammangement.CreateExamSubmissionDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamSubmissionDto;
import com.classroomapp.classroombackend.dto.exammangement.GradeExamDto;
import com.classroomapp.classroombackend.service.ExamSubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExamSubmissionController {

    private final ExamSubmissionService examSubmissionService;

    @PostMapping("/exams/{examId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamSubmissionDto> startExam(@PathVariable Long examId) {
        return ResponseEntity.ok(examSubmissionService.startExam(examId));
    }

    @PostMapping("/exam-submissions/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamSubmissionDto> submitExam(@PathVariable Long submissionId, @Valid @RequestBody CreateExamSubmissionDto submissionDto) {
        // The service layer will verify that the submission belongs to the authenticated user
        return ResponseEntity.ok(examSubmissionService.submitExam(submissionId, submissionDto));
    }

    @GetMapping("/exams/{examId}/submissions")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<ExamSubmissionDto>> getSubmissionsForExam(@PathVariable Long examId) {
        return ResponseEntity.ok(examSubmissionService.getSubmissionsForExam(examId));
    }

    @GetMapping("/exams/{examId}/submission/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamSubmissionDto> getMySubmissionForExam(@PathVariable Long examId) {
        return ResponseEntity.ok(examSubmissionService.getStudentSubmissionForExam(examId));
    }

    @PutMapping("/exam-submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ExamSubmissionDto> gradeSubmission(@PathVariable Long submissionId, @Valid @RequestBody GradeExamDto gradeDto) {
        return ResponseEntity.ok(examSubmissionService.gradeSubmission(submissionId, gradeDto));
    }
} 