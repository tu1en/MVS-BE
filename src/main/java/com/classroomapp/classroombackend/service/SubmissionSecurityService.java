package com.classroomapp.classroombackend.service;

/**
 * Security service for submission-related authorization
 */
public interface SubmissionSecurityService {
    
    /**
     * Check if the current authenticated user is the student with the given ID
     * @param studentId the student ID to check
     * @return true if current user is the student, false otherwise
     */
    boolean isCurrentStudent(Long studentId);
    
    /**
     * Check if the current user can access a specific assignment's submissions
     * @param assignmentId the assignment ID
     * @return true if user can access, false otherwise
     */
    boolean canAccessAssignmentSubmissions(Long assignmentId);
    
    /**
     * Check if the current user can grade submissions for an assignment
     * @param assignmentId the assignment ID
     * @return true if user can grade, false otherwise
     */
    boolean canGradeAssignmentSubmissions(Long assignmentId);
}