package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;

/**
 * Service for validating grade-related business rules
 */
public interface GradeValidationService {
    
    /**
     * Validate that a score does not exceed the assignment's maximum points
     * @param score The score to validate
     * @param assignment The assignment with maximum points
     * @throws IllegalArgumentException if score exceeds maximum points
     */
    void validateScoreWithinLimits(Integer score, Assignment assignment);
    
    /**
     * Validate that a submission's score does not exceed the assignment's maximum points
     * @param submission The submission to validate
     * @throws IllegalArgumentException if score exceeds maximum points
     */
    void validateSubmissionScore(Submission submission);
    
    /**
     * Fix existing submissions that have scores exceeding assignment points
     * @return Number of submissions fixed
     */
    int fixInvalidScores();
    
    /**
     * Audit all submissions for score validation issues
     * @return Number of invalid submissions found
     */
    int auditInvalidScores();
}
