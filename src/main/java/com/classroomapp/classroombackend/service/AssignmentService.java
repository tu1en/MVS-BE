package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.dto.CreateAssignmentDto;

import java.util.List;

public interface AssignmentService {
    
    // Get an assignment by ID
    AssignmentDto GetAssignmentById(Long id);
    
    // Create a new assignment
    AssignmentDto CreateAssignment(CreateAssignmentDto createAssignmentDto);
    
    // Update an existing assignment
    AssignmentDto UpdateAssignment(Long id, CreateAssignmentDto updateAssignmentDto);
    
    // Delete an assignment
    void DeleteAssignment(Long id);
    
    // Get all assignments for a classroom
    List<AssignmentDto> GetAssignmentsByClassroom(Long classroomId);
    
    // Get upcoming assignments for a classroom
    List<AssignmentDto> GetUpcomingAssignmentsByClassroom(Long classroomId);
    
    // Get past assignments for a classroom
    List<AssignmentDto> GetPastAssignmentsByClassroom(Long classroomId);
    
    // Search for assignments by title
    List<AssignmentDto> SearchAssignmentsByTitle(String title);
} 