package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.StudentQuestionDto;
import com.classroomapp.classroombackend.model.StudentQuestion;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.StudentQuestionRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.StudentQuestionService;

@Service
@Transactional
public class StudentQuestionServiceImpl implements StudentQuestionService {
    
    @Autowired
    private StudentQuestionRepository questionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public StudentQuestionDto createQuestion(StudentQuestionDto questionDto) {
        User student = userRepository.findById(questionDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User teacher = userRepository.findById(questionDto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        StudentQuestion question = new StudentQuestion();
        question.setStudent(student);
        question.setTeacher(teacher);
        question.setSubject(questionDto.getSubject());
        question.setContent(questionDto.getContent());
        question.setPriority(questionDto.getPriority() != null ? questionDto.getPriority() : "MEDIUM");
        question.setStatus("PENDING");
        
        StudentQuestion savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }
    
    @Override
    public StudentQuestionDto answerQuestion(Long questionId, String answer, Long teacherId) {
        StudentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        question.markAsAnswered(answer, teacher);
        StudentQuestion savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StudentQuestionDto getQuestionById(Long questionId) {
        StudentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return convertToDto(question);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getQuestionsByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        List<StudentQuestion> questions = questionRepository.findByStudentOrderByCreatedAtDesc(student);
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getQuestionsByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        List<StudentQuestion> questions = questionRepository.findByTeacherOrderByCreatedAtDesc(teacher);
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getPendingQuestionsByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        List<StudentQuestion> questions = questionRepository.findByTeacherAndStatusOrderByCreatedAtDesc(teacher, "PENDING");
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getQuestionsByStatus(String status) {
        List<StudentQuestion> questions = questionRepository.findByStatusOrderByCreatedAtDesc(status);
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getQuestionsByPriority(String priority) {
        List<StudentQuestion> questions = questionRepository.findByPriorityOrderByCreatedAtDesc(priority);
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getConversation(Long studentId, Long teacherId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        List<StudentQuestion> questions = questionRepository.findByStudentAndTeacherOrderByCreatedAtDesc(student, teacher);
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> searchQuestions(String keyword) {
        List<StudentQuestion> questions = questionRepository.searchQuestions(keyword);
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> getRecentQuestions() {
        List<StudentQuestion> questions = questionRepository.findRecentQuestions();
        return questions.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public StudentQuestionDto closeQuestion(Long questionId) {
        StudentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setStatus("CLOSED");
        question.setUpdatedAt(LocalDateTime.now());
        StudentQuestion savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }
    
    @Override
    public StudentQuestionDto updateQuestionPriority(Long questionId, String priority) {
        StudentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setPriority(priority);
        question.setUpdatedAt(LocalDateTime.now());
        StudentQuestion savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countPendingQuestionsByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return questionRepository.countPendingQuestionsByTeacher(teacher);
    }
    
    @Override
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(questionId);
    }
    
    private StudentQuestionDto convertToDto(StudentQuestion question) {
        StudentQuestionDto dto = new StudentQuestionDto();
        dto.setId(question.getId());
        dto.setStudentId(question.getStudent().getId());
        dto.setStudentName(question.getStudent().getFullName());
        dto.setTeacherId(question.getTeacher().getId());
        dto.setTeacherName(question.getTeacher().getFullName());
        dto.setSubject(question.getSubject());
        dto.setContent(question.getContent());
        dto.setPriority(question.getPriority());
        dto.setStatus(question.getStatus());
        dto.setAnswer(question.getAnswer());
        dto.setAnsweredAt(question.getAnsweredAt());
        if (question.getAnsweredBy() != null) {
            dto.setAnsweredById(question.getAnsweredBy().getId());
            dto.setAnsweredByName(question.getAnsweredBy().getFullName());
        }
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }
}
