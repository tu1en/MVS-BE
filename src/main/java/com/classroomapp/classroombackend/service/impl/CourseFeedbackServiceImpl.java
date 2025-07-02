package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CourseFeedbackDto;
import com.classroomapp.classroombackend.model.CourseFeedback;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseFeedbackRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.CourseFeedbackService;

@Service
@Transactional
public class CourseFeedbackServiceImpl implements CourseFeedbackService {
    
    @Autowired
    private CourseFeedbackRepository feedbackRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Override
    public CourseFeedbackDto createFeedback(CourseFeedbackDto feedbackDto) {
        User student = userRepository.findById(feedbackDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Classroom classroom = classroomRepository.findById(feedbackDto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        User teacher = userRepository.findById(feedbackDto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        // Check if student already gave feedback for this classroom
        if (feedbackRepository.existsByStudentAndClassroom(student, classroom)) {
            throw new RuntimeException("Student has already given feedback for this classroom");
        }
        
        CourseFeedback feedback = new CourseFeedback();
        feedback.setStudent(student);
        feedback.setClassroom(classroom);
        feedback.setTeacher(teacher);
        feedback.setTitle(feedbackDto.getTitle());
        feedback.setContent(feedbackDto.getContent());
        feedback.setOverallRating(feedbackDto.getOverallRating());
        feedback.setTeachingQualityRating(feedbackDto.getTeachingQualityRating());
        feedback.setCourseMaterialRating(feedbackDto.getCourseMaterialRating());
        feedback.setSupportRating(feedbackDto.getSupportRating());
        feedback.setCategory(feedbackDto.getCategory() != null ? feedbackDto.getCategory() : "GENERAL");
        feedback.setIsAnonymous(feedbackDto.getIsAnonymous() != null ? feedbackDto.getIsAnonymous() : false);
        feedback.setStatus("SUBMITTED");
        
        CourseFeedback savedFeedback = feedbackRepository.save(feedback);
        return convertToDto(savedFeedback);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseFeedbackDto getFeedbackById(Long feedbackId) {
        CourseFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        return convertToDto(feedback);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getFeedbackByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        List<CourseFeedback> feedbacks = feedbackRepository.findByStudentOrderByCreatedAtDesc(student);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getFeedbackByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        List<CourseFeedback> feedbacks = feedbackRepository.findByClassroomOrderByCreatedAtDesc(classroom);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getFeedbackByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        List<CourseFeedback> feedbacks = feedbackRepository.findByTeacherOrderByCreatedAtDesc(teacher);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getFeedbackByStatus(String status) {
        List<CourseFeedback> feedbacks = feedbackRepository.findByStatusOrderByCreatedAtDesc(status);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getFeedbackByCategory(String category) {
        List<CourseFeedback> feedbacks = feedbackRepository.findByCategoryOrderByCreatedAtDesc(category);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getFeedbackByRatingRange(Integer minRating, Integer maxRating) {
        List<CourseFeedback> feedbacks = feedbackRepository.findByOverallRatingBetweenOrderByCreatedAtDesc(minRating, maxRating);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> searchFeedback(String keyword) {
        List<CourseFeedback> feedbacks = feedbackRepository.searchFeedback(keyword);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getRecentFeedback(Integer days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days != null ? days : 30);
        List<CourseFeedback> feedbacks = feedbackRepository.findRecentFeedback(fromDate);
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getAnonymousFeedback() {
        List<CourseFeedback> feedbacks = feedbackRepository.findByIsAnonymousTrueOrderByCreatedAtDesc();
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public CourseFeedbackDto reviewFeedback(Long feedbackId, String response, Long reviewerId) {
        CourseFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        
        feedback.markAsReviewed(reviewer, response);
        CourseFeedback savedFeedback = feedbackRepository.save(feedback);
        return convertToDto(savedFeedback);
    }
    
    @Override
    public CourseFeedbackDto acknowledgeFeedback(Long feedbackId) {
        CourseFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        feedback.acknowledge();
        CourseFeedback savedFeedback = feedbackRepository.save(feedback);
        return convertToDto(savedFeedback);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentGivenFeedback(Long studentId, Long classroomId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        return feedbackRepository.existsByStudentAndClassroom(student, classroom);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        return feedbackRepository.getAverageRatingByClassroom(classroom);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getAverageTeachingQualityByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return feedbackRepository.getAverageTeachingQualityByTeacher(teacher);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countFeedbackByTeacherAndStatus(Long teacherId, String status) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return feedbackRepository.countFeedbackByTeacherAndStatus(teacher, status);
    }
    
    @Override
    public void deleteFeedback(Long feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new RuntimeException("Feedback not found");
        }
        feedbackRepository.deleteById(feedbackId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseFeedbackDto> getAllFeedback() {
        List<CourseFeedback> feedbacks = feedbackRepository.findAll();
        return feedbacks.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private CourseFeedbackDto convertToDto(CourseFeedback feedback) {
        CourseFeedbackDto dto = new CourseFeedbackDto();
        dto.setId(feedback.getId());
        dto.setStudentId(feedback.getStudent().getId());
        dto.setStudentName(feedback.getIsAnonymous() ? "Anonymous" : feedback.getStudent().getFullName());
        dto.setClassroomId(feedback.getClassroom().getId());
        dto.setClassroomName(feedback.getClassroom().getName());
        dto.setTeacherId(feedback.getTeacher().getId());
        dto.setTeacherName(feedback.getTeacher().getFullName());
        dto.setTitle(feedback.getTitle());
        dto.setContent(feedback.getContent());
        dto.setOverallRating(feedback.getOverallRating());
        dto.setTeachingQualityRating(feedback.getTeachingQualityRating());
        dto.setCourseMaterialRating(feedback.getCourseMaterialRating());
        dto.setSupportRating(feedback.getSupportRating());
        dto.setCategory(feedback.getCategory());
        dto.setStatus(feedback.getStatus());
        dto.setIsAnonymous(feedback.getIsAnonymous());
        dto.setReviewedAt(feedback.getReviewedAt());
        if (feedback.getReviewedBy() != null) {
            dto.setReviewedById(feedback.getReviewedBy().getId());
            dto.setReviewedByName(feedback.getReviewedBy().getFullName());
        }
        dto.setResponse(feedback.getResponse());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());
        return dto;
    }
}
