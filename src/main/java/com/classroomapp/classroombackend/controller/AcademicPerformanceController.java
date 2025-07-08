package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@RestController
@RequestMapping("/api/academic-performance")
public class AcademicPerformanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(AcademicPerformanceController.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> getStudentAcademicPerformance(Authentication authentication) {
        try {
            String usernameOrEmail = authentication.getName();
            User currentUser;
            
            // Check if the authentication name is an email or username
            if (usernameOrEmail.contains("@")) {
                currentUser = userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", usernameOrEmail));
            } else {
                currentUser = userRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", usernameOrEmail));
            }
            
            // Lấy dữ liệu thực từ database
            Map<String, Object> response = new HashMap<>();
            List<Submission> submissions = submissionRepository.findByStudent(currentUser);
            
            // Nhóm submissions theo môn học
            Map<String, List<Double>> scoresBySubject = new HashMap<>();
            
            for (Submission submission : submissions) {
                Assignment assignment = submission.getAssignment();
                if (assignment != null && submission.getScore() != null) {
                    String subjectName = "Unknown Subject";
                    if (assignment.getClassroom() != null && assignment.getClassroom().getSubject() != null) {
                        subjectName = assignment.getClassroom().getSubject();
                    }
                    
                    scoresBySubject.putIfAbsent(subjectName, new ArrayList<>());
                    scoresBySubject.get(subjectName).add(submission.getScore().doubleValue());
                }
            }
            
            // Tính điểm trung bình cho từng môn học
            List<Map<String, Object>> subjects = new ArrayList<>();
            double totalScore = 0;
            int subjectCount = 0;
            
            for (Map.Entry<String, List<Double>> entry : scoresBySubject.entrySet()) {
                String subject = entry.getKey();
                List<Double> scores = entry.getValue();
                
                if (!scores.isEmpty()) {
                    double sum = 0;
                    for (Double score : scores) {
                        sum += score;
                    }
                    double averageScore = sum / scores.size();
                    
                    Map<String, Object> subjectData = createSubject(subject, averageScore);
                    subjects.add(subjectData);
                    totalScore += averageScore;
                    subjectCount++;
                }
            }
            
            response.put("subjects", subjects);
            
            // Tính điểm trung bình tổng thể
            double overallAverageScore = subjectCount > 0 ? totalScore / subjectCount : 0;
            response.put("averageScore", Math.round(overallAverageScore * 100.0) / 100.0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating student academic performance", e);
            return ResponseEntity.status(500).body(Map.of("error", "An internal error occurred: " + e.getMessage()));
        }
    }
    
    private Map<String, Object> createSubject(String name, double score) {
        Map<String, Object> subject = new HashMap<>();
        subject.put("subject", name);
        subject.put("score", score);
        
        // Determine rank based on score
        String rank;
        if (score >= 8.5) {
            rank = "Giỏi";
        } else if (score >= 6.5) {
            rank = "Khá";
        } else {
            rank = "Trung bình";
        }
        subject.put("rank", rank);
        
        return subject;
    }
} 