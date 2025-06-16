package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.CourseFeedbackDto;

public interface CourseFeedbackService {
    
    // Create new feedback
    CourseFeedbackDto createFeedback(CourseFeedbackDto feedbackDto);
    
    // Get feedback by ID
    CourseFeedbackDto getFeedbackById(Long feedbackId);
    
    // Get all feedback by student
    List<CourseFeedbackDto> getFeedbackByStudent(Long studentId);
    
    // Get all feedback for a classroom
    List<CourseFeedbackDto> getFeedbackByClassroom(Long classroomId);
    
    // Get all feedback for a teacher
    List<CourseFeedbackDto> getFeedbackByTeacher(Long teacherId);
    
    // Get feedback by status
    List<CourseFeedbackDto> getFeedbackByStatus(String status);
    
    // Get feedback by category
    List<CourseFeedbackDto> getFeedbackByCategory(String category);
    
    // Get feedback by rating range
    List<CourseFeedbackDto> getFeedbackByRatingRange(Integer minRating, Integer maxRating);
    
    // Search feedback
    List<CourseFeedbackDto> searchFeedback(String keyword);
    
    // Get recent feedback
    List<CourseFeedbackDto> getRecentFeedback(Integer days);
    
    // Get anonymous feedback
    List<CourseFeedbackDto> getAnonymousFeedback();
    
    // Review feedback (by teacher or admin)
    CourseFeedbackDto reviewFeedback(Long feedbackId, String response, Long reviewerId);
    
    // Acknowledge feedback
    CourseFeedbackDto acknowledgeFeedback(Long feedbackId);
    
    // Check if student already gave feedback for classroom
    boolean hasStudentGivenFeedback(Long studentId, Long classroomId);
    
    // Get average rating for classroom
    Double getAverageRatingByClassroom(Long classroomId);
    
    // Get average teaching quality for teacher
    Double getAverageTeachingQualityByTeacher(Long teacherId);
    
    // Count feedback by status for teacher
    Long countFeedbackByTeacherAndStatus(Long teacherId, String status);
    
    // Delete feedback
    void deleteFeedback(Long feedbackId);
    
    // Get all feedback (admin only)
    List<CourseFeedbackDto> getAllFeedback();
}
