package com.classroomapp.classroombackend.controller.classroommanagement;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSessionDto;
import com.classroomapp.classroombackend.service.classroommanagement.SessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Session Management
 */
@RestController
@RequestMapping("/api/classroom-management/sessions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class SessionController {

    private final SessionService sessionService;

    /**
     * Lấy tất cả sessions với pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<SessionDto>> getAllSessions(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        log.info("🔍 Getting all sessions - User: {}", authentication.getName());
        Page<SessionDto> sessions = sessionService.getAllSessions(pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Lấy session theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SessionDto> getSessionById(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("🔍 Getting session by ID: {} - User: {}", id, authentication.getName());
        SessionDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    /**
     * Tạo session mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SessionDto> createSession(
            @Valid @RequestBody CreateSessionDto createDto,
            Authentication authentication) {
        
        log.info("📝 Creating new session for classroom: {} - User: {}", createDto.getClassroomId(), authentication.getName());
        SessionDto newSession = sessionService.createSession(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSession);
    }

    /**
     * Cập nhật session
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SessionDto> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionDto updateDto,
            Authentication authentication) {
        
        log.info("📝 Updating session ID: {} - User: {}", id, authentication.getName());
        SessionDto updatedSession = sessionService.updateSession(id, updateDto);
        return ResponseEntity.ok(updatedSession);
    }

    /**
     * Xóa session
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSession(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("🗑️ Deleting session ID: {} - User: {}", id, authentication.getName());
        sessionService.deleteSession(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa session thành công");
        response.put("deletedId", id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy sessions theo classroom ID
     */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SessionDto>> getSessionsByClassroom(
            @PathVariable Long classroomId,
            Authentication authentication) {
        
        log.info("🔍 Getting sessions for classroom ID: {} - User: {}", classroomId, authentication.getName());
        List<SessionDto> sessions = sessionService.getSessionsByClassroom(classroomId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Lấy sessions theo ngày
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SessionDto>> getSessionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        
        log.info("🔍 Getting sessions for date: {} - User: {}", date, authentication.getName());
        List<SessionDto> sessions = sessionService.getSessionsByDate(date);
        return ResponseEntity.ok(sessions);
    }
}
