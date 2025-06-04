package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.SubmissionDto;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.SubmissionService;
import com.classroomapp.classroombackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for teacher-specific submission operations
 */
@RestController
@RequestMapping("/api/teacher/submissions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TEACHER')")
public class TeacherSubmissionController {

    private final SubmissionService submissionService;
    private final UserService userService;
    private final AssignmentService assignmentService;
    
    /**
     * Get all submissions for a classroom
     * @param classroomId The ID of the classroom
     * @return List of submissions for the classroom
     */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<SubmissionDto>> getSubmissionsByClassroom(@PathVariable Long classroomId) {
        log.info("Getting submissions for classroom: {}", classroomId);
        
        // Get all assignments for the classroom
        List<Long> assignmentIds = assignmentService.GetAssignmentsByClassroom(classroomId)
                .stream()
                .map(assignment -> assignment.getId())
                .toList();
        
        // Get submissions for each assignment
        List<SubmissionDto> allSubmissions = new ArrayList<>();
        for (Long assignmentId : assignmentIds) {
            allSubmissions.addAll(submissionService.GetSubmissionsByAssignment(assignmentId));
        }
        
        return ResponseEntity.ok(allSubmissions);
    }
    
    /**
     * Grade a submission
     * @param submissionId The ID of the submission to grade
     * @param gradeSubmissionDto The grading information
     * @return The updated submission
     */
    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<SubmissionDto> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionDto gradeSubmissionDto) {
        
        log.info("Grading submission: {} with score: {}", submissionId, gradeSubmissionDto.getScore());
        
        // Get the current authenticated user (teacher)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long teacherId = userService.FindUserByUsername(username).getId();
        
        // Grade the submission
        SubmissionDto gradedSubmission = submissionService.GradeSubmission(
                submissionId, 
                gradeSubmissionDto, 
                teacherId
        );
        
        return ResponseEntity.ok(gradedSubmission);
    }
    
    /**
     * Get all graded submissions for a classroom
     * @param classroomId The ID of the classroom
     * @return List of graded submissions
     */
    @GetMapping("/classroom/{classroomId}/graded")
    public ResponseEntity<List<SubmissionDto>> getGradedSubmissionsByClassroom(@PathVariable Long classroomId) {
        log.info("Getting graded submissions for classroom: {}", classroomId);
        
        // Get all assignments for the classroom
        List<Long> assignmentIds = assignmentService.GetAssignmentsByClassroom(classroomId)
                .stream()
                .map(assignment -> assignment.getId())
                .toList();
        
        // Get graded submissions for each assignment
        List<SubmissionDto> gradedSubmissions = new ArrayList<>();
        for (Long assignmentId : assignmentIds) {
            gradedSubmissions.addAll(submissionService.GetGradedSubmissionsByAssignment(assignmentId));
        }
        
        return ResponseEntity.ok(gradedSubmissions);
    }
    
    /**
     * Get all ungraded submissions for a classroom
     * @param classroomId The ID of the classroom
     * @return List of ungraded submissions
     */
    @GetMapping("/classroom/{classroomId}/ungraded")
    public ResponseEntity<List<SubmissionDto>> getUngradedSubmissionsByClassroom(@PathVariable Long classroomId) {
        log.info("Getting ungraded submissions for classroom: {}", classroomId);
        
        // Get all assignments for the classroom
        List<Long> assignmentIds = assignmentService.GetAssignmentsByClassroom(classroomId)
                .stream()
                .map(assignment -> assignment.getId())
                .toList();
        
        // Get ungraded submissions for each assignment
        List<SubmissionDto> ungradedSubmissions = new ArrayList<>();
        for (Long assignmentId : assignmentIds) {
            ungradedSubmissions.addAll(submissionService.GetUngradedSubmissionsByAssignment(assignmentId));
        }
        
        return ResponseEntity.ok(ungradedSubmissions);
    }
    
    /**
     * Get submission statistics for a classroom
     * @param classroomId The ID of the classroom
     * @return Submission statistics
     */
    @GetMapping("/classroom/{classroomId}/statistics")
    public ResponseEntity<SubmissionService.SubmissionStatistics> getSubmissionStatistics(@PathVariable Long classroomId) {
        log.info("Getting submission statistics for classroom: {}", classroomId);
        
        // Get the first assignment for the classroom (for simplicity)
        // In a real implementation, you would aggregate statistics for all assignments
        List<Long> assignmentIds = assignmentService.GetAssignmentsByClassroom(classroomId)
                .stream()
                .map(assignment -> assignment.getId())
                .toList();
        
        if (assignmentIds.isEmpty()) {
            // Return empty statistics if no assignments
            SubmissionService.SubmissionStatistics emptyStats = new SubmissionService.SubmissionStatistics();
            emptyStats.setTotalStudents(0);
            emptyStats.setSubmissionCount(0);
            emptyStats.setGradedCount(0);
            emptyStats.setAverageScore(0.0);
            return ResponseEntity.ok(emptyStats);
        }
        
        // Get statistics for the first assignment
        return ResponseEntity.ok(submissionService.GetSubmissionStatisticsForAssignment(assignmentIds.get(0)));
    }
}
