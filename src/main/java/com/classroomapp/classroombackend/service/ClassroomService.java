package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.CreateClassroomDto;

import java.util.List;

public interface ClassroomService {
    
    // Retrieve all classrooms
    List<ClassroomDto> getAllClassrooms();
    
    // Retrieve a classroom by its ID (lowercase method for video conference)
    ClassroomDto getClassroomById(Long id);
    
    // Retrieve a classroom by its ID
    ClassroomDto GetClassroomById(Long id);
    
    // Create a new classroom with the logged-in user as teacher
    ClassroomDto CreateClassroom(CreateClassroomDto createClassroomDto, Long teacherId);
    
    // Update an existing classroom
    ClassroomDto UpdateClassroom(Long id, CreateClassroomDto updateClassroomDto);
    
    // Delete a classroom
    void DeleteClassroom(Long id);
    
    // Get all classrooms taught by a specific teacher
    List<ClassroomDto> GetClassroomsByTeacher(Long teacherId);
    
    // Get all classrooms where a student is enrolled
    List<ClassroomDto> GetClassroomsByStudent(Long studentId);
    
    // Enroll a student in a classroom
    void EnrollStudent(Long classroomId, Long studentId);
    
    // Remove a student from a classroom
    void UnenrollStudent(Long classroomId, Long studentId);
    
    // Search for classrooms by name
    List<ClassroomDto> SearchClassroomsByName(String name);
    
    // Get classrooms by subject
    List<ClassroomDto> GetClassroomsBySubject(String subject);
}