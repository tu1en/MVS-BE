package com.classroomapp.classroombackend.controller.hrmanagement;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateWorkShiftDto;
import com.classroomapp.classroombackend.dto.hrmanagement.WorkShiftDto;
import com.classroomapp.classroombackend.service.hrmanagement.WorkShiftService;
import com.classroomapp.classroombackend.util.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Work Shift management
 * Only accessible by MANAGER and ADMIN roles
 */
@RestController
@RequestMapping("/api/hr/shifts")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public class WorkShiftController {
    
    private final WorkShiftService workShiftService;
    
    /**
     * Create a new work shift
     * POST /api/hr/shifts
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createShift(@Valid @RequestBody CreateWorkShiftDto createDto) {
        try {
            log.info("Creating new work shift: {}", createDto.getName());
            
            Long currentUserId = AuthUtils.getCurrentUserId();
            WorkShiftDto createdShift = workShiftService.createShift(createDto, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ca lÃ m viá»‡c Ä‘Ã£ Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng");
            response.put("data", createdShift);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Lỗi khi tạo ca làm việc: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Unexpected error creating work shift", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi táº¡o ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update an existing work shift
     * PUT /api/hr/shifts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateShift(@PathVariable Long id, 
                                                          @Valid @RequestBody CreateWorkShiftDto updateDto) {
        try {
            log.info("Updating work shift with ID: {}", id);
            
            WorkShiftDto updatedShift = workShiftService.updateShift(id, updateDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ca lÃ m viá»‡c Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t thÃ nh cÃ´ng");
            response.put("data", updatedShift);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Lỗi khi cập nhật ca làm việc: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Unexpected error updating work shift", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi cáº­p nháº­t ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get work shift by ID
     * GET /api/hr/shifts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getShiftById(@PathVariable Long id) {
        try {
            WorkShiftDto shift = workShiftService.getShiftById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", shift);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy ca làm việc theo ID: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi láº¥y thÃ´ng tin ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all active work shifts
     * GET /api/hr/shifts
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<WorkShiftDto> shifts;
            if (search != null && !search.trim().isEmpty()) {
                shifts = workShiftService.searchShiftsByName(search.trim(), pageable);
            } else {
                shifts = workShiftService.getShifts(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", shifts.getContent());
            response.put("currentPage", shifts.getNumber());
            response.put("totalItems", shifts.getTotalElements());
            response.put("totalPages", shifts.getTotalPages());
            response.put("pageSize", shifts.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách ca làm việc", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi láº¥y danh sÃ¡ch ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all active shifts (for dropdown/select)
     * GET /api/hr/shifts/active
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveShifts() {
        try {
            List<WorkShiftDto> shifts = workShiftService.getAllActiveShifts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", shifts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách ca làm việc đang hoạt động", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi láº¥y danh sÃ¡ch ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a work shift
     * DELETE /api/hr/shifts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteShift(@PathVariable Long id) {
        try {
            workShiftService.deleteShift(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ca lÃ m viá»‡c Ä‘Ã£ Ä‘Æ°á»£c xÃ³a thÃ nh cÃ´ng");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa ca làm việc", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi xÃ³a ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Toggle shift status (activate/deactivate)
     * PATCH /api/hr/shifts/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleShiftStatus(@PathVariable Long id, 
                                                               @RequestParam boolean isActive) {
        try {
            WorkShiftDto updatedShift = workShiftService.toggleShiftStatus(id, isActive);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isActive ? "Ca lÃ m viá»‡c Ä‘Ã£ Ä‘Æ°á»£c kÃ­ch hoáº¡t" : "Ca lÃ m viá»‡c Ä‘Ã£ Ä‘Æ°á»£c vÃ´ hiá»‡u hÃ³a");
            response.put("data", updatedShift);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi chuyển trạng thái ca làm việc", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi thay Ä‘á»•i tráº¡ng thÃ¡i ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Check if shift name is available
     * GET /api/hr/shifts/check-name
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkShiftName(@RequestParam String name, 
                                                            @RequestParam(required = false) Long excludeId) {
        try {
            boolean isAvailable = workShiftService.isShiftNameAvailable(name, excludeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", isAvailable);
            response.put("message", isAvailable ? "TÃªn ca lÃ m viá»‡c cÃ³ thá»ƒ sá»­ dá»¥ng" : "TÃªn ca lÃ m viá»‡c Ä‘Ã£ tá»“n táº¡i");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tên ca làm việc đã tồn tại", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "CÃ³ lá»—i xáº£y ra khi kiá»ƒm tra tÃªn ca lÃ m viá»‡c");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
