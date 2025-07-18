package com.classroomapp.classroombackend.controller;

import java.security.Principal;
import java.util.List;

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

import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    
    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDto> GetSubmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.GetSubmissionById(id));
    }

    /**
     * Creates or updates a submission for the current user.
     * This single endpoint handles both initial submissions and resubmissions.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionDto> submitOrUpdateSubmission(
            @Valid @RequestBody CreateSubmissionDto createSubmissionDto,
            Principal principal) {
        return ResponseEntity.ok(submissionService.submit(createSubmissionDto, principal.getName()));
    }

    /**
     * @deprecated Use the main POST /api/submissions endpoint which now handles updates.
     */
    @Deprecated
    @PostMapping("/create") // Kept old method temporarily on a different path to avoid breaking changes
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionDto> CreateSubmission(
            @Valid @RequestBody CreateSubmissionDto createSubmissionDto,
            Principal principal) {
        return new ResponseEntity<>(submissionService.CreateSubmission(createSubmissionDto, principal.getName()), 
                HttpStatus.CREATED);
    }

    /**
     * @deprecated Use the main POST /api/submissions endpoint which now handles updates.
     */
    @Deprecated
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionDto> UpdateSubmission(
            @PathVariable Long id,
            @Valid @RequestBody CreateSubmissionDto updateSubmissionDto) {
        return ResponseEntity.ok(submissionService.UpdateSubmission(id, updateSubmissionDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> DeleteSubmission(@PathVariable Long id) {
        submissionService.DeleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<SubmissionDto>> GetSubmissionsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.GetSubmissionsByAssignment(assignmentId));
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SubmissionDto>> GetSubmissionsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(submissionService.GetSubmissionsByStudent(studentId));
    }
    
    @GetMapping(value = "/assignment/{assignmentId}/student/{studentId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<SubmissionDto> GetStudentSubmissionForAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(submissionService.GetStudentSubmissionForAssignment(assignmentId, studentId));
    }
    
    @PutMapping("/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SubmissionDto> GradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionDto gradeSubmissionDto,
            Principal principal) {
        return ResponseEntity.ok(submissionService.GradeSubmission(submissionId, gradeSubmissionDto, principal.getName()));
    }
    
    @GetMapping("/assignment/{assignmentId}/graded")
    public ResponseEntity<List<SubmissionDto>> GetGradedSubmissionsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.GetGradedSubmissionsByAssignment(assignmentId));
    }
    
    @GetMapping("/assignment/{assignmentId}/ungraded")
    public ResponseEntity<List<SubmissionDto>> GetUngradedSubmissionsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.GetUngradedSubmissionsByAssignment(assignmentId));
    }
    
    @GetMapping("/assignment/{assignmentId}/statistics")
    public ResponseEntity<SubmissionService.SubmissionStatistics> GetSubmissionStatisticsForAssignment(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.GetSubmissionStatisticsForAssignment(assignmentId));
    }
} 