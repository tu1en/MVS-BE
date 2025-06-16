package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.StudentQuestionDto;

public interface StudentQuestionService {
    
    // Create a new question
    StudentQuestionDto createQuestion(StudentQuestionDto questionDto);
    
    // Answer a question
    StudentQuestionDto answerQuestion(Long questionId, String answer, Long teacherId);
    
    // Get question by ID
    StudentQuestionDto getQuestionById(Long questionId);
    
    // Get all questions by student
    List<StudentQuestionDto> getQuestionsByStudent(Long studentId);
    
    // Get all questions for a teacher
    List<StudentQuestionDto> getQuestionsByTeacher(Long teacherId);
    
    // Get pending questions for a teacher
    List<StudentQuestionDto> getPendingQuestionsByTeacher(Long teacherId);
    
    // Get questions by status
    List<StudentQuestionDto> getQuestionsByStatus(String status);
    
    // Get questions by priority
    List<StudentQuestionDto> getQuestionsByPriority(String priority);
    
    // Get conversation between student and teacher
    List<StudentQuestionDto> getConversation(Long studentId, Long teacherId);
    
    // Search questions
    List<StudentQuestionDto> searchQuestions(String keyword);
    
    // Get recent questions
    List<StudentQuestionDto> getRecentQuestions();
    
    // Close a question
    StudentQuestionDto closeQuestion(Long questionId);
    
    // Update question priority
    StudentQuestionDto updateQuestionPriority(Long questionId, String priority);
    
    // Count pending questions for a teacher
    Long countPendingQuestionsByTeacher(Long teacherId);
    
    // Delete a question
    void deleteQuestion(Long questionId);
}
