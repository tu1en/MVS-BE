package com.classroomapp.classroombackend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
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
        logger.info("Received submission request from user: {}", principal.getName());
        logger.info("Submission data: assignmentId={}, comment={}, attachments count={}", 
                createSubmissionDto.getAssignmentId(), 
                createSubmissionDto.getComment(),
                createSubmissionDto.getAttachments() != null ? createSubmissionDto.getAttachments().size() : 0);
        
        SubmissionDto result = submissionService.submit(createSubmissionDto, principal.getName());
        logger.info("Submission successful with ID: {}", result.getId());
        return ResponseEntity.ok(result);
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
    @PreAuthorize("@submissionSecurityService.canAccessAssignmentSubmissions(#assignmentId)")
    public ResponseEntity<List<SubmissionDto>> GetSubmissionsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.GetSubmissionsByAssignment(assignmentId));
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @submissionSecurityService.isCurrentStudent(#studentId))")
    public ResponseEntity<List<SubmissionDto>> GetSubmissionsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(submissionService.GetSubmissionsByStudent(studentId));
    }
    
    @GetMapping(value = "/assignment/{assignmentId}/student/{studentId}", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('TEACHER') or (hasRole('STUDENT') and @submissionSecurityService.isCurrentStudent(#studentId))")
    public ResponseEntity<SubmissionDto> GetStudentSubmissionForAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {

        logger.info("🔍 Getting submission for assignment {} and student {}", assignmentId, studentId);
        SubmissionDto submission = submissionService.GetStudentSubmissionForAssignment(assignmentId, studentId);

        if (submission == null) {
            logger.info("📝 No submission found for assignment {} and student {}", assignmentId, studentId);
            return ResponseEntity.notFound().build();
        }

        logger.info("✅ Found submission {} for assignment {} and student {}", submission.getId(), assignmentId, studentId);
        return ResponseEntity.ok(submission);
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

    // Debug endpoint to check submission data
    @GetMapping("/debug/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<Object> debugStudentSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {

        logger.info("🔍 DEBUG: Checking submission for assignment {} and student {}", assignmentId, studentId);

        try {
            // Check if assignment exists
            boolean assignmentExists = submissionService.assignmentExists(assignmentId);
            logger.info("📋 Assignment {} exists: {}", assignmentId, assignmentExists);

            // Check if student exists
            boolean studentExists = submissionService.studentExists(studentId);
            logger.info("👤 Student {} exists: {}", studentId, studentExists);

            // Get all submissions for this assignment
            var allSubmissions = submissionService.GetSubmissionsByAssignment(assignmentId);
            logger.info("📝 Total submissions for assignment {}: {}", assignmentId, allSubmissions.size());

            // Try to get the specific submission
            SubmissionDto submission = submissionService.GetStudentSubmissionForAssignment(assignmentId, studentId);

            return ResponseEntity.ok(Map.of(
                "assignmentExists", assignmentExists,
                "studentExists", studentExists,
                "totalSubmissions", allSubmissions.size(),
                "submissionFound", submission != null,
                "submission", submission
            ));

        } catch (Exception e) {
            logger.error("❌ DEBUG: Error checking submission", e);
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            ));
        }
    }
}