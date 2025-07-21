package com.classroomapp.classroombackend.service.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;

/**
 * Service interface for Classroom management
 */
public interface ClassroomService {

    /**
     * Get all classrooms with pagination
     */
    Page<ClassroomDto> getAllClassrooms(Pageable pageable);

    /**
     * Get classroom by ID (throws exception if not found)
     */
    ClassroomDto getClassroomById(Long id);

    /**
     * Get classroom by ID (returns Optional)
     */
    Optional<ClassroomDto> findClassroomById(Long id);

    /**
     * Get classrooms by teacher ID
     */
    List<ClassroomDto> getClassroomsByTeacherId(Long teacherId);

    /**
     * Get classrooms by teacher (alias for controller)
     */
    List<ClassroomDto> getClassroomsByTeacher(Long teacherId);

    /**
     * Search classrooms by keyword
     */
    Page<ClassroomDto> searchClassrooms(String keyword, Pageable pageable);

    /**
     * Create new classroom
     */
    ClassroomDto createClassroom(CreateClassroomDto createDto);

    /**
     * Update existing classroom
     */
    ClassroomDto updateClassroom(Long id, UpdateClassroomDto updateDto);

    /**
     * Delete classroom
     */
    void deleteClassroom(Long id);

    /**
     * Check if classroom exists by ID
     */
    boolean existsById(Long id);

    /**
     * Count classrooms by teacher ID
     */
    long countClassroomsByTeacherId(Long teacherId);
}
