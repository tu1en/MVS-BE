package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.CourseFeedbackDto;
import com.classroomapp.classroombackend.service.CourseFeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseFeedbackController {
    
    @Autowired
    private CourseFeedbackService feedbackService;
    
    // Create new feedback
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CourseFeedbackDto> createFeedback(@Valid @RequestBody CourseFeedbackDto feedbackDto) {
        try {
            CourseFeedbackDto createdFeedback = feedbackService.createFeedback(feedbackDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFeedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get feedback by ID
    @GetMapping("/{feedbackId}")
    public ResponseEntity<CourseFeedbackDto> getFeedbackById(@PathVariable Long feedbackId) {
        try {
            CourseFeedbackDto feedback = feedbackService.getFeedbackById(feedbackId);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get all feedback by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseFeedbackDto>> getFeedbackByStudent(@PathVariable Long studentId) {
        try {
            List<CourseFeedbackDto> feedback = feedbackService.getFeedbackByStudent(studentId);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get all feedback for a classroom
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER')")
    public ResponseEntity<List<CourseFeedbackDto>> getFeedbackByClassroom(@PathVariable Long classroomId) {
        try {
            List<CourseFeedbackDto> feedback = feedbackService.getFeedbackByClassroom(classroomId);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get all feedback for a teacher
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseFeedbackDto>> getFeedbackByTeacher(@PathVariable Long teacherId) {
        try {
            List<CourseFeedbackDto> feedback = feedbackService.getFeedbackByTeacher(teacherId);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get feedback by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseFeedbackDto>> getFeedbackByStatus(@PathVariable String status) {
        List<CourseFeedbackDto> feedback = feedbackService.getFeedbackByStatus(status);
        return ResponseEntity.ok(feedback);
    }
    
    // Get feedback by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseFeedbackDto>> getFeedbackByCategory(@PathVariable String category) {
        List<CourseFeedbackDto> feedback = feedbackService.getFeedbackByCategory(category);
        return ResponseEntity.ok(feedback);
    }
    
    // Get feedback by rating range
    @GetMapping("/rating")
    public ResponseEntity<List<CourseFeedbackDto>> getFeedbackByRatingRange(
            @RequestParam Integer minRating, 
            @RequestParam Integer maxRating) {
        List<CourseFeedbackDto> feedback = feedbackService.getFeedbackByRatingRange(minRating, maxRating);
        return ResponseEntity.ok(feedback);
    }
    
    // Search feedback
    @GetMapping("/search")
    public ResponseEntity<List<CourseFeedbackDto>> searchFeedback(@RequestParam String keyword) {
        List<CourseFeedbackDto> feedback = feedbackService.searchFeedback(keyword);
        return ResponseEntity.ok(feedback);
    }
    
    // Get recent feedback
    @GetMapping("/recent")
    public ResponseEntity<List<CourseFeedbackDto>> getRecentFeedback(@RequestParam(defaultValue = "30") Integer days) {
        List<CourseFeedbackDto> feedback = feedbackService.getRecentFeedback(days);
        return ResponseEntity.ok(feedback);
    }
    
    // Get anonymous feedback
    @GetMapping("/anonymous")
    public ResponseEntity<List<CourseFeedbackDto>> getAnonymousFeedback() {
        List<CourseFeedbackDto> feedback = feedbackService.getAnonymousFeedback();
        return ResponseEntity.ok(feedback);
    }
    
    // Review feedback (by teacher or admin)
    @PutMapping("/{feedbackId}/review")
    public ResponseEntity<CourseFeedbackDto> reviewFeedback(
            @PathVariable Long feedbackId,
            @RequestParam String response,
            @RequestParam Long reviewerId) {
        try {
            CourseFeedbackDto reviewedFeedback = feedbackService.reviewFeedback(feedbackId, response, reviewerId);
            return ResponseEntity.ok(reviewedFeedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Acknowledge feedback
    @PutMapping("/{feedbackId}/acknowledge")
    public ResponseEntity<CourseFeedbackDto> acknowledgeFeedback(@PathVariable Long feedbackId) {
        try {
            CourseFeedbackDto acknowledgedFeedback = feedbackService.acknowledgeFeedback(feedbackId);
            return ResponseEntity.ok(acknowledgedFeedback);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Check if student already gave feedback for classroom
    @GetMapping("/check/{studentId}/{classroomId}")
    public ResponseEntity<Boolean> hasStudentGivenFeedback(
            @PathVariable Long studentId, 
            @PathVariable Long classroomId) {
        boolean hasFeedback = feedbackService.hasStudentGivenFeedback(studentId, classroomId);
        return ResponseEntity.ok(hasFeedback);
    }
    
    // Get average rating for classroom
    @GetMapping("/classroom/{classroomId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByClassroom(@PathVariable Long classroomId) {
        Double averageRating = feedbackService.getAverageRatingByClassroom(classroomId);
        return ResponseEntity.ok(averageRating);
    }
    
    // Get average teaching quality for teacher
    @GetMapping("/teacher/{teacherId}/average-teaching-quality")
    public ResponseEntity<Double> getAverageTeachingQualityByTeacher(@PathVariable Long teacherId) {
        Double averageQuality = feedbackService.getAverageTeachingQualityByTeacher(teacherId);
        return ResponseEntity.ok(averageQuality);
    }
    
    // Count feedback by status for teacher
    @GetMapping("/teacher/{teacherId}/count/{status}")
    public ResponseEntity<Long> countFeedbackByTeacherAndStatus(
            @PathVariable Long teacherId, 
            @PathVariable String status) {
        Long count = feedbackService.countFeedbackByTeacherAndStatus(teacherId, status);
        return ResponseEntity.ok(count);
    }
    
    // Delete feedback
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId) {
        try {
            feedbackService.deleteFeedback(feedbackId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get all feedback (admin only)
    @GetMapping("/all")
    public ResponseEntity<List<CourseFeedbackDto>> getAllFeedback() {
        List<CourseFeedbackDto> feedback = feedbackService.getAllFeedback();
        return ResponseEntity.ok(feedback);
    }
}
