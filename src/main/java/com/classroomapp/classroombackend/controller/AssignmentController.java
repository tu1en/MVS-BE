package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.AssignmentRubricDto;
import com.classroomapp.classroombackend.dto.AssignmentSubmissionDto;
import com.classroomapp.classroombackend.dto.BulkGradingDto;
import com.classroomapp.classroombackend.dto.BulkGradingResultDto;
import com.classroomapp.classroombackend.dto.CreateFeedbackDto;
import com.classroomapp.classroombackend.dto.CreateRubricDto;
import com.classroomapp.classroombackend.dto.FeedbackDto;
import com.classroomapp.classroombackend.dto.GradeDto;
import com.classroomapp.classroombackend.dto.GradingAnalyticsDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;
    // private final SubmissionService submissionService;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    // Add endpoint to get all assignments (needed by frontend)
    @GetMapping
    public ResponseEntity<Map<String, Object>> GetAllAssignments() {
        List<AssignmentDto> assignments = assignmentService.GetAllAssignments();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Retrieved all assignments successfully");
        response.put("data", assignments);

        return ResponseEntity.ok(response);
    }

    // Add endpoint to get assignments by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(assignmentService.GetAssignmentsByStudent(studentId));
    }

    // Add endpoint for currently authenticated student
    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> GetAssignmentsForCurrentStudent() {
        log.info("Getting assignments for current student");

        // Get the authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Find user by username
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Get assignments for this student
        List<AssignmentDto> studentAssignments = assignmentService.GetAssignmentsByStudent(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Retrieved student assignments successfully");        response.put("data", studentAssignments);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AssignmentDto> CreateAssignment(@Valid @RequestBody CreateAssignmentDto createAssignmentDto) {
        return new ResponseEntity<>(assignmentService.CreateAssignment(createAssignmentDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDto> UpdateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody CreateAssignmentDto updateAssignmentDto) {
        return ResponseEntity.ok(assignmentService.UpdateAssignment(id, updateAssignmentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> DeleteAssignment(@PathVariable Long id) {
        assignmentService.DeleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(assignmentService.GetAssignmentsByClassroom(classroomId));
    }

    @GetMapping("/classroom/{classroomId}/upcoming")
    public ResponseEntity<List<AssignmentDto>> GetUpcomingAssignmentsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(assignmentService.GetUpcomingAssignmentsByClassroom(classroomId));
    }

    @GetMapping("/classroom/{classroomId}/past")
    public ResponseEntity<List<AssignmentDto>> GetPastAssignmentsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(assignmentService.GetPastAssignmentsByClassroom(classroomId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AssignmentDto>> SearchAssignmentsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(assignmentService.SearchAssignmentsByTitle(title));
    }

    // Advanced Grading APIs for frontend AdvancedGrading.jsx
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmissionDto>> getAssignmentSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentSubmissions(id));
    }

    @PostMapping("/{id}/grade")
    public ResponseEntity<GradeDto> gradeSubmission(
            @PathVariable Long id,
            @Valid @RequestBody GradeSubmissionDto gradeSubmissionDto) {
        return ResponseEntity.ok(assignmentService.gradeSubmission(id, gradeSubmissionDto));
    }

    @GetMapping("/{id}/rubric")
    public ResponseEntity<AssignmentRubricDto> getAssignmentRubric(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentRubric(id));
    }

    @PostMapping("/{id}/rubric")
    public ResponseEntity<AssignmentRubricDto> createAssignmentRubric(
            @PathVariable Long id,
            @Valid @RequestBody CreateRubricDto createRubricDto) {
        return ResponseEntity.ok(assignmentService.createAssignmentRubric(id, createRubricDto));
    }

    @PostMapping("/{id}/bulk-grade")
    public ResponseEntity<BulkGradingResultDto> bulkGradeSubmissions(
            @PathVariable Long id,
            @Valid @RequestBody BulkGradingDto bulkGradingDto) {
        return ResponseEntity.ok(assignmentService.bulkGradeSubmissions(id, bulkGradingDto));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<GradingAnalyticsDto> getGradingAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getGradingAnalytics(id));
    }

    @PostMapping("/{assignmentId}/feedback")
    public ResponseEntity<FeedbackDto> provideFeedback(
            @PathVariable Long assignmentId,
            @Valid @RequestBody CreateFeedbackDto createFeedbackDto) {
        return ResponseEntity.ok(assignmentService.provideFeedback(assignmentId, createFeedbackDto));
    }

    /**
     * Seed the database with mock assignment data
     * This endpoint is for development/testing purposes only
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedMockData(
            @RequestBody(required = false) List<CreateAssignmentDto> mockData) {
        Map<String, Object> result = new HashMap<>();
        List<AssignmentDto> assignmentsCreated = new ArrayList<>();
        List<SubmissionDto> submissionsCreated = new ArrayList<>();

        try {
            log.info("Starting to seed mock assignment data");

            // If no mock data is provided, use default mock data
            if (mockData == null || mockData.isEmpty()) {
                // Find classrooms to associate assignments with
                List<Classroom> classrooms = classroomRepository.findAll();
                if (classrooms.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("status", "error", "message",
                                    "No classrooms available to create assignments"));
                }

                // Create default mock assignments
                CreateAssignmentDto assignment1 = new CreateAssignmentDto();
                assignment1.setTitle("Bài tập 1: Giải phương trình bậc 2");
                assignment1.setDescription("Làm các bài tập từ 1-10 trang 45 sách giáo khoa.");
                assignment1.setDueDate(LocalDateTime.now().plusDays(3));
                assignment1.setPoints(100);
                assignment1.setClassroomId(classrooms.get(0).getId());
                assignment1.setFileAttachmentUrl("https://example.com/attachment1.pdf");

                CreateAssignmentDto assignment2 = new CreateAssignmentDto();
                assignment2.setTitle("Bài tập 2: Viết đoạn văn tả mùa xuân");
                assignment2.setDescription("Viết một đoạn văn khoảng 200 từ tả cảnh mùa xuân ở quê hương em.");
                assignment2.setDueDate(LocalDateTime.now().plusDays(7));
                assignment2.setPoints(50);
                assignment2
                        .setClassroomId(classrooms.size() > 1 ? classrooms.get(1).getId() : classrooms.get(0).getId());

                CreateAssignmentDto assignment3 = new CreateAssignmentDto();
                assignment3.setTitle("Bài tập 3: Viết chương trình Hello World");
                assignment3.setDescription("Viết chương trình Java hiển thị dòng chữ \"Hello World\" ra màn hình.");
                assignment3.setDueDate(LocalDateTime.now().minusDays(2));
                assignment3.setPoints(20);
                assignment3
                        .setClassroomId(classrooms.size() > 2 ? classrooms.get(2).getId() : classrooms.get(0).getId());

                // Save mock assignments
                AssignmentDto created1 = assignmentService.CreateAssignment(assignment1);
                AssignmentDto created2 = assignmentService.CreateAssignment(assignment2);
                AssignmentDto created3 = assignmentService.CreateAssignment(assignment3);
                assignmentsCreated.add(created1);
                assignmentsCreated.add(created2);
                assignmentsCreated.add(created3);

                log.info("Created {} mock assignments", assignmentsCreated.size());

                // Create some example submissions for these assignments
                List<User> students = userRepository.findByRoleId(1); // Assuming role_id 1 is student
                if (!students.isEmpty()) {
                    log.info("Found {} students for creating submissions", students.size());

                    // For assignment 1
                    Assignment assignment1Entity = assignmentService.findEntityById(created1.getId());

                    // Submission 1 for assignment 1
                    Submission submission1 = new Submission();
                    submission1.setAssignment(assignment1Entity);
                    submission1.setStudent(students.get(0));
                    submission1.setComment("Em đã hoàn thành bài tập. Có một số bài em chưa chắc chắn.");
                    submission1.setFileSubmissionUrl("https://example.com/submission1.pdf");
                    submission1.setSubmittedAt(LocalDateTime.now().minusDays(1));
                    submission1.setScore(85);
                    submission1.setFeedback("Bài làm tốt, cần cải thiện phần giải phương trình vô nghiệm.");
                    submission1.setGradedAt(LocalDateTime.now().minusHours(6));
                    if (students.size() > 1) { // Use another student as the grader (assume teaching assistant)
                        submission1.setGradedBy(students.get(1));
                    }

                    Submission savedSubmission1 = submissionRepository.save(submission1);
                    submissionsCreated.add(convertToSubmissionDto(savedSubmission1));

                    // If we have more students, create more submissions
                    if (students.size() > 2) {
                        // Submission 2 for assignment 1
                        Submission submission2 = new Submission();
                        submission2.setAssignment(assignment1Entity);
                        submission2.setStudent(students.get(1));
                        submission2.setComment("Em đã làm xong bài tập.");
                        submission2.setFileSubmissionUrl("https://example.com/submission2.pdf");
                        submission2.setSubmittedAt(LocalDateTime.now().minusDays(2));
                        submission2.setScore(92);
                        submission2.setFeedback("Bài làm rất tốt, đầy đủ và chính xác.");
                        submission2.setGradedAt(LocalDateTime.now().minusHours(12));
                        submission2.setGradedBy(students.get(2));

                        Submission savedSubmission2 = submissionRepository.save(submission2);
                        submissionsCreated.add(convertToSubmissionDto(savedSubmission2));
                    }

                    // For assignment 3 (ungraded submission)
                    Assignment assignment3Entity = assignmentService.findEntityById(created3.getId());
                    Submission submission3 = new Submission();
                    submission3.setAssignment(assignment3Entity);
                    submission3.setStudent(students.get(0));
                    submission3.setComment("Em đã nộp bài, mong thầy/cô góp ý.");
                    submission3.setFileSubmissionUrl("https://example.com/submission3.java");
                    submission3.setSubmittedAt(LocalDateTime.now().minusDays(3));

                    Submission savedSubmission3 = submissionRepository.save(submission3);
                    submissionsCreated.add(convertToSubmissionDto(savedSubmission3));

                    log.info("Created {} mock submissions", submissionsCreated.size());
                } else {
                    log.warn("No students found to create submissions");
                }

            } else {
                // Save provided mock data
                for (CreateAssignmentDto assignmentDto : mockData) {
                    AssignmentDto created = assignmentService.CreateAssignment(assignmentDto);
                    assignmentsCreated.add(created);
                }
                log.info("Created {} mock assignments from provided data", assignmentsCreated.size());
            }

            result.put("status", "success");
            result.put("message", "Mock data has been added to the database");
            result.put("assignments", assignmentsCreated);
            result.put("submissions", submissionsCreated);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to seed mock data", e);
            result.put("status", "error");
            result.put("message", "Failed to seed mock data: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Helper method to convert Submission entity to DTO
     */
    private SubmissionDto convertToSubmissionDto(Submission submission) {
        SubmissionDto dto = new SubmissionDto();
        dto.setId(submission.getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setStudentId(submission.getStudent().getId());
        dto.setStudentName(submission.getStudent().getFullName());
        dto.setComment(submission.getComment());
        dto.setFileSubmissionUrl(submission.getFileSubmissionUrl());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setScore(submission.getScore());
        dto.setFeedback(submission.getFeedback());
        dto.setGradedAt(submission.getGradedAt());
        if (submission.getGradedBy() != null) {
            dto.setGradedById(submission.getGradedBy().getId());
            dto.setGradedByName(submission.getGradedBy().getFullName());
        }
        return dto;
    }
}