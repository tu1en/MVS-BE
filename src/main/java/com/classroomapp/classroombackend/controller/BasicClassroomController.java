package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.exammangement.ExamDto;
import com.classroomapp.classroombackend.service.ExamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class BasicClassroomController {

    private final ExamService examService;

    // ================= Exam Endpoints ================= //

    @GetMapping("/{id}/exams")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamDto>> getExamsInClassroom(@PathVariable Long id) {
        List<ExamDto> exams = examService.getExamsByClassroomId(id);
        return ResponseEntity.ok(exams);
    }
}