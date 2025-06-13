package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.SubmissionService;
import com.classroomapp.classroombackend.util.ModelMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public SubmissionDto GetSubmissionById(Long id) {
        Submission submission = FindSubmissionById(id);
        return modelMapper.MapToSubmissionDto(submission);
    }

    @Override
    @Transactional
    public SubmissionDto CreateSubmission(CreateSubmissionDto createSubmissionDto, Long studentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(createSubmissionDto.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", createSubmissionDto.getAssignmentId()));
        
        // Get student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
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
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setComment(createSubmissionDto.getComment());
        submission.setFileSubmissionUrl(createSubmissionDto.getFileSubmissionUrl());
        submission.setSubmittedAt(LocalDateTime.now());
        
        // Save and return
        Submission savedSubmission = submissionRepository.save(submission);
        return modelMapper.MapToSubmissionDto(savedSubmission);
    }

    @Override
    @Transactional
    public SubmissionDto UpdateSubmission(Long id, CreateSubmissionDto updateSubmissionDto) {
        Submission submission = FindSubmissionById(id);
        
        // Only allow updating if not graded
        if (submission.getScore() != null) {
            throw new IllegalArgumentException("Cannot update a graded submission");
        }
        
        // Update fields
        submission.setComment(updateSubmissionDto.getComment());
        submission.setFileSubmissionUrl(updateSubmissionDto.getFileSubmissionUrl());
        submission.setSubmittedAt(LocalDateTime.now());
        
        // Save and return
        Submission updatedSubmission = submissionRepository.save(submission);
        return modelMapper.MapToSubmissionDto(updatedSubmission);
    }

    @Override
    @Transactional
    public void DeleteSubmission(Long id) {
        Submission submission = FindSubmissionById(id);
        
        // Only allow deleting if not graded
        if (submission.getScore() != null) {
            throw new IllegalArgumentException("Cannot delete a graded submission");
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
                .map(modelMapper::MapToSubmissionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionDto> GetSubmissionsByStudent(Long studentId) {
        // Get student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Get submissions
        List<Submission> submissions = submissionRepository.findByStudent(student);
        return submissions.stream()
                .map(modelMapper::MapToSubmissionDto)
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
        
        return modelMapper.MapToSubmissionDto(submission);
    }

    @Override
    @Transactional
    public SubmissionDto GradeSubmission(Long submissionId, GradeSubmissionDto gradeSubmissionDto, Long teacherId) {
        // Get submission
        Submission submission = FindSubmissionById(submissionId);
        
        // Get teacher
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
        
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
        if (gradeSubmissionDto.getScore() > assignment.getPoints()) {
            throw new IllegalArgumentException("Score cannot exceed the maximum points for the assignment");
        }
        
        // Update submission
        submission.setScore(gradeSubmissionDto.getScore());
        submission.setFeedback(gradeSubmissionDto.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacher);
        
        // Save and return
        Submission gradedSubmission = submissionRepository.save(submission);
        return modelMapper.MapToSubmissionDto(gradedSubmission);
    }

    @Override
    public List<SubmissionDto> GetGradedSubmissionsByAssignment(Long assignmentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
        
        // Get graded submissions
        List<Submission> submissions = submissionRepository.findByAssignmentAndScoreIsNotNull(assignment);
        return submissions.stream()
                .map(modelMapper::MapToSubmissionDto)
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
                .map(modelMapper::MapToSubmissionDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionStatistics GetSubmissionStatisticsForAssignment(Long assignmentId) {
        // Get assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
        
        // Create statistics object
        SubmissionStatistics stats = new SubmissionStatistics();
        
        // Set total students count
        int totalStudents = assignment.getClassroom().getStudents().size();
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
} 