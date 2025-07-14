package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.service.GradeValidationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GradeValidationServiceImpl implements GradeValidationService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Override
    public void validateScoreWithinLimits(Integer score, Assignment assignment) {
        if (score == null || assignment == null || assignment.getPoints() == null) {
            return; // Skip validation if any required field is null
        }
        
        if (score > assignment.getPoints()) {
            throw new IllegalArgumentException(
                String.format("Score %d exceeds maximum points %d for assignment '%s'", 
                    score, assignment.getPoints(), assignment.getTitle())
            );
        }
        
        log.debug("✅ Score validation passed: {} <= {} for assignment {}", 
            score, assignment.getPoints(), assignment.getId());
    }

    @Override
    public void validateSubmissionScore(Submission submission) {
        if (submission == null || submission.getScore() == null) {
            return; // Skip validation if submission or score is null
        }
        
        validateScoreWithinLimits(submission.getScore(), submission.getAssignment());
    }

    @Override
    @Transactional
    public int fixInvalidScores() {
        log.info("🔧 Starting to fix invalid scores where score > assignment.points");
        
        // Find all submissions with scores
        List<Submission> submissionsWithScores = submissionRepository.findAll().stream()
                .filter(s -> s.getScore() != null && s.getAssignment() != null && s.getAssignment().getPoints() != null)
                .collect(Collectors.toList());
        
        log.info("🔍 Found {} submissions with scores to check", submissionsWithScores.size());
        
        int fixedCount = 0;
        for (Submission submission : submissionsWithScores) {
            Integer score = submission.getScore();
            Integer maxPoints = submission.getAssignment().getPoints();
            
            if (score > maxPoints) {
                log.warn("🚨 Invalid score found: Submission {} has score {} > max points {} for assignment '{}'", 
                    submission.getId(), score, maxPoints, submission.getAssignment().getTitle());
                
                // Fix by capping the score to maximum points
                submission.setScore(maxPoints);
                submissionRepository.save(submission);
                fixedCount++;
                
                log.info("🔧 Fixed submission {}: score {} → {} (capped to max points)", 
                    submission.getId(), score, maxPoints);
            }
        }
        
        log.info("✅ Fixed {} invalid scores", fixedCount);
        return fixedCount;
    }

    @Override
    public int auditInvalidScores() {
        log.info("🔍 Auditing submissions for invalid scores");
        
        List<Submission> submissionsWithScores = submissionRepository.findAll().stream()
                .filter(s -> s.getScore() != null && s.getAssignment() != null && s.getAssignment().getPoints() != null)
                .collect(Collectors.toList());
        
        int invalidCount = 0;
        for (Submission submission : submissionsWithScores) {
            Integer score = submission.getScore();
            Integer maxPoints = submission.getAssignment().getPoints();
            
            if (score > maxPoints) {
                log.warn("❌ Invalid score: Submission {} has score {} > max points {} for assignment '{}'", 
                    submission.getId(), score, maxPoints, submission.getAssignment().getTitle());
                invalidCount++;
            }
        }
        
        log.info("📊 Audit complete: {} invalid scores found out of {} submissions", 
            invalidCount, submissionsWithScores.size());
        return invalidCount;
    }
}
