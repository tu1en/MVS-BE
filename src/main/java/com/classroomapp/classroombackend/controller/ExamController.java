package com.classroomapp.classroombackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.exammangement.CreateExamDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamDto;
import com.classroomapp.classroombackend.service.ExamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN') and @classroomSecurityService.isTeacher(#createExamDto.classroomId)")
    public ResponseEntity<ExamDto> createExam(@Valid @RequestBody CreateExamDto createExamDto) {
        ExamDto newExam = examService.createExam(createExamDto);
        return new ResponseEntity<>(newExam, HttpStatus.CREATED);
    }

    @GetMapping("/{examId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDto> getExamById(@PathVariable Long examId) {
        // Here we should also check if the user is a member of the exam's classroom
        // This logic can be added in the service layer or with a more complex security expression
        ExamDto exam = examService.getExamById(examId);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{examId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<ExamDto> updateExam(@PathVariable Long examId, @Valid @RequestBody CreateExamDto createExamDto) {
        // Security check should ensure the user is the teacher of the classroom
        ExamDto updatedExam = examService.updateExam(examId, createExamDto);
        return ResponseEntity.ok(updatedExam);
    }

    @DeleteMapping("/{examId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long examId) {
        // Security check should ensure the user is the teacher of the classroom
        examService.deleteExam(examId);
        return ResponseEntity.noContent().build();
    }
} 