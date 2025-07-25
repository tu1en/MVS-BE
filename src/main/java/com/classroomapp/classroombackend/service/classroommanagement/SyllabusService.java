package com.classroomapp.classroombackend.service.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSyllabusDto;

public interface SyllabusService {

    // Lấy danh sách tất cả syllabus
    List<SyllabusDto> getAllSyllabuses();

    // Lấy chi tiết syllabus
    SyllabusDto getSyllabusById(Long id);
    Optional<SyllabusDto> findSyllabusById(Long id);

    // Lấy syllabus theo classroom
    Optional<SyllabusDto> getSyllabusByClassroomId(Long classroomId);
    boolean classroomHasSyllabus(Long classroomId);

    // Tạo syllabus
    SyllabusDto createSyllabus(CreateSyllabusDto dto);
    SyllabusDto createSyllabusWithFile(CreateSyllabusDto dto, MultipartFile file);

    // Cập nhật syllabus
    SyllabusDto updateSyllabus(Long id, UpdateSyllabusDto dto);
    SyllabusDto updateSyllabusWithFile(Long id, UpdateSyllabusDto dto, MultipartFile file);

    // Xóa syllabus
    void deleteSyllabus(Long id);

    // Upload file
    String uploadSyllabusFile(Long syllabusId, MultipartFile file);

    // Tìm kiếm syllabus
    Page<SyllabusDto> searchSyllabuses(String keyword, String subject, String gradeLevel, Pageable pageable);

    // Validate
    void validateSyllabus(CreateSyllabusDto dto);
    void validateSyllabusUpdate(Long id, UpdateSyllabusDto dto);
    void validateSyllabusFile(MultipartFile file);

    // Kiểm tra sự tồn tại
    boolean existsById(Long id);
    boolean classroomExists(Long classroomId);
}
