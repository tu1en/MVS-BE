package com.classroomapp.classroombackend.controller.classroommanagement;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSyllabusDto;
import com.classroomapp.classroombackend.service.classroommanagement.SyllabusService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Lấy tất cả syllabuses
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SyllabusDto>> getAllSyllabuses(Authentication authentication) {
        log.info("🔍 Getting all syllabuses - User: {}", authentication.getName());
        List<SyllabusDto> syllabuses = syllabusService.getAllSyllabuses();
        return ResponseEntity.ok(syllabuses);
    }

    /**
     * Lấy syllabus theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SyllabusDto> getSyllabusById(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("🔍 Getting syllabus by ID: {} - User: {}", id, authentication.getName());
        SyllabusDto syllabus = syllabusService.getSyllabusById(id);
        return ResponseEntity.ok(syllabus);
    }

    /**
     * Lấy syllabus theo classroom ID
     */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SyllabusDto> getSyllabusByClassroom(
            @PathVariable Long classroomId,
            Authentication authentication) {
        
        log.info("🔍 Getting syllabus for classroom ID: {} - User: {}", classroomId, authentication.getName());
        
        Optional<SyllabusDto> syllabus = syllabusService.getSyllabusByClassroomId(classroomId);
        if (syllabus.isPresent()) {
            return ResponseEntity.ok(syllabus.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tạo syllabus mới (chỉ text)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createSyllabus(
            @Valid @RequestBody CreateSyllabusDto createDto,
            Authentication authentication) {
        
        log.info("📝 Creating new syllabus for classroom ID: {} - User: {}", 
                createDto.getClassroomId(), authentication.getName());
        
        try {
            SyllabusDto newSyllabus = syllabusService.createSyllabus(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSyllabus);
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error creating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("❌ Error creating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Tạo syllabus với file upload
     */
    @PostMapping("/with-file")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createSyllabusWithFile(
            @Valid @RequestPart("syllabus") CreateSyllabusDto createDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        
        log.info("📝 Creating new syllabus with file for classroom ID: {} - User: {}", 
                createDto.getClassroomId(), authentication.getName());
        
        try {
            SyllabusDto newSyllabus = syllabusService.createSyllabusWithFile(createDto, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSyllabus);
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error creating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("❌ Error creating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Cập nhật syllabus (chỉ text)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateSyllabus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSyllabusDto updateDto,
            Authentication authentication) {
        
        log.info("📝 Updating syllabus ID: {} - User: {}", id, authentication.getName());
        
        try {
            SyllabusDto updatedSyllabus = syllabusService.updateSyllabus(id, updateDto);
            return ResponseEntity.ok(updatedSyllabus);
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error updating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("❌ Error updating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Cập nhật syllabus với file upload
     */
    @PutMapping("/{id}/with-file")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateSyllabusWithFile(
            @PathVariable Long id,
            @Valid @RequestPart("syllabus") UpdateSyllabusDto updateDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        
        log.info("📝 Updating syllabus with file, ID: {} - User: {}", id, authentication.getName());
        
        try {
            SyllabusDto updatedSyllabus = syllabusService.updateSyllabusWithFile(id, updateDto, file);
            return ResponseEntity.ok(updatedSyllabus);
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error updating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("❌ Error updating syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Upload file cho syllabus đã tồn tại
     */
    @PostMapping("/{id}/upload")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadSyllabusFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("📎 Uploading file for syllabus ID: {} - User: {}", id, authentication.getName());
        
        try {
            String fileUrl = syllabusService.uploadSyllabusFile(id, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error uploading file: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("❌ Error uploading file: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "UPLOAD_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Xóa syllabus
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSyllabus(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("🗑️ Deleting syllabus ID: {} - User: {}", id, authentication.getName());
        
        try {
            syllabusService.deleteSyllabus(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa syllabus thành công");
            response.put("deletedId", id);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("❌ Error deleting syllabus: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Kiểm tra classroom đã có syllabus chưa
     */
    @GetMapping("/classroom/{classroomId}/exists")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> checkClassroomHasSyllabus(
            @PathVariable Long classroomId,
            Authentication authentication) {
        
        log.info("🔍 Checking if classroom has syllabus, classroom ID: {} - User: {}", 
                classroomId, authentication.getName());
        
        boolean hasSyllabus = syllabusService.classroomHasSyllabus(classroomId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("classroomId", classroomId);
        response.put("hasSyllabus", hasSyllabus);
        response.put("message", hasSyllabus ? "Classroom đã có syllabus" : "Classroom chưa có syllabus");
        
        return ResponseEntity.ok(response);
    }
}