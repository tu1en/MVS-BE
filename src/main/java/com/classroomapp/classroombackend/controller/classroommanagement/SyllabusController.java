package com.classroomapp.classroombackend.controller.classroommanagement;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSyllabusDto;
import com.classroomapp.classroombackend.service.classroommanagement.SyllabusService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Syllabus Management
 */
@RestController
@RequestMapping("/api/classroom-management/syllabus")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class SyllabusController {

    private final SyllabusService syllabusService;

    /** Lấy tất cả syllabuses */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SyllabusDto>> getAllSyllabuses(Authentication authentication) {
        log.info("🔍 Getting all syllabuses - User: {}", authentication.getName());
        return ResponseEntity.ok(syllabusService.getAllSyllabuses());
    }

    /** Lấy syllabus theo ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SyllabusDto> getSyllabusById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("🔍 Getting syllabus by ID: {} - User: {}", id, authentication.getName());
        return ResponseEntity.ok(syllabusService.getSyllabusById(id));
    }

    /** Lấy syllabus theo classroom ID */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SyllabusDto> getSyllabusByClassroom(
            @PathVariable Long classroomId,
            Authentication authentication) {

        log.info("🔍 Getting syllabus for classroom ID: {} - User: {}", classroomId, authentication.getName());

        return syllabusService.getSyllabusByClassroomId(classroomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Tạo syllabus mới (chỉ text) */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createSyllabus(
            @Valid @RequestBody CreateSyllabusDto createDto,
            Authentication authentication) {

        log.info("📝 Creating new syllabus for classroom ID: {} - User: {}", createDto.getClassroomId(), authentication.getName());
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(syllabusService.createSyllabus(createDto));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("VALIDATION_ERROR", e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse("BUSINESS_ERROR", e.getMessage());
        }
    }

    /** Tạo syllabus với file upload */
    @PostMapping("/with-file")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createSyllabusWithFile(
            @Valid @RequestPart("syllabus") CreateSyllabusDto createDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        log.info("📝 Creating new syllabus with file for classroom ID: {} - User: {}", createDto.getClassroomId(), authentication.getName());
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(syllabusService.createSyllabusWithFile(createDto, file));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("VALIDATION_ERROR", e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse("BUSINESS_ERROR", e.getMessage());
        }
    }

    /** Cập nhật syllabus (chỉ text) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateSyllabus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSyllabusDto updateDto,
            Authentication authentication) {

        log.info("📝 Updating syllabus ID: {} - User: {}", id, authentication.getName());
        try {
            return ResponseEntity.ok(syllabusService.updateSyllabus(id, updateDto));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("VALIDATION_ERROR", e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse("BUSINESS_ERROR", e.getMessage());
        }
    }

    /** Cập nhật syllabus với file upload */
    @PutMapping("/{id}/with-file")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateSyllabusWithFile(
            @PathVariable Long id,
            @Valid @RequestPart("syllabus") UpdateSyllabusDto updateDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        log.info("📝 Updating syllabus with file, ID: {} - User: {}", id, authentication.getName());
        try {
            return ResponseEntity.ok(syllabusService.updateSyllabusWithFile(id, updateDto, file));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("VALIDATION_ERROR", e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse("BUSINESS_ERROR", e.getMessage());
        }
    }

    /** Upload file cho syllabus đã tồn tại */
    @PostMapping("/{id}/upload")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadSyllabusFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("📂 Uploading file for syllabus ID: {} - User: {}", id, authentication.getName());
        try {
            String fileUrl = syllabusService.uploadSyllabusFile(id, file);
            return ResponseEntity.ok(Map.of("success", true, "message", "File uploaded successfully", "fileUrl", fileUrl));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("VALIDATION_ERROR", e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse("UPLOAD_ERROR", e.getMessage());
        }
    }

    /** Xóa syllabus */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSyllabus(@PathVariable Long id, Authentication authentication) {
        log.info("🗑️ Deleting syllabus ID: {} - User: {}", id, authentication.getName());
        try {
            syllabusService.deleteSyllabus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa syllabus thành công", "deletedId", id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** Kiểm tra classroom đã có syllabus chưa */
    @GetMapping("/classroom/{classroomId}/exists")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> checkClassroomHasSyllabus(
            @PathVariable Long classroomId,
            Authentication authentication) {

        log.info("🔍 Checking if classroom has syllabus, classroom ID: {} - User: {}", classroomId, authentication.getName());
        boolean hasSyllabus = syllabusService.classroomHasSyllabus(classroomId);
        return ResponseEntity.ok(Map.of("classroomId", classroomId, "hasSyllabus", hasSyllabus,
                "message", hasSyllabus ? "Classroom đã có syllabus" : "Classroom chưa có syllabus"));
    }

    /** Helper method for error response */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String type, String message) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", message, "type", type));
    }
}
