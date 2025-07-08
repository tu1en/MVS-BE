package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.exammangement.CreateExamDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamDto;

public interface ExamService {
    ExamDto createExam(CreateExamDto createExamDto);
    List<ExamDto> getExamsByClassroomId(Long classroomId);
    ExamDto getExamById(Long examId);
    ExamDto updateExam(Long examId, CreateExamDto createExamDto);
    void deleteExam(Long examId);
} 