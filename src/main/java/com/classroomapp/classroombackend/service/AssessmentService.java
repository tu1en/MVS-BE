package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.AssessmentDto;
import com.classroomapp.classroombackend.dto.CreateAssessmentDto;

public interface AssessmentService {
    AssessmentDto createAssessment(Long lectureId, CreateAssessmentDto createAssessmentDto, String userEmail);
    List<AssessmentDto> getAssessmentsByLectureId(Long lectureId);
    AssessmentDto getAssessmentById(Long assessmentId);
    void deleteAssessment(Long assessmentId, String userEmail);
} 