package com.classroomapp.classroombackend.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.AssignmentRubricDto;
import com.classroomapp.classroombackend.dto.AssignmentSubmissionDto;
import com.classroomapp.classroombackend.dto.BulkGradingDto;
import com.classroomapp.classroombackend.dto.BulkGradingResultDto;
import com.classroomapp.classroombackend.dto.CreateFeedbackDto;
import com.classroomapp.classroombackend.dto.CreateRubricDto;
import com.classroomapp.classroombackend.dto.FeedbackDto;
import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.GradeDto;
import com.classroomapp.classroombackend.dto.GradingAnalyticsDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.assignmentmanagement.SubmissionAttachment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final FileStorageService fileStorageService;
    private final SubmissionService submissionService;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentMessageRepository studentMessageRepository;

    // Add endpoint to get all assignments (needed by frontend)
    @GetMapping
    // @PreAuthorize("permitAll()") // Temporarily disable security for testing
    public ResponseEntity<Map<String, Object>> GetAllAssignments() {
        List<AssignmentDto> assignments = assignmentService.GetAllAssignments();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Retrieved all assignments successfully");
        response.put("data", assignments);

        return ResponseEntity.ok(response);
    }

    // Add endpoint to get assignments for the current teacher
    @GetMapping("/current-teacher")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsByCurrentTeacher() {
        log.info("🔍 AssignmentController.GetAssignmentsByCurrentTeacher called");
        try {
            List<AssignmentDto> assignments = assignmentService.getAssignmentsByCurrentTeacher();
            log.info("✅ Successfully retrieved {} assignments for current teacher", assignments.size());
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            log.error("❌ Error retrieving assignments for current teacher: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Add endpoint to get assignments by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(assignmentService.GetAssignmentsByStudent(studentId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<AssignmentDto>> getAssignmentsByTeacher(@PathVariable Long teacherId) {
        log.info("🔍 AssignmentController.getAssignmentsByTeacher called with teacherId: {}", teacherId);
        try {
            List<AssignmentDto> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
            log.info("✅ Successfully retrieved {} assignments for teacher {}", assignments.size(), teacherId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            log.error("❌ Error retrieving assignments for teacher {}: {}", teacherId, e.getMessage(), e);
            throw e;
        }
    }

    // Add endpoint for currently authenticated student
    @GetMapping("/student/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsForCurrentStudent() {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCurrentStudent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> GetAssignmentById(@PathVariable Long id) {
        AssignmentDto assignment = assignmentService.GetAssignmentById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Assignment retrieved successfully");
        response.put("data", assignment);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<AssignmentDto> CreateAssignment(@RequestBody CreateAssignmentDto createAssignmentDto,
                                                         java.security.Principal principal) {
        String teacherUsername = principal != null ? principal.getName() : null;
        return new ResponseEntity<>(assignmentService.CreateAssignment(createAssignmentDto, teacherUsername), HttpStatus.CREATED);
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
    @PreAuthorize("permitAll()") // Temporarily disable security for testing
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsByClassroom(@PathVariable Long classroomId) {
        log.info("🔍 GetAssignmentsByClassroom called with classroomId: {}", classroomId);
        List<AssignmentDto> assignments = assignmentService.GetAssignmentsByClassroom(classroomId);
        log.info("✅ Found {} assignments for classroom {}", assignments.size(), classroomId);
        assignments.forEach(assignment ->
            log.info("📝 Assignment: id={}, title={}, classroomId={}",
                assignment.getId(), assignment.getTitle(), assignment.getClassroomId())
        );
        return ResponseEntity.ok(assignments);
    }

    // Debug endpoint to check classroom and student data
    @GetMapping("/debug/classroom/{classroomId}")
    @PreAuthorize("permitAll()") // Temporarily disable security for testing
    public ResponseEntity<Map<String, Object>> debugClassroomData(@PathVariable Long classroomId) {
        log.info("🔍 Debug endpoint called for classroomId: {}", classroomId);
        Map<String, Object> debugInfo = new HashMap<>();

        try {
            // Get assignments
            List<AssignmentDto> assignments = assignmentService.GetAssignmentsByClassroom(classroomId);
            debugInfo.put("assignmentCount", assignments.size());
            debugInfo.put("assignments", assignments);

            // Get classroom info
            // Note: You might need to inject ClassroomService here
            debugInfo.put("classroomId", classroomId);

            log.info("📊 Debug info for classroom {}: {} assignments", classroomId, assignments.size());

        } catch (Exception e) {
            log.error("❌ Error in debug endpoint: {}", e.getMessage(), e);
            debugInfo.put("error", e.getMessage());
        }

        return ResponseEntity.ok(debugInfo);
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
    
    // Temporary endpoint to create sample assignments for testing
    @GetMapping("/create-samples/{classroomId}")
    public ResponseEntity<Map<String, Object>> createSampleAssignments(@PathVariable Long classroomId) {
        try {
            // Find classroom
            Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
            if (classroom == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Classroom not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create sample assignments
            List<Assignment> sampleAssignments = new ArrayList<>();
            
            Assignment assignment1 = new Assignment();
            assignment1.setTitle("Bài tập 1: Giới thiệu bản thân");
            assignment1.setDescription("Viết một đoạn văn ngắn giới thiệu về bản thân, sở thích và mục tiêu học tập. Độ dài khoảng 200-300 từ.");
            assignment1.setDueDate(LocalDateTime.of(2024, 12, 30, 23, 59, 59));
            assignment1.setPoints(100);
            assignment1.setClassroom(classroom);
            sampleAssignments.add(assignment1);
            
            Assignment assignment2 = new Assignment();
            assignment2.setTitle("Bài tập 2: Tìm hiểu về lập trình");
            assignment2.setDescription("Nghiên cứu về một ngôn ngữ lập trình mà bạn quan tâm. Trình bày ưu điểm, nhược điểm và ứng dụng thực tế.");
            assignment2.setDueDate(LocalDateTime.of(2025, 1, 15, 23, 59, 59));
            assignment2.setPoints(150);
            assignment2.setClassroom(classroom);
            sampleAssignments.add(assignment2);
            
            Assignment assignment3 = new Assignment();
            assignment3.setTitle("Bài tập 3: Dự án nhóm");
            assignment3.setDescription("Làm việc theo nhóm để tạo ra một ứng dụng web đơn giản. Yêu cầu có giao diện thân thiện và chức năng cơ bản.");
            assignment3.setDueDate(LocalDateTime.of(2025, 2, 1, 23, 59, 59));
            assignment3.setPoints(200);
            assignment3.setClassroom(classroom);
            sampleAssignments.add(assignment3);
            
            // Save assignments using service
            for (Assignment assignment : sampleAssignments) {
                assignmentService.CreateAssignment(convertToCreateDto(assignment), "system");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Sample assignments created successfully");
            response.put("count", sampleAssignments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error creating sample assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Helper method to convert Assignment to CreateAssignmentDto
    private CreateAssignmentDto convertToCreateDto(Assignment assignment) {
        CreateAssignmentDto dto = new CreateAssignmentDto();
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setPoints(assignment.getPoints());
        dto.setClassroomId(assignment.getClassroom().getId());
        return dto;
    }

    // Advanced Grading APIs for frontend AdvancedGrading.jsx
    @GetMapping("/{id}/submissions")
    @PreAuthorize("permitAll()") // Allow access for testing
    public ResponseEntity<List<AssignmentSubmissionDto>> getAssignmentSubmissions(@PathVariable Long id) {
        try {
            log.info("Getting submissions for assignment ID: " + id);
            List<AssignmentSubmissionDto> submissions = assignmentService.getAssignmentSubmissions(id);
            log.info("Found {} submissions for assignment {}", submissions.size(), id);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("Error getting submissions for assignment " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{assignmentId}/submissions/{submissionId}/grade")
    public ResponseEntity<GradeDto> gradeSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionDto gradeSubmissionDto) {
        log.info("Grading submission {} for assignment {}", submissionId, assignmentId);
        return ResponseEntity.ok(assignmentService.gradeSubmission(assignmentId, submissionId, gradeSubmissionDto));
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
                String defaultTeacherUsername = classrooms.get(0).getTeacher().getEmail();


                // Create default mock assignments
                CreateAssignmentDto assignment1 = new CreateAssignmentDto();
                assignment1.setTitle("Bài tập 1: Giải phương trình bậc 2");
                assignment1.setDescription("Làm các bài tập từ 1-10 trang 45 sách giáo khoa.");
                assignment1.setDueDate(LocalDateTime.now().plusDays(3));
                assignment1.setPoints(100);
                assignment1.setClassroomId(classrooms.get(0).getId());
                assignment1.setAttachments(new ArrayList<>()); // No attachments for this mock

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
                AssignmentDto created1 = assignmentService.CreateAssignment(assignment1, defaultTeacherUsername);
                AssignmentDto created2 = assignmentService.CreateAssignment(assignment2, defaultTeacherUsername);
                AssignmentDto created3 = assignmentService.CreateAssignment(assignment3, defaultTeacherUsername);
                assignmentsCreated.addAll(List.of(created1, created2, created3));

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
                    SubmissionAttachment attachment1 = new SubmissionAttachment();
                    attachment1.setFileUrl("https://example.com/submission1.pdf");
                    attachment1.setFileName("submission1.pdf");
                    submission1.addAttachment(attachment1);
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
                        SubmissionAttachment attachment2 = new SubmissionAttachment();
                        attachment2.setFileUrl("https://example.com/submission2.pdf");
                        attachment2.setFileName("submission2.pdf");
                        submission2.addAttachment(attachment2);
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
                    SubmissionAttachment attachment3 = new SubmissionAttachment();
                    attachment3.setFileUrl("https://example.com/submission3.java");
                    attachment3.setFileName("submission3.java");
                    submission3.addAttachment(attachment3);
                    submission3.setSubmittedAt(LocalDateTime.now().minusDays(3));

                    Submission savedSubmission3 = submissionRepository.save(submission3);
                    submissionsCreated.add(convertToSubmissionDto(savedSubmission3));

                    log.info("Created {} mock submissions", submissionsCreated.size());
                } else {
                    log.warn("No students found to create submissions");
                }

            } else {
                // Find the first teacher to act as the creator for all mock data
                User teacher = userRepository.findAll().stream()
                        .filter(u -> u.getRoleId() == 2 || u.getRoleId() == 3)
                        .findFirst()
                        .orElse(null);

                if (teacher == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("status", "error", "message", "No teachers available to create assignments"));
                }
                String teacherUsername = teacher.getEmail();

                for (CreateAssignmentDto mockAssignment : mockData) {
                    assignmentsCreated.add(assignmentService.CreateAssignment(mockAssignment, teacherUsername));
                }
            }

            log.info("Finished seeding mock assignments. Created {} assignments.", assignmentsCreated.size());

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
        if (submission.getAttachments() != null && !submission.getAttachments().isEmpty()) {
            dto.setFileSubmissionUrl(submission.getAttachments().get(0).getFileUrl());
        }
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

    @PostMapping("/upload")
    @PreAuthorize("permitAll()") // Allow access for testing
    public ResponseEntity<Map<String, Object>> uploadAssignmentFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "assignmentId", required = false) Long assignmentId) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Validate file
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("status", "error");
                response.put("message", "File size too large. Maximum 10MB allowed.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".pdf") && !filename.endsWith(".docx") 
                && !filename.endsWith(".doc") && !filename.endsWith(".txt") && !filename.endsWith(".zip"))) {
                response.put("status", "error");
                response.put("message", "Invalid file type. Only PDF, DOCX, DOC, TXT, ZIP files are allowed.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save file to a temporary location
            Path tempFile = Files.createTempFile("upload-", ".tmp");
            file.transferTo(tempFile);

            // Upload file to Firebase Storage
            FileUploadResponse uploadResponse;
            try {
                log.info("Uploading file to Firebase Storage: " + filename);
                uploadResponse = fileStorageService.save(file, "assignments");
                log.info("File uploaded successfully to Firebase: " + uploadResponse.getFileUrl());
            } catch (Exception e) {
                log.error("Error uploading file to Firebase: " + e.getMessage());
                response.put("status", "error");
                response.put("message", "Failed to upload file to Firebase: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            // Create submission record in database
            try {
                // For now, use a test student ID. In production, get from SecurityContext
                Long studentId = 185L; // Use the actual ID from database
                
                // Get assignment and student entities
                Assignment assignment = assignmentService.findEntityById(assignmentId);
                User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
                
                // Check if submission already exists
                Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndStudent(assignment, student);
                
                Submission submission;
                if (existingSubmission.isEmpty()) {
                    // Create new submission
                    submission = new Submission();
                    submission.setAssignment(assignment);
                    submission.setStudent(student);
                    submission.setSubmittedAt(LocalDateTime.now());
                    submission.setComment("File submission: " + filename);
                } else {
                    // Update existing submission
                    submission = existingSubmission.get();
                    submission.setSubmittedAt(LocalDateTime.now());
                    submission.setComment("File submission: " + filename);
                }
                
                // Save submission
                submission = submissionRepository.save(submission);
                log.info("Submission created/updated with ID: " + submission.getId());
                
                // Create submission attachment
                SubmissionAttachment attachment = new SubmissionAttachment();
                attachment.setSubmission(submission);
                attachment.setFileName(uploadResponse.getFileName());
                attachment.setFileUrl(uploadResponse.getFileUrl());
                attachment.setFileType(uploadResponse.getFileType());
                attachment.setFileSize(uploadResponse.getSize());
                
                // Add attachment to submission
                submission.addAttachment(attachment);
                
                // Save submission again to persist the attachment
                submissionRepository.save(submission);
                log.info("Submission attachment created for file: " + filename);
                
            } catch (Exception e) {
                log.error("Error creating submission record: " + e.getMessage(), e);
                // Don't fail the upload if database operation fails
            }
            
            // Return success response
            response.put("status", "success");
            response.put("message", "File uploaded successfully to Firebase Storage");
            response.put("filename", uploadResponse.getFileName());
            response.put("originalFilename", filename);
            response.put("size", uploadResponse.getSize());
            response.put("url", uploadResponse.getFileUrl());
            response.put("fileType", uploadResponse.getFileType());
            response.put("assignmentId", assignmentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Upload error: " + e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Test endpoint to debug submissions
    @GetMapping("/{id}/submissions-debug")
    @PreAuthorize("permitAll()") // Allow access for testing
    public ResponseEntity<Map<String, Object>> getAssignmentSubmissionsDebug(@PathVariable Long id) {
        try {
            log.info("Debug: Getting submissions for assignment ID: " + id);
            Map<String, Object> result = new HashMap<>();
            
            // Get raw submissions from repository
            List<Submission> submissions = submissionRepository.findByAssignmentId(id);
            log.info("Debug: Found {} raw submissions", submissions.size());
            
            result.put("submissionCount", submissions.size());
            result.put("assignmentId", id);
            
            if (!submissions.isEmpty()) {
                Submission first = submissions.get(0);
                Map<String, Object> firstSubmission = new HashMap<>();
                firstSubmission.put("id", first.getId());
                firstSubmission.put("comment", first.getComment());
                firstSubmission.put("submittedAt", first.getSubmittedAt());
                firstSubmission.put("hasStudent", first.getStudent() != null);
                if (first.getStudent() != null) {
                    firstSubmission.put("studentId", first.getStudent().getId());
                    firstSubmission.put("studentName", first.getStudent().getFullName());
                }
                firstSubmission.put("attachmentCount", first.getAttachments().size());
                result.put("firstSubmission", firstSubmission);
            }
            
            // Try the service method
            try {
                List<AssignmentSubmissionDto> dtos = assignmentService.getAssignmentSubmissions(id);
                result.put("dtoCount", dtos.size());
                result.put("serviceWorking", true);
            } catch (Exception e) {
                result.put("serviceWorking", false);
                result.put("serviceError", e.getMessage());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Debug endpoint error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to check classroom enrollments and submissions relationship
    @GetMapping("/debug/classroom/{classroomId}/submissions-analysis")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> debugClassroomSubmissionsAnalysis(@PathVariable Long classroomId) {
        try {
            log.info("Debug: Analyzing submissions for classroom {}", classroomId);
            Map<String, Object> result = new HashMap<>();

            // Get classroom info
            Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);
            if (classroomOpt.isPresent()) {
                Classroom classroom = classroomOpt.get();
                result.put("classroomName", classroom.getName());
                result.put("classroomId", classroom.getId());
            } else {
                result.put("classroomExists", false);
                return ResponseEntity.ok(result);
            }

            // Get enrollments
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findById_ClassroomId(classroomId);
            result.put("enrollmentCount", enrollments.size());

            List<Map<String, Object>> enrollmentDetails = new ArrayList<>();
            for (ClassroomEnrollment enrollment : enrollments) {
                Map<String, Object> enrollmentInfo = new HashMap<>();
                enrollmentInfo.put("userId", enrollment.getUser().getId());
                enrollmentInfo.put("userName", enrollment.getUser().getFullName());
                enrollmentInfo.put("userEmail", enrollment.getUser().getEmail());
                enrollmentInfo.put("userRole", enrollment.getUser().getRole());
                enrollmentDetails.add(enrollmentInfo);
            }
            result.put("enrollments", enrollmentDetails);

            // Get assignments for this classroom
            List<Assignment> assignments = assignmentRepository.findByClassroomId(classroomId);
            result.put("assignmentCount", assignments.size());

            List<Map<String, Object>> assignmentDetails = new ArrayList<>();
            for (Assignment assignment : assignments) {
                Map<String, Object> assignmentInfo = new HashMap<>();
                assignmentInfo.put("assignmentId", assignment.getId());
                assignmentInfo.put("assignmentTitle", assignment.getTitle());

                // Get submissions for this assignment
                List<Submission> submissions = submissionRepository.findByAssignmentId(assignment.getId());
                assignmentInfo.put("submissionCount", submissions.size());

                List<Map<String, Object>> submissionDetails = new ArrayList<>();
                for (Submission submission : submissions) {
                    Map<String, Object> submissionInfo = new HashMap<>();
                    submissionInfo.put("submissionId", submission.getId());
                    submissionInfo.put("studentId", submission.getStudent() != null ? submission.getStudent().getId() : null);
                    submissionInfo.put("studentName", submission.getStudent() != null ? submission.getStudent().getFullName() : "NULL");
                    submissionInfo.put("studentEmail", submission.getStudent() != null ? submission.getStudent().getEmail() : "NULL");
                    submissionInfo.put("submittedAt", submission.getSubmittedAt());
                    submissionInfo.put("score", submission.getScore());

                    // Check if student is enrolled in classroom
                    boolean isEnrolled = enrollments.stream()
                        .anyMatch(e -> submission.getStudent() != null &&
                                     e.getUser().getId().equals(submission.getStudent().getId()));
                    submissionInfo.put("studentEnrolledInClassroom", isEnrolled);

                    submissionDetails.add(submissionInfo);
                }
                assignmentInfo.put("submissions", submissionDetails);
                assignmentDetails.add(assignmentInfo);
            }
            result.put("assignments", assignmentDetails);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Debug classroom submissions analysis error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to clean invalid submissions (from non-enrolled students)
    @DeleteMapping("/debug/classroom/{classroomId}/clean-invalid-submissions")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> cleanInvalidSubmissions(@PathVariable Long classroomId) {
        try {
            log.info("Controller: Delegating cleanup to service for classroom {}", classroomId);
            Map<String, Object> result = assignmentService.cleanInvalidSubmissionsForClassroom(classroomId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error cleaning invalid submissions for classroom {}: {}", classroomId, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // New endpoint to clean invalid submissions for a specific assignment
    @DeleteMapping("/debug/assignment/{assignmentId}/clean-invalid-submissions")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> cleanInvalidSubmissionsForAssignment(@PathVariable Long assignmentId) {
        try {
            log.info("Controller: Cleaning invalid submissions for assignment {}", assignmentId);
            int deletedCount = submissionService.cleanInvalidSubmissionsForAssignment(assignmentId);

            Map<String, Object> result = new HashMap<>();
            result.put("assignmentId", assignmentId);
            result.put("deletedSubmissions", deletedCount);
            result.put("message", "Successfully cleaned " + deletedCount + " invalid submissions for assignment " + assignmentId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error cleaning invalid submissions for assignment {}: {}", assignmentId, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to check submissions consistency
    @GetMapping("/debug/submissions-consistency-check")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> checkSubmissionsConsistency() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Query để tìm submissions từ non-enrolled students
            List<Object[]> invalidSubmissions = submissionRepository.findSubmissionsFromNonEnrolledStudents();

            result.put("totalInvalidSubmissions", invalidSubmissions.size());
            result.put("invalidSubmissions", invalidSubmissions);
            result.put("status", invalidSubmissions.isEmpty() ? "HEALTHY" : "ISSUES_FOUND");
            result.put("message", invalidSubmissions.isEmpty() ?
                "✅ All submissions are from enrolled students" :
                "🚨 Found " + invalidSubmissions.size() + " submissions from non-enrolled students");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking submissions consistency: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to clean ALL invalid submissions across all assignments
    @DeleteMapping("/debug/clean-all-invalid-submissions")
    @PreAuthorize("permitAll()") // Debug endpoint
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanAllInvalidSubmissions() {
        try {
            log.info("Controller: Cleaning ALL invalid submissions across all assignments");

            // Get all invalid submissions first
            List<Object[]> invalidSubmissions = submissionRepository.findSubmissionsFromNonEnrolledStudents();
            int totalInvalid = invalidSubmissions.size();

            if (totalInvalid == 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("message", "✅ No invalid submissions found - database is clean");
                result.put("deletedSubmissions", 0);
                result.put("status", "ALREADY_CLEAN");
                return ResponseEntity.ok(result);
            }

            // Delete invalid submissions one by one
            List<Long> invalidSubmissionIds = invalidSubmissions.stream()
                .map(row -> ((Number) row[0]).longValue()) // submission_id is first column
                .collect(Collectors.toList());

            int deletedCount = 0;
            for (Long submissionId : invalidSubmissionIds) {
                try {
                    submissionRepository.deleteById(submissionId);
                    deletedCount++;
                    log.info("Deleted invalid submission ID: {}", submissionId);
                } catch (Exception e) {
                    log.error("Failed to delete submission ID {}: {}", submissionId, e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalInvalidFound", totalInvalid);
            result.put("deletedSubmissions", deletedCount);
            result.put("invalidSubmissionDetails", invalidSubmissions);
            result.put("message", "🧹 Successfully cleaned " + deletedCount + " invalid submissions from " + totalInvalid + " found");
            result.put("status", "CLEANED");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error cleaning all invalid submissions: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to check attendance consistency
    @GetMapping("/debug/attendance-consistency-check")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> checkAttendanceConsistency() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Query để tìm attendance từ non-enrolled students
            List<Object[]> invalidAttendance = attendanceRepository.findAttendanceFromNonEnrolledStudents();

            result.put("totalInvalidAttendance", invalidAttendance.size());
            result.put("invalidAttendance", invalidAttendance);
            result.put("status", invalidAttendance.isEmpty() ? "HEALTHY" : "ISSUES_FOUND");
            result.put("message", invalidAttendance.isEmpty() ?
                "✅ All attendance records are from enrolled students" :
                "🚨 Found " + invalidAttendance.size() + " attendance records from non-enrolled students");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking attendance consistency: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to check messages consistency
    @GetMapping("/debug/messages-consistency-check")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> checkMessagesConsistency() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Query để tìm messages without proper classroom context
            List<Object[]> invalidMessages = studentMessageRepository.findMessagesWithoutClassroomContext();

            result.put("totalInvalidMessages", invalidMessages.size());
            result.put("invalidMessages", invalidMessages);
            result.put("status", invalidMessages.isEmpty() ? "HEALTHY" : "ISSUES_FOUND");
            result.put("message", invalidMessages.isEmpty() ?
                "✅ All messages have proper classroom context" :
                "🚨 Found " + invalidMessages.size() + " messages without proper classroom context");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking messages consistency: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to check duplicate enrollments
    @GetMapping("/debug/enrollments-consistency-check")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> checkEnrollmentsConsistency() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Query để tìm duplicate enrollments
            List<Object[]> duplicateEnrollments = classroomEnrollmentRepository.findDuplicateEnrollments();

            result.put("totalDuplicateEnrollments", duplicateEnrollments.size());
            result.put("duplicateEnrollments", duplicateEnrollments);
            result.put("status", duplicateEnrollments.isEmpty() ? "HEALTHY" : "ISSUES_FOUND");
            result.put("message", duplicateEnrollments.isEmpty() ?
                "✅ No duplicate enrollment records found" :
                "🚨 Found " + duplicateEnrollments.size() + " duplicate enrollment records");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking enrollments consistency: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Comprehensive data consistency check
    @GetMapping("/debug/comprehensive-consistency-check")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> comprehensiveConsistencyCheck() {
        try {
            Map<String, Object> result = new HashMap<>();
            int totalIssues = 0;

            // Check submissions
            List<Object[]> invalidSubmissions = submissionRepository.findSubmissionsFromNonEnrolledStudents();
            result.put("submissions", Map.of(
                "count", invalidSubmissions.size(),
                "details", invalidSubmissions,
                "description", "Submissions from students not enrolled in the classroom"
            ));
            totalIssues += invalidSubmissions.size();

            // Check attendance
            List<Object[]> invalidAttendance = attendanceRepository.findAttendanceFromNonEnrolledStudents();
            result.put("attendance", Map.of(
                "count", invalidAttendance.size(),
                "details", invalidAttendance,
                "description", "Attendance records for students not enrolled in the classroom"
            ));
            totalIssues += invalidAttendance.size();

            // Check messages
            List<Object[]> invalidMessages = studentMessageRepository.findMessagesWithoutClassroomContext();
            result.put("messages", Map.of(
                "count", invalidMessages.size(),
                "details", invalidMessages,
                "description", "Messages between users without proper classroom context"
            ));
            totalIssues += invalidMessages.size();

            // Check duplicate enrollments
            List<Object[]> duplicateEnrollments = classroomEnrollmentRepository.findDuplicateEnrollments();
            result.put("duplicateEnrollments", Map.of(
                "count", duplicateEnrollments.size(),
                "details", duplicateEnrollments,
                "description", "Duplicate enrollment records for same student-classroom pair"
            ));
            totalIssues += duplicateEnrollments.size();

            // Summary
            result.put("summary", Map.of(
                "totalIssuesFound", totalIssues,
                "status", totalIssues == 0 ? "HEALTHY" : "ISSUES_FOUND",
                "message", totalIssues == 0 ?
                    "✅ No data consistency issues found" :
                    "🚨 Found " + totalIssues + " data consistency issues"
            ));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during comprehensive consistency check: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to clean invalid messages
    @DeleteMapping("/debug/clean-all-invalid-messages")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> cleanAllInvalidMessages() {
        try {
            log.info("Starting cleanup of invalid messages...");

            // Get invalid messages first
            List<Object[]> invalidMessages = studentMessageRepository.findMessagesWithoutClassroomContext();

            Map<String, Object> result = new HashMap<>();
            int cleanedCount = 0;

            // Clean each invalid message
            for (Object[] row : invalidMessages) {
                try {
                    Long messageId = ((Number) row[0]).longValue();
                    studentMessageRepository.deleteById(messageId);
                    cleanedCount++;
                    log.info("Deleted invalid message ID: {} from sender: {} to recipient: {}",
                        messageId, row[2], row[4]);
                } catch (Exception e) {
                    log.error("Failed to delete message ID {}: {}", row[0], e.getMessage());
                }
            }

            result.put("totalCleaned", cleanedCount);
            result.put("originalInvalidCount", invalidMessages.size());
            result.put("cleanedMessages", invalidMessages);
            result.put("status", "CLEANED");
            result.put("message", cleanedCount > 0 ?
                "🧹 Successfully cleaned " + cleanedCount + " invalid messages" :
                "✅ No invalid messages found to clean");

            log.info("Cleanup completed. Cleaned {} invalid messages", cleanedCount);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error during invalid messages cleanup: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to clean invalid attendance records
    @DeleteMapping("/debug/clean-all-invalid-attendance")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> cleanAllInvalidAttendance() {
        try {
            log.info("Starting cleanup of invalid attendance records...");

            // Get invalid attendance first
            List<Object[]> invalidAttendance = attendanceRepository.findAttendanceFromNonEnrolledStudents();

            Map<String, Object> result = new HashMap<>();
            int cleanedCount = 0;

            // Clean each invalid attendance record
            for (Object[] row : invalidAttendance) {
                try {
                    Long attendanceId = ((Number) row[0]).longValue();
                    attendanceRepository.deleteById(attendanceId);
                    cleanedCount++;
                    log.info("Deleted invalid attendance ID: {} for student: {} in classroom: {}",
                        attendanceId, row[3], row[6]);
                } catch (Exception e) {
                    log.error("Failed to delete attendance ID {}: {}", row[0], e.getMessage());
                }
            }

            result.put("totalCleaned", cleanedCount);
            result.put("originalInvalidCount", invalidAttendance.size());
            result.put("cleanedAttendance", invalidAttendance);
            result.put("status", "CLEANED");
            result.put("message", cleanedCount > 0 ?
                "🧹 Successfully cleaned " + cleanedCount + " invalid attendance records" :
                "✅ No invalid attendance records found to clean");

            log.info("Cleanup completed. Cleaned {} invalid attendance records", cleanedCount);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error during invalid attendance cleanup: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Comprehensive cleanup endpoint
    @DeleteMapping("/debug/clean-all-data-issues")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> cleanAllDataIssues() {
        try {
            log.info("Starting comprehensive data cleanup...");

            Map<String, Object> result = new HashMap<>();
            int totalCleaned = 0;

            // 1. Clean invalid submissions
            List<Object[]> invalidSubmissions = submissionRepository.findSubmissionsFromNonEnrolledStudents();
            int cleanedSubmissions = 0;
            for (Object[] row : invalidSubmissions) {
                try {
                    Long submissionId = ((Number) row[0]).longValue();
                    submissionRepository.deleteById(submissionId);
                    cleanedSubmissions++;
                } catch (Exception e) {
                    log.error("Failed to delete submission: {}", e.getMessage());
                }
            }
            result.put("cleanedSubmissions", cleanedSubmissions);
            totalCleaned += cleanedSubmissions;

            // 2. Clean invalid attendance records
            List<Object[]> invalidAttendance = attendanceRepository.findAttendanceFromNonEnrolledStudents();
            int cleanedAttendance = 0;
            for (Object[] row : invalidAttendance) {
                try {
                    Long attendanceId = ((Number) row[0]).longValue();
                    attendanceRepository.deleteById(attendanceId);
                    cleanedAttendance++;
                } catch (Exception e) {
                    log.error("Failed to delete attendance: {}", e.getMessage());
                }
            }
            result.put("cleanedAttendance", cleanedAttendance);
            totalCleaned += cleanedAttendance;

            // 3. Clean invalid messages
            List<Object[]> invalidMessages = studentMessageRepository.findMessagesWithoutClassroomContext();
            int cleanedMessages = 0;
            for (Object[] row : invalidMessages) {
                try {
                    Long messageId = ((Number) row[0]).longValue();
                    studentMessageRepository.deleteById(messageId);
                    cleanedMessages++;
                } catch (Exception e) {
                    log.error("Failed to delete message: {}", e.getMessage());
                }
            }
            result.put("cleanedMessages", cleanedMessages);
            totalCleaned += cleanedMessages;

            result.put("summary", Map.of(
                "totalCleaned", totalCleaned,
                "status", "CLEANED",
                "message", "🧹 Successfully cleaned " + totalCleaned + " data consistency issues"
            ));

            log.info("Comprehensive cleanup completed. Cleaned {} issues", totalCleaned);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error during comprehensive data cleanup: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to get current data counts
    @GetMapping("/debug/data-counts")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> getCurrentDataCounts() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Count submissions
            long totalSubmissions = submissionRepository.count();
            List<Object[]> submissionsByClassroom = submissionRepository.findSubmissionCountsByClassroom();

            // Count messages
            long totalMessages = studentMessageRepository.count();

            // Count attendance records
            long totalAttendance = attendanceRepository.count();

            // Count enrollments
            long totalEnrollments = classroomEnrollmentRepository.count();

            // Count assignments
            long totalAssignments = assignmentRepository.count();

            result.put("totalSubmissions", totalSubmissions);
            result.put("totalMessages", totalMessages);
            result.put("totalAttendance", totalAttendance);
            result.put("totalEnrollments", totalEnrollments);
            result.put("totalAssignments", totalAssignments);
            result.put("submissionsByClassroom", submissionsByClassroom);

            result.put("status", "SUCCESS");
            result.put("message", "📊 Current data counts retrieved successfully");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error getting current data counts: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Debug endpoint to create valid replacement submissions
    @PostMapping("/debug/create-valid-submissions")
    @PreAuthorize("permitAll()") // Debug endpoint
    public ResponseEntity<Map<String, Object>> createValidSubmissions() {
        try {
            log.info("Creating valid replacement submissions...");

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> createdSubmissions = new ArrayList<>();
            int totalCreated = 0;

            // Get all assignments
            List<Assignment> assignments = assignmentRepository.findAll();

            for (Assignment assignment : assignments) {
                // Get enrolled students for this assignment's classroom
                Set<Long> enrolledStudentIds = classroomEnrollmentRepository.findStudentIdsByClassroomId(assignment.getClassroom().getId());

                for (Long studentId : enrolledStudentIds) {
                    // Check if submission already exists
                    Optional<Submission> existingSubmission = submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), studentId);

                    if (existingSubmission.isEmpty()) {
                        // Create new valid submission
                        User student = userRepository.findById(studentId).orElse(null);
                        if (student != null && student.getRoleId() == 1) { // Only students
                            Submission newSubmission = new Submission();
                            newSubmission.setAssignment(assignment);
                            newSubmission.setStudent(student);
                            newSubmission.setComment("Bài tập được nộp đúng hạn - " + assignment.getTitle());
                            newSubmission.setSubmittedAt(LocalDateTime.now().minusDays(1)); // Submitted yesterday

                            Submission saved = submissionRepository.save(newSubmission);

                            Map<String, Object> submissionInfo = new HashMap<>();
                            submissionInfo.put("submissionId", saved.getId());
                            submissionInfo.put("assignmentId", assignment.getId());
                            submissionInfo.put("assignmentTitle", assignment.getTitle());
                            submissionInfo.put("studentId", studentId);
                            submissionInfo.put("studentName", student.getFullName());
                            submissionInfo.put("classroomId", assignment.getClassroom().getId());
                            submissionInfo.put("classroomName", assignment.getClassroom().getName());

                            createdSubmissions.add(submissionInfo);
                            totalCreated++;
                        }
                    }
                }
            }

            result.put("totalCreated", totalCreated);
            result.put("createdSubmissions", createdSubmissions);
            result.put("status", "SUCCESS");
            result.put("message", "✅ Successfully created " + totalCreated + " valid submissions");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error creating valid submissions: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}