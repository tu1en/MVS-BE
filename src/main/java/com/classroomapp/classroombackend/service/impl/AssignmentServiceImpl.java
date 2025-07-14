package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentAttachmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.AssignmentAttachment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Lazy
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final ModelMapper modelMapper;
    @Lazy
    private final ClassroomSecurityService classroomSecurityService;
    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);



    /**
     * Find Assignment entity by ID
     */
    @Override
    public Assignment findEntityById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    /**
     * Get assignment by ID
     */
    @Override
    public AssignmentDto GetAssignmentById(Long id) {
        Assignment assignment = findEntityById(id);
        return convertToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentDto CreateAssignment(CreateAssignmentDto createAssignmentDto, String teacherUsername) {
        log.info("AssignmentServiceImpl.CreateAssignment called with classroomId: {}, teacherUsername: {}",
                createAssignmentDto.getClassroomId(), teacherUsername);
        log.info("Assignment creation data: title={}, dueDate={}, points={}",
                createAssignmentDto.getTitle(), createAssignmentDto.getDueDate(), createAssignmentDto.getPoints());

        try {
            Classroom classroom = classroomRepository.findById(createAssignmentDto.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + createAssignmentDto.getClassroomId()));
            log.info("Found classroom: id={}, name={}", classroom.getId(), classroom.getName());

            User teacher = userRepository.findByEmail(teacherUsername)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", teacherUsername));
            log.info("Found teacher: id={}, username={}, email={}", teacher.getId(), teacher.getUsername(), teacher.getEmail());

            if (!classroomSecurityService.isTeacherOfClassroom(teacher, classroom.getId())) {
                log.error("Access denied: User {} is not the teacher of classroom {}", teacher.getUsername(), classroom.getId());
                throw new AccessDeniedException("User is not the teacher of this classroom.");
            }
            log.info("Security check passed: User {} is authorized to create assignments in classroom {}",
                    teacher.getUsername(), classroom.getId());

            Assignment assignment = modelMapper.map(createAssignmentDto, Assignment.class);
            assignment.setClassroom(classroom);
            log.info("Created assignment entity: title={}, dueDate={}, points={}",
                    assignment.getTitle(), assignment.getDueDate(), assignment.getPoints());

            if (createAssignmentDto.getAttachments() != null && !createAssignmentDto.getAttachments().isEmpty()) {
                log.info("Processing {} attachments for assignment", createAssignmentDto.getAttachments().size());
                for (FileUploadResponse fileInfo : createAssignmentDto.getAttachments()) {
                    AssignmentAttachment attachment = new AssignmentAttachment();
                    attachment.setFileName(fileInfo.getFileName());
                    attachment.setFileUrl(fileInfo.getFileUrl());
                    attachment.setFileType(fileInfo.getFileType());
                    attachment.setFileSize(fileInfo.getSize());
                    assignment.addAttachment(attachment);
                    log.info("Added attachment: fileName={}, fileType={}, fileSize={}",
                            fileInfo.getFileName(), fileInfo.getFileType(), fileInfo.getSize());
                }
            } else {
                log.info("No attachments provided for assignment");
            }

            Assignment savedAssignment = assignmentRepository.save(assignment);
            log.info("Successfully saved assignment with ID: {}", savedAssignment.getId());

            AssignmentDto result = modelMapper.map(savedAssignment, AssignmentDto.class);
            log.info("Assignment creation completed successfully: assignmentId={}, title={}",
                    result.getId(), result.getTitle());
            return result;

        } catch (Exception e) {
            log.error("Error creating assignment: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public AssignmentDto UpdateAssignment(Long id, CreateAssignmentDto updateAssignmentDto) {
        Assignment assignment = findEntityById(id);
        assignment.setTitle(updateAssignmentDto.getTitle());
        assignment.setDescription(updateAssignmentDto.getDescription());
        assignment.setDueDate(updateAssignmentDto.getDueDate());
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return modelMapper.map(updatedAssignment, AssignmentDto.class);
    }

    @Override
    public void DeleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
    
    @Override
    public List<AssignmentDto> GetAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new EntityNotFoundException("Classroom with ID " + classroomId + " not found"));
        return assignmentRepository.findByClassroomOrderByDueDateAsc(classroom).stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> GetAllAssignments() {
        return assignmentRepository.findAll().stream().map(assignment -> modelMapper.map(assignment, AssignmentDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> getAssignmentsByCurrentTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName(); // This is the email
        
        System.out.println("DEBUG: Current principal name (email): " + currentPrincipalName);
        
        // FIX: Look up user by email, which is the principal name from JWT
        User currentUser = userRepository.findByEmail(currentPrincipalName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentPrincipalName));

        System.out.println("DEBUG: Found user: " + currentUser.getUsername() + " with email: " + currentUser.getEmail());

        // Use the correct username from the fetched user entity to find assignments
        List<Assignment> assignments = assignmentRepository.findByTeacherUsername(currentUser.getUsername());

        System.out.println("DEBUG: Found " + assignments.size() + " assignments for teacher: " + currentUser.getUsername());

        return assignments.stream()
                .map(a -> modelMapper.map(a, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> GetAssignmentsByStudent(Long studentId) {
        // Find all classroom enrollments for the student
        List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(studentId);

        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract the classrooms from the enrollments
        List<Classroom> enrolledClassrooms = enrollments.stream()
                .map(ClassroomEnrollment::getClassroom)
                .collect(Collectors.toList());

        if (enrolledClassrooms.isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch all assignments for those classrooms in a single query
        return assignmentRepository.findByClassroomInOrderByDueDateAsc(enrolledClassrooms)
                .stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> getAssignmentsByTeacher(Long teacherId) {
        // Kiểm tra xem giáo viên có tồn tại không
        if (!userRepository.existsById(teacherId)) {
            throw new EntityNotFoundException("Teacher not found with ID: " + teacherId);
        }
        // Gọi phương thức repository mới với truy vấn đã được định nghĩa rõ ràng
        return assignmentRepository.findByTeacherId(teacherId).stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }    @Override
    public List<AssignmentDto> GetUpcomingAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new EntityNotFoundException("Classroom with ID " + classroomId + " not found"));
        return assignmentRepository.findByClassroomAndDueDateAfter(classroom, LocalDateTime.now()).stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }    @Override
    public List<AssignmentDto> GetPastAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new EntityNotFoundException("Classroom with ID " + classroomId + " not found"));
        return assignmentRepository.findByClassroomAndDueDateBefore(classroom, LocalDateTime.now()).stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> SearchAssignmentsByTitle(String title) {
        return assignmentRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionDto> getAssignmentSubmissions(Long assignmentId) {
        log.info("AssignmentServiceImpl.getAssignmentSubmissions called with assignmentId: {}", assignmentId);
        
        try {
            // Find all submissions for this assignment
            List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
            log.info("Found {} submissions in database for assignment {}", submissions.size(), assignmentId);
            
            // Convert to DTOs
            List<AssignmentSubmissionDto> submissionDtos = new ArrayList<>();
            for (Submission submission : submissions) {
                try {
                    log.info("Processing submission ID: {}", submission.getId());
                    
                    AssignmentSubmissionDto dto = new AssignmentSubmissionDto();
                    dto.setId(submission.getId());
                    
                    // Safely access student
                    if (submission.getStudent() != null) {
                        dto.setStudentId(submission.getStudent().getId());
                        dto.setStudentName(submission.getStudent().getFullName());
                    } else {
                        log.warn("Submission {} has null student", submission.getId());
                        dto.setStudentId(0L);
                        dto.setStudentName("Unknown Student");
                    }
                    
                    dto.setSubmissionText(submission.getComment());
                    dto.setSubmissionDate(submission.getSubmittedAt());
                    
                    // Set grade field from score
                    if (submission.getScore() != null) {
                        dto.setGrade(submission.getScore().doubleValue());
                    }
                    
                    dto.setFeedback(submission.getFeedback());
                    dto.setStatus(submission.getScore() != null ? "GRADED" : "SUBMITTED");
                    
                    // Get first attachment URL if exists
                    try {
                        if (submission.getAttachments() != null && !submission.getAttachments().isEmpty()) {
                            dto.setAttachmentUrl(submission.getAttachments().get(0).getFileUrl());
                            log.info("Added attachment URL: {}", submission.getAttachments().get(0).getFileUrl());
                        }
                    } catch (Exception e) {
                        log.error("Error loading attachments for submission {}: {}", submission.getId(), e.getMessage());
                    }
                    
                    submissionDtos.add(dto);
                    log.info("Successfully processed submission {}", submission.getId());
                    
                } catch (Exception e) {
                    log.error("Error processing submission {}: {}", submission.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Returning {} submission DTOs", submissionDtos.size());
            return submissionDtos;
            
        } catch (Exception e) {
            log.error("Error in getAssignmentSubmissions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public GradeDto gradeSubmission(Long assignmentId, Long submissionId, GradeSubmissionDto gradeSubmissionDto) {
        log.info("gradeSubmission called with assignmentId: {}, submissionId: {}, score: {}, feedback: {}",
                assignmentId, submissionId, gradeSubmissionDto.getScore(), gradeSubmissionDto.getFeedback());

        // Find the submission by its ID
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        // Optional: Verify the submission belongs to the assignment
        if (!submission.getAssignment().getId().equals(assignmentId)) {
            throw new IllegalArgumentException("Submission with id " + submissionId + " does not belong to assignment with id " + assignmentId);
        }

        // Get the current authenticated user (the grader)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new InsufficientAuthenticationException("User must be authenticated to grade a submission.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User grader = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        // Get the assignment from the submission for validation
        Assignment assignment = submission.getAssignment();

        // Validate score does not exceed assignment points
        if (gradeSubmissionDto.getScore() != null && assignment.getPoints() != null) {
            if (gradeSubmissionDto.getScore() > assignment.getPoints()) {
                throw new IllegalArgumentException(
                    String.format("Score %d exceeds maximum points %d for assignment '%s'",
                        gradeSubmissionDto.getScore(), assignment.getPoints(), assignment.getTitle())
                );
            }
        }

        // Update the submission with the grade and feedback
        submission.setScore(gradeSubmissionDto.getScore());
        submission.setFeedback(gradeSubmissionDto.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(grader); // Set the grader

        // Save the updated submission
        Submission savedSubmission = submissionRepository.save(submission);
        log.info("Successfully graded and saved submission with ID: {}", savedSubmission.getId());

        // Return a DTO representing the grade
        return new GradeDto(
                savedSubmission.getId(),
                savedSubmission.getAssignment().getId(),
                savedSubmission.getScore() != null ? savedSubmission.getScore().doubleValue() : null,
                savedSubmission.getFeedback(),
                savedSubmission.getGradedAt(),
                grader.getFullName());
    }

    @Override
    public AssignmentRubricDto getAssignmentRubric(Long assignmentId) {
        return null;
    }

    @Override
    public AssignmentRubricDto createAssignmentRubric(Long assignmentId, CreateRubricDto createRubricDto) {
        return null;
    }

    @Override
    public BulkGradingResultDto bulkGradeSubmissions(Long assignmentId, BulkGradingDto bulkGradingDto) {
        return null;
    }

    @Override
    public GradingAnalyticsDto getGradingAnalytics(Long assignmentId) {
        return null;
    }

    @Override
    public FeedbackDto provideFeedback(Long assignmentId, CreateFeedbackDto createFeedbackDto) {
        return null;
    }

    @Override
    public List<AssignmentDto> getAssignmentsByCurrentStudent() {
        log.info("getAssignmentsByCurrentStudent: Getting assignments for current student");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new InsufficientAuthenticationException("User is not properly authenticated");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long studentId = userDetails.getId();

        log.info("getAssignmentsByCurrentStudent: authenticated user id: {}", studentId);

        List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(studentId);

        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract the classrooms from the enrollments
        List<Classroom> enrolledClassrooms = enrollments.stream()
                .map(ClassroomEnrollment::getClassroom)
                .collect(Collectors.toList());

        if (enrolledClassrooms.isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch all assignments for those classrooms in a single query
        return assignmentRepository.findByClassroomInOrderByDueDateAsc(enrolledClassrooms)
                .stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> findByTeacherId(Long teacherId) {
        List<Classroom> teacherClassrooms = classroomRepository.findByTeacherId(teacherId);
        return assignmentRepository.findByClassroomInOrderByDueDateAsc(teacherClassrooms)
                .stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> findByStudentId(Long studentId) {
        List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(studentId);
        List<Classroom> enrolledClassrooms = enrollments.stream()
                .map(ClassroomEnrollment::getClassroom)
                .collect(Collectors.toList());
        
        return assignmentRepository.findByClassroomInOrderByDueDateAsc(enrolledClassrooms)
                .stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> cleanInvalidSubmissionsForClassroom(Long classroomId) {
        log.info("Service: Starting cleanup of invalid submissions for classroom {}", classroomId);

        Map<String, Object> result = new HashMap<>();
        int totalCleaned = 0;

        try {
            // Get all assignments for this classroom
            List<Assignment> assignments = assignmentRepository.findByClassroomId(classroomId);

            if (assignments.isEmpty()) {
                result.put("message", "Không tìm thấy assignment nào cho classroom " + classroomId);
                result.put("totalCleaned", 0);
                result.put("status", "NO_ASSIGNMENTS");
                return result;
            }

            // Get enrolled student IDs for this classroom
            Set<Long> enrolledStudentIds = classroomEnrollmentRepository.findStudentIdsByClassroomId(classroomId);

            List<Map<String, Object>> cleanupDetails = new ArrayList<>();

            for (Assignment assignment : assignments) {
                // Get all submissions for this assignment
                List<Submission> submissions = submissionRepository.findByAssignmentId(assignment.getId());

                // Find invalid submissions (from non-enrolled students)
                List<Submission> invalidSubmissions = submissions.stream()
                    .filter(s -> s.getStudent() != null && !enrolledStudentIds.contains(s.getStudent().getId()))
                    .collect(Collectors.toList());

                if (!invalidSubmissions.isEmpty()) {
                    Map<String, Object> assignmentCleanup = new HashMap<>();
                    assignmentCleanup.put("assignmentId", assignment.getId());
                    assignmentCleanup.put("assignmentTitle", assignment.getTitle());
                    assignmentCleanup.put("invalidSubmissionsCount", invalidSubmissions.size());

                    // Delete invalid submissions
                    for (Submission invalidSubmission : invalidSubmissions) {
                        submissionRepository.delete(invalidSubmission);
                        totalCleaned++;
                        log.info("Deleted invalid submission ID {} from non-enrolled student ID {} for assignment {}",
                                invalidSubmission.getId(), invalidSubmission.getStudent().getId(), assignment.getId());
                    }

                    cleanupDetails.add(assignmentCleanup);
                }
            }

            result.put("classroomId", classroomId);
            result.put("totalAssignments", assignments.size());
            result.put("totalCleaned", totalCleaned);
            result.put("cleanupDetails", cleanupDetails);
            result.put("status", totalCleaned > 0 ? "CLEANED" : "ALREADY_CLEAN");
            result.put("message", totalCleaned > 0 ?
                "🧹 Đã xóa thành công " + totalCleaned + " submission không hợp lệ từ classroom " + classroomId :
                "✅ Không tìm thấy submission không hợp lệ nào trong classroom " + classroomId);

            log.info("Service: Completed cleanup for classroom {}. Total cleaned: {}", classroomId, totalCleaned);
            return result;

        } catch (Exception e) {
            log.error("Service: Error during cleanup for classroom {}: {}", classroomId, e.getMessage(), e);
            result.put("error", e.getMessage());
            result.put("status", "ERROR");
            result.put("totalCleaned", totalCleaned);
            return result;
        }
    }

    /**
     * Convert Assignment entity to AssignmentDto with attachments
     */
    private AssignmentDto convertToAssignmentDto(Assignment assignment) {
        AssignmentDto dto = modelMapper.map(assignment, AssignmentDto.class);

        // Map attachments
        if (assignment.getAttachments() != null && !assignment.getAttachments().isEmpty()) {
            List<AssignmentAttachmentDto> attachmentDtos = assignment.getAttachments().stream()
                .map(this::convertToAttachmentDto)
                .collect(Collectors.toList());
            dto.setAttachments(attachmentDtos);

            // Set first attachment URL for backward compatibility
            dto.setFileAttachmentUrl(assignment.getAttachments().get(0).getFileUrl());
        }

        return dto;
    }

    /**
     * Convert AssignmentAttachment entity to AssignmentAttachmentDto
     */
    private AssignmentAttachmentDto convertToAttachmentDto(AssignmentAttachment attachment) {
        AssignmentAttachmentDto dto = new AssignmentAttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setDownloadUrl(attachment.getFileUrl()); // Use fileUrl as downloadUrl for now
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setAssignmentId(attachment.getAssignment().getId());
        dto.setCreatedAt(attachment.getCreatedAt());
        return dto;
    }
}