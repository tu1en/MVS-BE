package com.classroomapp.classroombackend.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.FileStorageService;

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
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

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
    // @PreAuthorize("permitAll()") // Temporarily disable security for testing
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
}