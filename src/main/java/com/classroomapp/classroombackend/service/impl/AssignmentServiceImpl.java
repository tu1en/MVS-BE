package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.util.ModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final ModelMapper modelMapper;

    @Override
    public AssignmentDto GetAssignmentById(Long id) {
        Assignment assignment = FindAssignmentById(id);
        return modelMapper.MapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentDto CreateAssignment(CreateAssignmentDto createAssignmentDto) {
        // Get classroom
        Classroom classroom = classroomRepository.findById(createAssignmentDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", createAssignmentDto.getClassroomId()));
        
        // Create assignment
        Assignment assignment = new Assignment();
        assignment.setTitle(createAssignmentDto.getTitle());
        assignment.setDescription(createAssignmentDto.getDescription());
        assignment.setDueDate(createAssignmentDto.getDueDate());
        assignment.setPoints(createAssignmentDto.getPoints());
        assignment.setFileAttachmentUrl(createAssignmentDto.getFileAttachmentUrl());
        assignment.setClassroom(classroom);
        
        // Save and return
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return modelMapper.MapToAssignmentDto(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentDto UpdateAssignment(Long id, CreateAssignmentDto updateAssignmentDto) {
        // Get assignment
        Assignment assignment = FindAssignmentById(id);
        
        // If classroom ID has changed, get the new classroom
        if (!assignment.getClassroom().getId().equals(updateAssignmentDto.getClassroomId())) {
            Classroom classroom = classroomRepository.findById(updateAssignmentDto.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", updateAssignmentDto.getClassroomId()));
            assignment.setClassroom(classroom);
        }
        
        // Update fields
        assignment.setTitle(updateAssignmentDto.getTitle());
        assignment.setDescription(updateAssignmentDto.getDescription());
        assignment.setDueDate(updateAssignmentDto.getDueDate());
        assignment.setPoints(updateAssignmentDto.getPoints());
        assignment.setFileAttachmentUrl(updateAssignmentDto.getFileAttachmentUrl());
        
        // Save and return
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return modelMapper.MapToAssignmentDto(updatedAssignment);
    }

    @Override
    @Transactional
    public void DeleteAssignment(Long id) {
        Assignment assignment = FindAssignmentById(id);
        assignmentRepository.delete(assignment);
    }

    @Override
    public List<AssignmentDto> GetAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<Assignment> assignments = assignmentRepository.findByClassroomOrderByDueDateAsc(classroom);
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> GetUpcomingAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<Assignment> assignments = assignmentRepository.findByClassroomAndDueDateAfterOrderByDueDateAsc(
                classroom, LocalDateTime.now());
        
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> GetPastAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<Assignment> assignments = assignmentRepository.findByClassroomAndDueDateBeforeOrderByDueDateDesc(
                classroom, LocalDateTime.now());
        
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> SearchAssignmentsByTitle(String title) {
        List<Assignment> assignments = assignmentRepository.findByTitleContainingIgnoreCase(title);
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }
    
    // Helper method to find assignment by ID or throw exception
    private Assignment FindAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }
} 