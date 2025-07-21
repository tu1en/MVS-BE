package com.classroomapp.classroombackend.service.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSyllabusDto;

/**
 * Service interface for Syllabus management
 */
public interface SyllabusService {

    /**
     * Get all syllabuses
     */
    List<SyllabusDto> getAllSyllabuses();

    /**
     * Get syllabus by ID
     */
    SyllabusDto getSyllabusById(Long id);

    /**
     * Get syllabus by ID (returns Optional)
     */
    Optional<SyllabusDto> findSyllabusById(Long id);

    /**
     * Get syllabus by classroom ID
     */
    Optional<SyllabusDto> getSyllabusByClassroomId(Long classroomId);

    /**
     * Create new syllabus
     */
    SyllabusDto createSyllabus(CreateSyllabusDto createDto);

    /**
     * Create syllabus with file upload
     */
    SyllabusDto createSyllabusWithFile(CreateSyllabusDto createDto, MultipartFile file);

    /**
     * Update existing syllabus
     */
    SyllabusDto updateSyllabus(Long id, UpdateSyllabusDto updateDto);

    /**
     * Update syllabus with file upload
     */
    SyllabusDto updateSyllabusWithFile(Long id, UpdateSyllabusDto updateDto, MultipartFile file);

    /**
     * Delete syllabus
     */
    void deleteSyllabus(Long id);

    /**
     * Upload syllabus file
     */
    String uploadSyllabusFile(Long syllabusId, MultipartFile file);

    /**
     * Check if syllabus exists by ID
     */
    boolean existsById(Long id);

    /**
     * Check if classroom exists
     */
    boolean classroomExists(Long classroomId);

    /**
     * Check if classroom already has syllabus
     */
    boolean classroomHasSyllabus(Long classroomId);

    /**
     * Validate syllabus data
     */
    void validateSyllabus(CreateSyllabusDto createDto);

    /**
     * Validate syllabus update data
     */
    void validateSyllabusUpdate(Long syllabusId, UpdateSyllabusDto updateDto);

    /**
     * Validate uploaded file
     */
    void validateSyllabusFile(MultipartFile file);
}