package com.classroomapp.classroombackend.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.exammangement.CreateExamSubmissionDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamSubmissionDto;
import com.classroomapp.classroombackend.dto.exammangement.GradeExamDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.model.exammangement.Exam;
import com.classroomapp.classroombackend.model.exammangement.ExamSubmission;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.exammangement.ExamRepository;
import com.classroomapp.classroombackend.repository.exammangement.ExamSubmissionRepository;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;
import com.classroomapp.classroombackend.service.ExamSubmissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamSubmissionServiceImpl implements ExamSubmissionService {

    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamRepository examRepository;
    private final ClassroomSecurityService classroomSecurityService;
    private final ModelMapper modelMapper;

    @Override
    public ExamSubmissionDto startExam(Long examId) {
        User currentUser = getCurrentUser();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessLogicException("Exam not found"));

        if (!classroomSecurityService.isMember(exam.getClassroom().getId(), currentUser)) {
            throw new BusinessLogicException("You are not enrolled in this classroom.");
        }

        if (Instant.now().isBefore(exam.getStartTime()) || Instant.now().isAfter(exam.getEndTime())) {
            throw new BusinessLogicException("Exam is not active");
        }

        examSubmissionRepository.findByExamIdAndStudentId(examId, currentUser.getId()).ifPresent(s -> {
            throw new BusinessLogicException("You have already started this exam");
        });

        ExamSubmission submission = new ExamSubmission(exam, currentUser, Instant.now());
        ExamSubmission savedSubmission = examSubmissionRepository.save(submission);
        return convertToDto(savedSubmission);
    }

    @Override
    public ExamSubmissionDto submitExam(Long submissionId, CreateExamSubmissionDto submissionDto) {
        User currentUser = getCurrentUser();
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessLogicException("Submission not found"));

        if (!submission.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessLogicException("You are not authorized to submit this exam");
        }

        if (submission.getSubmittedAt() != null) {
            throw new BusinessLogicException("Exam has already been submitted");
        }

        Exam exam = submission.getExam();
        Instant deadline = submission.getStartedAt().plus(exam.getDurationInMinutes(), ChronoUnit.MINUTES);
        if (Instant.now().isAfter(deadline)) {
            throw new BusinessLogicException("Submission time has expired");
        }

        submission.setContent(submissionDto.getContent());
        submission.setSubmittedAt(Instant.now());
        ExamSubmission updatedSubmission = examSubmissionRepository.save(submission);
        return convertToDto(updatedSubmission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamSubmissionDto> getSubmissionsForExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessLogicException("Exam not found"));
        if (!classroomSecurityService.isTeacher(exam.getClassroom().getId())) {
            throw new BusinessLogicException("You are not authorized to view submissions for this exam.");
        }

        List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(examId);
        return submissions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamSubmissionDto getStudentSubmissionForExam(Long examId) {
        User currentUser = getCurrentUser();
        ExamSubmission submission = examSubmissionRepository.findByExamIdAndStudentId(examId, currentUser.getId())
                .orElseThrow(() -> new BusinessLogicException("You have not started this exam"));
        return convertToDto(submission);
    }

    @Override
    public ExamSubmissionDto gradeSubmission(Long submissionId, GradeExamDto gradeDto) {
        User currentUser = getCurrentUser();
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessLogicException("Submission not found"));

        if(!classroomSecurityService.isTeacher(submission.getExam().getClassroom().getId())){
            throw new BusinessLogicException("You are not authorized to grade this submission.");
        }
        
        if (submission.getSubmittedAt() == null) {
            throw new BusinessLogicException("Cannot grade an exam that has not been submitted");
        }

        submission.setScore(gradeDto.getScore());
        submission.setFeedback(gradeDto.getFeedback());
        submission.setGradedAt(Instant.now());
        submission.setGradedBy(currentUser);

        ExamSubmission gradedSubmission = examSubmissionRepository.save(submission);
        return convertToDto(gradedSubmission);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ExamSubmissionDto convertToDto(ExamSubmission submission) {
        return modelMapper.map(submission, ExamSubmissionDto.class);
    }
} 