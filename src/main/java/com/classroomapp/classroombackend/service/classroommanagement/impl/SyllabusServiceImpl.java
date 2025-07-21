package com.classroomapp.classroombackend.service.classroommanagement.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateSyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSyllabusDto;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SyllabusRepository;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.classroommanagement.SyllabusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SyllabusServiceImpl implements SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final ClassroomRepository classroomRepository;
    private final FileStorageService fileStorageService;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    @Transactional(readOnly = true)
    public List<SyllabusDto> getAllSyllabuses() {
        return syllabusRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SyllabusDto getSyllabusById(Long id) {
        return syllabusRepository.findById(id).map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Syllabus not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SyllabusDto> findSyllabusById(Long id) {
        return syllabusRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SyllabusDto> getSyllabusByClassroomId(Long classroomId) {
        return syllabusRepository.findByClassroomId(classroomId).map(this::convertToDto);
    }

    @Override
    public SyllabusDto createSyllabus(CreateSyllabusDto createDto) {
        validateSyllabus(createDto);

        if (classroomHasSyllabus(createDto.getClassroomId())) {
            throw new RuntimeException("Classroom already has a syllabus.");
        }

        Classroom classroom = classroomRepository.findById(createDto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found with ID: " + createDto.getClassroomId()));

        Syllabus syllabus = new Syllabus();
        syllabus.setClassroom(classroom);
        syllabus.setTitle(createDto.getTrimmedTitle());
        syllabus.setContent(createDto.getContent());
        syllabus.setLearningObjectives(createDto.getLearningObjectives());
        syllabus.setRequiredMaterials(createDto.getRequiredMaterials());
        syllabus.setGradingCriteria(createDto.getGradingCriteria());

        return convertToDto(syllabusRepository.save(syllabus));
    }

    @Override
    public SyllabusDto createSyllabusWithFile(CreateSyllabusDto createDto, MultipartFile file) {
        if (file != null && !file.isEmpty()) validateSyllabusFile(file);

        SyllabusDto syllabusDto = createSyllabus(createDto);

        if (file != null && !file.isEmpty()) {
            try {
                String fileUrl = fileStorageService.save(file, "syllabus").getFileUrl();
                log.info("📎 Syllabus file uploaded: {}", fileUrl);
            } catch (Exception e) {
                log.error("❌ Error uploading syllabus file: {}", e.getMessage());
            }
        }
        return syllabusDto;
    }
@Override
public SyllabusDto updateSyllabus(Long id, UpdateSyllabusDto updateDto) {
    validateSyllabusUpdate(id, updateDto);

    Syllabus syllabus = syllabusRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Syllabus not found with ID: " + id));

    syllabus.setTitle(updateDto.getTrimmedTitle());
    syllabus.setContent(updateDto.getContent());
    syllabus.setLearningObjectives(updateDto.getLearningObjectives());
    syllabus.setRequiredMaterials(updateDto.getRequiredMaterials());
    syllabus.setGradingCriteria(updateDto.getGradingCriteria());

    return convertToDto(syllabusRepository.save(syllabus));
}

@Override
public SyllabusDto updateSyllabusWithFile(Long id, UpdateSyllabusDto updateDto, MultipartFile file) {
    if (file != null && !file.isEmpty()) validateSyllabusFile(file);

    SyllabusDto syllabusDto = updateSyllabus(id, updateDto);

    if (file != null && !file.isEmpty()) {
        try {
            String fileUrl = fileStorageService.save(file, "syllabus").getFileUrl();
            log.info("📎 Syllabus file updated: {}", fileUrl);
        } catch (Exception e) {
            log.error("❌ Error updating syllabus file: {}", e.getMessage());
        }
    }
    return syllabusDto;
}

    @Override
    public String uploadSyllabusFile(Long syllabusId, MultipartFile file) {
        if (!existsById(syllabusId)) throw new RuntimeException("Syllabus not found with ID: " + syllabusId);
        validateSyllabusFile(file);
        FileUploadResponse response = fileStorageService.save(file, "syllabus");
        return response.getFileUrl();
    }

    @Override
    public void deleteSyllabus(Long id) {
        if (!syllabusRepository.existsById(id)) {
            throw new RuntimeException("Syllabus not found with ID: " + id);
        }
        syllabusRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return syllabusRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean classroomExists(Long classroomId) {
        return classroomRepository.existsById(classroomId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean classroomHasSyllabus(Long classroomId) {
        return syllabusRepository.findByClassroomId(classroomId).isPresent();
    }

    @Override
    public void validateSyllabus(CreateSyllabusDto createDto) {
        if (!createDto.isValidTitle()) {
            throw new IllegalArgumentException("Syllabus title is required and cannot exceed 255 characters");
        }
        if (!classroomExists(createDto.getClassroomId())) {
            throw new IllegalArgumentException("Classroom not found with ID: " + createDto.getClassroomId());
        }
    }

    @Override
    public void validateSyllabusUpdate(Long syllabusId, UpdateSyllabusDto updateDto) {
        if (!updateDto.isValidTitle()) {
            throw new IllegalArgumentException("Syllabus title is required and cannot exceed 255 characters");
        }
    }

    @Override
    public void validateSyllabusFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is required");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("File size cannot exceed 10MB");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: PDF, DOC, DOCX, TXT");
        }
    }

    private SyllabusDto convertToDto(Syllabus syllabus) {
        SyllabusDto dto = new SyllabusDto();
        dto.setId(syllabus.getId());
        dto.setTitle(syllabus.getTitle());
        dto.setContent(syllabus.getContent());
        dto.setLearningObjectives(syllabus.getLearningObjectives());
        dto.setRequiredMaterials(syllabus.getRequiredMaterials());
        dto.setGradingCriteria(syllabus.getGradingCriteria());
        dto.setClassroomId(syllabus.getClassroom().getId());
        dto.setClassroomName(syllabus.getClassroom().getName());
        return dto;
    }
}
