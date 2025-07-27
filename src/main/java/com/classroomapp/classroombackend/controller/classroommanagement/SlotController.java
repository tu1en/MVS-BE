package com.classroomapp.classroombackend.controller.classroommanagement;

import com.classroomapp.classroombackend.dto.classroommanagement.SlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateSlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSlotDto;
import com.classroomapp.classroombackend.service.classroommanagement.SlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller cho Slot Management
 */
@RestController
@RequestMapping("/api/classroom-management/slots")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class SlotController {

    private final SlotService slotService;

    /**
     * Láº¥y táº¥t cáº£ slots vá»›i pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<SlotDto>> getAllSlots(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        log.info("ðŸ” Getting all slots - User: {}", authentication.getName());
        Page<SlotDto> slots = slotService.getAllSlots(pageable);
        return ResponseEntity.ok(slots);
    }

    /**
     * Láº¥y slot theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SlotDto> getSlotById(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("ðŸ” Getting slot by ID: {} - User: {}", id, authentication.getName());
        SlotDto slot = slotService.getSlotById(id);
        return ResponseEntity.ok(slot);
    }

    /**
     * Táº¡o slot má»›i
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SlotDto> createSlot(
            @Valid @RequestBody CreateSlotDto createDto,
            Authentication authentication) {
        
        log.info("ðŸ“ Creating new slot: {} - User: {}", createDto.getSlotName(), authentication.getName());
        SlotDto newSlot = slotService.createSlot(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSlot);
    }

    /**
     * Cáº­p nháº­t slot
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SlotDto> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSlotDto updateDto,
            Authentication authentication) {
        
        log.info("ðŸ“ Updating slot ID: {} - User: {}", id, authentication.getName());
        SlotDto updatedSlot = slotService.updateSlot(id, updateDto);
        return ResponseEntity.ok(updatedSlot);
    }

    /**
     * XÃ³a slot
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSlot(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("ðŸ—‘ï¸ Deleting slot ID: {} - User: {}", id, authentication.getName());
        slotService.deleteSlot(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "XÃ³a slot thÃ nh cÃ´ng");
        response.put("deletedId", id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Láº¥y slots theo session ID
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SlotDto>> getSlotsBySession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        
        log.info("ðŸ” Getting slots for session ID: {} - User: {}", sessionId, authentication.getName());
        List<SlotDto> slots = slotService.getSlotsBySession(sessionId);
        return ResponseEntity.ok(slots);
    }

    /**
     * Láº¥y slots theo session ID vá»›i pagination
     */
    @GetMapping("/session/{sessionId}/paged")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<SlotDto>> getSlotsBySessionPaged(
            @PathVariable Long sessionId,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        
        log.info("ðŸ” Getting slots for session ID: {} with pagination - User: {}", sessionId, authentication.getName());
        Page<SlotDto> slots = slotService.getSlotsBySession(sessionId, pageable);
        return ResponseEntity.ok(slots);
    }
}
