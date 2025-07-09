package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.AssessmentDto;
import com.classroomapp.classroombackend.dto.CreateAssessmentDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.UnauthorizedException;
import com.classroomapp.classroombackend.model.Assessment;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.repository.AssessmentRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.service.AssessmentService;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AssessmentDto createAssessment(Long lectureId, CreateAssessmentDto createAssessmentDto, String userEmail) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));

        if (!lecture.getClassroom().getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User is not authorized to create assessments for this lecture.");
        }

        Assessment assessment = modelMapper.map(createAssessmentDto, Assessment.class);
        assessment.setLecture(lecture);

        Assessment savedAssessment = assessmentRepository.save(assessment);
        return modelMapper.map(savedAssessment, AssessmentDto.class);
    }

    @Override
    public List<AssessmentDto> getAssessmentsByLectureId(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new ResourceNotFoundException("Lecture not found with id: " + lectureId);
        }
        List<Assessment> assessments = assessmentRepository.findByLectureId(lectureId);
        return assessments.stream()
                .map(assessment -> modelMapper.map(assessment, AssessmentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public AssessmentDto getAssessmentById(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));
        return modelMapper.map(assessment, AssessmentDto.class);
    }

    @Override
    public void deleteAssessment(Long assessmentId, String userEmail) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        if (!assessment.getLecture().getClassroom().getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User is not authorized to delete this assessment.");
        }

        assessmentRepository.delete(assessment);
    }
} 