package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final ModelMapper modelMapper;
    private final ClassroomSecurityService classroomSecurityService;
    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Override
    public Assignment findEntityById(Long id) {
        return assignmentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Assignment with ID " + id + " not found"));
    }

    @Override
    public AssignmentDto GetAssignmentById(Long id) {
        Assignment assignment = findEntityById(id);
        return modelMapper.map(assignment, AssignmentDto.class);
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
    }

    @Override
    public List<AssignmentDto> GetUpcomingAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new EntityNotFoundException("Classroom with ID " + classroomId + " not found"));
        return assignmentRepository.findByClassroomAndDueDateAfter(classroom, LocalDateTime.now()).stream()
                .map(assignment -> modelMapper.map(assignment, AssignmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
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
                    dto.setGrade(submission.getScore() != null ? submission.getScore().doubleValue() : null);
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
    public GradeDto gradeSubmission(Long assignmentId, GradeSubmissionDto gradeSubmissionDto) {
        log.info("AssignmentServiceImpl.gradeSubmission called with assignmentId: {}", assignmentId);
        log.info("Grading data: score={}, hasFeedback={}",
                gradeSubmissionDto.getScore(), gradeSubmissionDto.getFeedback() != null);

        try {
            // Validate assignment exists
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
            log.info("Found assignment: id={}, title={}", assignment.getId(), assignment.getTitle());

            // Note: The current implementation expects submissionId to be passed separately
            // For now, we'll need to find the submission through other means or modify the API
            // This is a placeholder implementation that needs to be completed based on the actual API design
            log.warn("gradeSubmission method needs submissionId - current implementation is incomplete");

            // Create a basic response for now
            GradeDto gradeDto = new GradeDto();
            gradeDto.setGrade(gradeSubmissionDto.getScore().doubleValue());
            gradeDto.setFeedback(gradeSubmissionDto.getFeedback());
            gradeDto.setGradedDate(LocalDateTime.now());

            // Get current user for grading audit
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            User grader = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));
            gradeDto.setGradedBy(grader.getFullName());

            log.info("Grading operation logged - actual implementation needs submissionId parameter");
            return gradeDto;

        } catch (Exception e) {
            log.error("Error grading submission: assignmentId={}, error={}",
                    assignmentId, e.getMessage(), e);
            throw e;
        }
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
}