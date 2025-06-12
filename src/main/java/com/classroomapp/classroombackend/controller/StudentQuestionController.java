package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.classroomapp.classroombackend.dto.StudentQuestionDto;
import com.classroomapp.classroombackend.service.StudentQuestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentQuestionController {
    
    @Autowired
    private StudentQuestionService questionService;
    
    // Create a new question
    @PostMapping
    public ResponseEntity<StudentQuestionDto> createQuestion(@Valid @RequestBody StudentQuestionDto questionDto) {
        try {
            StudentQuestionDto createdQuestion = questionService.createQuestion(questionDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get question by ID
    @GetMapping("/{questionId}")
    public ResponseEntity<StudentQuestionDto> getQuestionById(@PathVariable Long questionId) {
        try {
            StudentQuestionDto question = questionService.getQuestionById(questionId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get questions by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentQuestionDto>> getQuestionsByStudent(@PathVariable Long studentId) {
        try {
            List<StudentQuestionDto> questions = questionService.getQuestionsByStudent(studentId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get questions by teacher
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<StudentQuestionDto>> getQuestionsByTeacher(@PathVariable Long teacherId) {
        try {
            List<StudentQuestionDto> questions = questionService.getQuestionsByTeacher(teacherId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get pending questions for teacher
    @GetMapping("/teacher/{teacherId}/pending")
    public ResponseEntity<List<StudentQuestionDto>> getPendingQuestionsByTeacher(@PathVariable Long teacherId) {
        try {
            List<StudentQuestionDto> questions = questionService.getPendingQuestionsByTeacher(teacherId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get questions by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudentQuestionDto>> getQuestionsByStatus(@PathVariable String status) {
        List<StudentQuestionDto> questions = questionService.getQuestionsByStatus(status);
        return ResponseEntity.ok(questions);
    }
    
    // Get questions by priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<StudentQuestionDto>> getQuestionsByPriority(@PathVariable String priority) {
        List<StudentQuestionDto> questions = questionService.getQuestionsByPriority(priority);
        return ResponseEntity.ok(questions);
    }
    
    // Get conversation between student and teacher
    @GetMapping("/conversation")
    public ResponseEntity<List<StudentQuestionDto>> getConversation(
            @RequestParam Long studentId, @RequestParam Long teacherId) {
        try {
            List<StudentQuestionDto> questions = questionService.getConversation(studentId, teacherId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Search questions
    @GetMapping("/search")
    public ResponseEntity<List<StudentQuestionDto>> searchQuestions(@RequestParam String keyword) {
        List<StudentQuestionDto> questions = questionService.searchQuestions(keyword);
        return ResponseEntity.ok(questions);
    }
    
    // Get recent questions
    @GetMapping("/recent")
    public ResponseEntity<List<StudentQuestionDto>> getRecentQuestions() {
        List<StudentQuestionDto> questions = questionService.getRecentQuestions();
        return ResponseEntity.ok(questions);
    }
    
    // Answer a question
    @PutMapping("/{questionId}/answer")
    public ResponseEntity<StudentQuestionDto> answerQuestion(
            @PathVariable Long questionId,
            @RequestParam String answer,
            @RequestParam Long teacherId) {
        try {
            StudentQuestionDto answeredQuestion = questionService.answerQuestion(questionId, answer, teacherId);
            return ResponseEntity.ok(answeredQuestion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Close a question
    @PutMapping("/{questionId}/close")
    public ResponseEntity<StudentQuestionDto> closeQuestion(@PathVariable Long questionId) {
        try {
            StudentQuestionDto closedQuestion = questionService.closeQuestion(questionId);
            return ResponseEntity.ok(closedQuestion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Update question priority
    @PutMapping("/{questionId}/priority")
    public ResponseEntity<StudentQuestionDto> updateQuestionPriority(
            @PathVariable Long questionId, @RequestParam String priority) {
        try {
            StudentQuestionDto updatedQuestion = questionService.updateQuestionPriority(questionId, priority);
            return ResponseEntity.ok(updatedQuestion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Count pending questions for teacher
    @GetMapping("/teacher/{teacherId}/pending/count")
    public ResponseEntity<Long> countPendingQuestionsByTeacher(@PathVariable Long teacherId) {
        try {
            Long count = questionService.countPendingQuestionsByTeacher(teacherId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Delete a question
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        try {
            questionService.deleteQuestion(questionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
