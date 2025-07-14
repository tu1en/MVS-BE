package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.assignmentmanagement.SubmissionAttachment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.SubmissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    @Override
    public SubmissionDto GetSubmissionById(Long id) {
        Submission submission = FindSubmissionById(id);
        return modelMapper.map(submission, SubmissionDto.class);
    }

    @Override
    @Transactional
    public SubmissionDto submit(CreateSubmissionDto dto, String studentUsername) {
        User student = userRepository.findByEmail(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", studentUsername));

        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", dto.getAssignmentId()));

        // 1. Validate student role
        if (student.getRoleId() != 1) {
            throw new IllegalArgumentException("Only users with student role can submit assignments");
        }

        // 2. Check if student is enrolled in the classroom (optimized)
        Classroom classroom = assignment.getClassroom();
        boolean isEnrolled = classroomEnrollmentRepository.isStudentEnrolledInClassroom(classroom.getId(), student.getId());
        if (!isEnrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this classroom");
        }

        // 3. Kiểm tra hạn nộp
        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new BusinessLogicException("Assignment deadline has passed.");
        }

        // 2. Logic Upsert: Tìm hoặc tạo mới Submission
        Submission submission = submissionRepository
                .findByAssignmentAndStudent(assignment, student)
                .orElse(new Submission(assignment, student));

        // 3. Nếu là nộp lại, xóa file cũ
        if (submission.getId() != null && submission.getAttachments() != null) {
            submission.getAttachments().forEach(att -> fileStorageService.delete(att.getFileName()));
            submission.getAttachments().clear();
        }

        // 4. Cập nhật nội dung và file mới
        submission.setComment(dto.getComment());
        submission.setSubmittedAt(LocalDateTime.now());

        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            dto.getAttachments().forEach(fileInfo ->
                submission.addAttachment(new SubmissionAttachment(fileInfo, submission))
            );
        }

        Submission savedSubmission = submissionRepository.save(submission);
        return modelMapper.map(savedSubmission, SubmissionDto.class);
    }

    @Override
    @Transactional
    public SubmissionDto CreateSubmission(CreateSubmissionDto createSubmissionDto, String studentUsername) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(createSubmissionDto.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", createSubmissionDto.getAssignmentId()));
        
        // Get student
        User student = userRepository.findByEmail(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", studentUsername));
        
        // Validate student role
        if (student.getRoleId() != 1) {
            throw new IllegalArgumentException("Only users with student role can submit assignments");
        }
        
        // Check if student is enrolled in the classroom
        Classroom classroom = assignment.getClassroom();
        if (classroom.getStudents() == null || !classroom.getStudents().contains(student)) {
            throw new IllegalArgumentException("Student is not enrolled in this classroom");
        }
          // Check if submission already exists
        submissionRepository.findByAssignmentAndStudent(assignment, student)
                .ifPresent(existingSubmission -> {
                    throw new IllegalArgumentException("Student has already submitted this assignment");
                });
        
        // Create submission
        Submission submission = new Submission(assignment, student);
        submission.setComment(createSubmissionDto.getComment());
        submission.setSubmittedAt(LocalDateTime.now());

        if (createSubmissionDto.getAttachments() != null && !createSubmissionDto.getAttachments().isEmpty()) {
            createSubmissionDto.getAttachments().forEach(fileInfo ->
                submission.addAttachment(new SubmissionAttachment(fileInfo, submission))
            );
        }
        
        // Save and return
        Submission savedSubmission = submissionRepository.save(submission);
        return modelMapper.map(savedSubmission, SubmissionDto.class);
    }

    @Override
    @Transactional
    public SubmissionDto UpdateSubmission(Long id, CreateSubmissionDto updateSubmissionDto) {
        Submission submission = FindSubmissionById(id);
        
        // Only allow updating if not graded
        if (submission.getScore() != null) {
            throw new IllegalArgumentException("Cannot update a graded submission");
        }
        
        // Delete old attachments from storage
        if (submission.getAttachments() != null && !submission.getAttachments().isEmpty()) {
            submission.getAttachments().forEach(attachment -> fileStorageService.delete(attachment.getFileName()));
        }

        // Clear the old collection of attachments managed by JPA
        submission.getAttachments().clear();
        
        // Update fields
        submission.setComment(updateSubmissionDto.getComment());
        submission.setSubmittedAt(LocalDateTime.now());

        // Add new attachments
        if (updateSubmissionDto.getAttachments() != null && !updateSubmissionDto.getAttachments().isEmpty()) {
            updateSubmissionDto.getAttachments().forEach(fileInfo ->
                submission.addAttachment(new SubmissionAttachment(fileInfo, submission))
            );
        }
        
        // Save and return
        Submission updatedSubmission = submissionRepository.save(submission);
        return modelMapper.map(updatedSubmission, SubmissionDto.class);
    }

    @Override
    @Transactional
    public void DeleteSubmission(Long id) {
        Submission submission = FindSubmissionById(id);
        
        // Only allow deleting if not graded
        if (submission.getScore() != null) {
            throw new IllegalArgumentException("Cannot delete a graded submission");
        }

        // Also delete files from storage before deleting the submission from DB
        if (submission.getAttachments() != null && !submission.getAttachments().isEmpty()) {
            submission.getAttachments().forEach(attachment -> fileStorageService.delete(attachment.getFileName()));
        }
        
        submissionRepository.delete(submission);
    }

    @Override
    public List<SubmissionDto> GetSubmissionsByAssignment(Long assignmentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
        
        // Get submissions
        List<Submission> submissions = submissionRepository.findByAssignmentOrderBySubmittedAtDesc(assignment);
        return submissions.stream()
                .map(submission -> modelMapper.map(submission, SubmissionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionDto> GetSubmissionsByStudent(Long studentId) {
        // Get student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Get submissions using the optimized query
        List<Submission> submissions = submissionRepository.findByStudentWithDetails(student);
        return submissions.stream()
                .map(submission -> modelMapper.map(submission, SubmissionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionDto GetStudentSubmissionForAssignment(Long assignmentId, Long studentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
        
        // Get student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Get submission
        Submission submission = submissionRepository.findByAssignmentAndStudent(assignment, student)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "assignment and student", 
                        assignmentId + " and " + studentId));
        
        return modelMapper.map(submission, SubmissionDto.class);
    }

    @Override
    @Transactional
    public SubmissionDto GradeSubmission(Long submissionId, GradeSubmissionDto gradeSubmissionDto, String teacherUsername) {
        // Get submission
        Submission submission = FindSubmissionById(submissionId);
        
        // Get teacher
        User teacher = userRepository.findByEmail(teacherUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", teacherUsername));
        
        // Validate teacher role
        if (teacher.getRoleId() != 2 && teacher.getRoleId() != 3) {
            throw new IllegalArgumentException("Only teachers can grade submissions");
        }
        
        // Validate teacher is the owner of the classroom
        Assignment assignment = submission.getAssignment();
        Classroom classroom = assignment.getClassroom();
        
        if (!classroom.getTeacher().equals(teacher)) {
            throw new IllegalArgumentException("Only the classroom teacher can grade submissions");
        }
        
        // Validate score is within the assignment's points
        if (gradeSubmissionDto.getScore() != null && assignment.getPoints() != null) {
            if (gradeSubmissionDto.getScore() > assignment.getPoints()) {
                throw new IllegalArgumentException(
                    String.format("Score %d exceeds maximum points %d for assignment '%s'",
                        gradeSubmissionDto.getScore(), assignment.getPoints(), assignment.getTitle())
                );
            }
        }

        // Update submission
        submission.setScore(gradeSubmissionDto.getScore());
        submission.setFeedback(gradeSubmissionDto.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacher);
        
        // Save and return
        Submission gradedSubmission = submissionRepository.save(submission);
        return modelMapper.map(gradedSubmission, SubmissionDto.class);
    }

    @Override
    public List<SubmissionDto> GetGradedSubmissionsByAssignment(Long assignmentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
        
        // Get graded submissions
        List<Submission> submissions = submissionRepository.findByAssignmentAndScoreIsNotNull(assignment);
        return submissions.stream()
                .map(submission -> modelMapper.map(submission, SubmissionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionDto> GetUngradedSubmissionsByAssignment(Long assignmentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
        
        // Get ungraded submissions
        List<Submission> submissions = submissionRepository.findByAssignmentAndScoreIsNull(assignment);
        return submissions.stream()
                .map(submission -> modelMapper.map(submission, SubmissionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionStatistics GetSubmissionStatisticsForAssignment(Long assignmentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        // Create statistics object
        SubmissionStatistics stats = new SubmissionStatistics();

        // Set total students count using enrollment repository for consistency
        // This ensures we count only actually enrolled students, not lazy-loaded ones
        Set<Long> enrolledStudentIds = classroomEnrollmentRepository.findStudentIdsByClassroomId(assignment.getClassroom().getId());
        int totalStudents = enrolledStudentIds.size();
        stats.setTotalStudents(totalStudents);
        
        // Set submission count
        long submissionCount = submissionRepository.countByAssignment(assignment);
        stats.setSubmissionCount(submissionCount);
        
        // Get all graded submissions
        List<Submission> gradedSubmissions = submissionRepository.findByAssignmentAndScoreIsNotNull(assignment);
        stats.setGradedCount(gradedSubmissions.size());
        
        // Calculate average score if there are graded submissions
        if (!gradedSubmissions.isEmpty()) {
            OptionalDouble averageScore = gradedSubmissions.stream()
                    .mapToInt(Submission::getScore)
                    .average();
            
            stats.setAverageScore(averageScore.orElse(0.0));
        } else {
            stats.setAverageScore(0.0);
        }
        
        return stats;
    }
    
    // Helper method to find submission by ID or throw exception
    private Submission FindSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
    }

    /**
     * Clean up invalid submissions from students who are not enrolled in the classroom
     * This method ensures data consistency between enrollment and submission data
     */
    @Transactional
    public int cleanInvalidSubmissionsForAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        // Get enrolled student IDs for this classroom
        Set<Long> enrolledStudentIds = classroomEnrollmentRepository.findStudentIdsByClassroomId(assignment.getClassroom().getId());

        // Get all submissions for this assignment
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        // Find invalid submissions (from non-enrolled students)
        List<Submission> invalidSubmissions = submissions.stream()
            .filter(s -> s.getStudent() != null && !enrolledStudentIds.contains(s.getStudent().getId()))
            .collect(java.util.stream.Collectors.toList());

        // Delete invalid submissions
        for (Submission invalidSubmission : invalidSubmissions) {
            submissionRepository.delete(invalidSubmission);
        }

        return invalidSubmissions.size();
    }
}