package com.classroomapp.classroombackend.model;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_evaluations")
public class TeacherEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher; // Giảng viên được đánh giá
    
    @ManyToOne  
    @JoinColumn(name = "evaluator_id")
    private User evaluator; // Trợ giảng đánh giá
    
    @Column(name = "evaluation_date")
    private LocalDateTime evaluationDate;
    
    @Column(name = "teaching_quality_score")
    private Integer teachingQualityScore; // 1-5
    
    @Column(name = "student_interaction_score") 
    private Integer studentInteractionScore; // 1-5
    
    @Column(name = "punctuality_score")
    private Integer punctualityScore; // 1-5
    
    @Column(name = "overall_score")
    private Integer overallScore; // 1-5
    
    @Column(name = "comments", length = 1000)
    private String comments;
    
    @Column(name = "class_session_id")
    private Long classSessionId; // Buổi học được đánh giá

    // Constructors
    public TeacherEvaluation() {}

    public TeacherEvaluation(User teacher, User evaluator, LocalDateTime evaluationDate, 
                           Integer teachingQualityScore, Integer studentInteractionScore, 
                           Integer punctualityScore, Integer overallScore, String comments, 
                           Long classSessionId) {
        this.teacher = teacher;
        this.evaluator = evaluator;
        this.evaluationDate = evaluationDate;
        this.teachingQualityScore = teachingQualityScore;
        this.studentInteractionScore = studentInteractionScore;
        this.punctualityScore = punctualityScore;
        this.overallScore = overallScore;
        this.comments = comments;
        this.classSessionId = classSessionId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public User getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(User evaluator) {
        this.evaluator = evaluator;
    }

    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public Integer getTeachingQualityScore() {
        return teachingQualityScore;
    }

    public void setTeachingQualityScore(Integer teachingQualityScore) {
        this.teachingQualityScore = teachingQualityScore;
    }

    public Integer getStudentInteractionScore() {
        return studentInteractionScore;
    }

    public void setStudentInteractionScore(Integer studentInteractionScore) {
        this.studentInteractionScore = studentInteractionScore;
    }

    public Integer getPunctualityScore() {
        return punctualityScore;
    }

    public void setPunctualityScore(Integer punctualityScore) {
        this.punctualityScore = punctualityScore;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getClassSessionId() {
        return classSessionId;
    }

    public void setClassSessionId(Long classSessionId) {
        this.classSessionId = classSessionId;
    }
}