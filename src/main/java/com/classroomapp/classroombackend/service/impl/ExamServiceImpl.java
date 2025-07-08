package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.exammangement.CreateExamDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.exammangement.Exam;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.exammangement.ExamRepository;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;
import com.classroomapp.classroombackend.service.ExamService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomSecurityService classroomSecurityService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ExamDto createExam(CreateExamDto createExamDto) {
        Classroom classroom = classroomRepository.findById(createExamDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", createExamDto.getClassroomId()));

        Exam exam = new Exam();
        exam.setTitle(createExamDto.getTitle());
        exam.setClassroom(classroom);
        exam.setStartTime(createExamDto.getStartTime());
        exam.setEndTime(createExamDto.getEndTime());
        exam.setDurationInMinutes(createExamDto.getDurationInMinutes());

        Exam savedExam = examRepository.save(exam);
        return convertToDto(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsByClassroomId(Long classroomId) {
        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classroom", "id", classroomId);
        }
        List<Exam> exams = examRepository.findByClassroomId(classroomId);
        return exams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDto getExamById(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", examId));

        if (!classroomSecurityService.isMember(exam.getClassroom().getId())) {
            throw new AccessDeniedException("You are not a member of this classroom.");
        }

        return convertToDto(exam);
    }

    @Override
    @Transactional
    public ExamDto updateExam(Long examId, CreateExamDto createExamDto) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", examId));

        if (!classroomSecurityService.isTeacher(exam.getClassroom().getId())) {
            throw new AccessDeniedException("You are not the teacher of this classroom.");
        }
        
        Classroom classroom = classroomRepository.findById(createExamDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", createExamDto.getClassroomId()));

        exam.setTitle(createExamDto.getTitle());
        exam.setClassroom(classroom);
        exam.setStartTime(createExamDto.getStartTime());
        exam.setEndTime(createExamDto.getEndTime());
        exam.setDurationInMinutes(createExamDto.getDurationInMinutes());

        Exam updatedExam = examRepository.save(exam);
        return convertToDto(updatedExam);
    }

    @Override
    @Transactional
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", examId));

        if (!classroomSecurityService.isTeacher(exam.getClassroom().getId())) {
            throw new AccessDeniedException("You are not the teacher of this classroom.");
        }
        
        examRepository.deleteById(examId);
    }

    private ExamDto convertToDto(Exam exam) {
        ExamDto examDto = modelMapper.map(exam, ExamDto.class);
        if (exam.getClassroom() != null) {
            examDto.setClassroomId(exam.getClassroom().getId());
            examDto.setClassroomName(exam.getClassroom().getName());
        }
        return examDto;
    }
} 