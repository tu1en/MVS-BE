package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;

public interface SubmissionService {
    
    // Get a submission by ID
    SubmissionDto GetSubmissionById(Long id);
    
    /**
     * Creates or updates a submission for the current user.
     * This method implements an "upsert" logic.
     * @param createSubmissionDto DTO containing submission data.
     * @param studentUsername The username of the student submitting.
     * @return The created or updated submission as a DTO.
     */
    SubmissionDto submit(CreateSubmissionDto createSubmissionDto, String studentUsername);

    /**
     * @deprecated Use submit() instead.
     */
    @Deprecated
    SubmissionDto CreateSubmission(CreateSubmissionDto createSubmissionDto, String studentUsername);
    
    /**
     * @deprecated Use submit() instead.
     */
    @Deprecated
    SubmissionDto UpdateSubmission(Long id, CreateSubmissionDto updateSubmissionDto);
    
    // Delete a submission
    void DeleteSubmission(Long id);
    
    // Get all submissions for an assignment
    List<SubmissionDto> GetSubmissionsByAssignment(Long assignmentId);
    
    // Get all submissions by a student
    List<SubmissionDto> GetSubmissionsByStudent(Long studentId);
    
    // Get a student's submission for an assignment
    SubmissionDto GetStudentSubmissionForAssignment(Long assignmentId, Long studentId);
    
    // Grade a submission
    SubmissionDto GradeSubmission(Long submissionId, GradeSubmissionDto gradeSubmissionDto, String teacherUsername);
    
    // Get all graded submissions for an assignment
    List<SubmissionDto> GetGradedSubmissionsByAssignment(Long assignmentId);
    
    // Get all ungraded submissions for an assignment
    List<SubmissionDto> GetUngradedSubmissionsByAssignment(Long assignmentId);
    
    // Get submission statistics for an assignment
    SubmissionStatistics GetSubmissionStatisticsForAssignment(Long assignmentId);
    
    // Inner class for submission statistics
    class SubmissionStatistics {
        private long totalStudents;
        private long submissionCount;
        private long gradedCount;
        private double averageScore;
        
        // Getters and setters
        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
        
        public long getSubmissionCount() { return submissionCount; }
        public void setSubmissionCount(long submissionCount) { this.submissionCount = submissionCount; }
        
        public long getGradedCount() { return gradedCount; }
        public void setGradedCount(long gradedCount) { this.gradedCount = gradedCount; }
        
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    }
} 