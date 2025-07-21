package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftAssignmentDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UserShiftAssignmentDto;
import com.classroomapp.classroombackend.service.hrmanagement.UserShiftAssignmentService;
import com.classroomapp.classroombackend.utils.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for User Shift Assignment management
 * Only accessible by MANAGER and ADMIN roles
 */
@RestController
@RequestMapping("/api/hr/user-shift-assignments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public class UserShiftAssignmentController {
    
    private final UserShiftAssignmentService assignmentService;
    
    /**
     * Create shift assignments for multiple users
     * POST /api/hr/user-shift-assignments
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createShiftAssignments(@Valid @RequestBody CreateShiftAssignmentDto createDto) {
        try {
            log.info("Creating shift assignments for {} users", createDto.getUserIds().size());
            
            Long currentUserId = AuthUtils.getCurrentUserId();
            List<UserShiftAssignmentDto> assignments = assignmentService.createShiftAssignments(createDto, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Phân công ca làm việc đã được tạo thành công");
            response.put("data", assignments);
            response.put("count", assignments.size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error creating shift assignments: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Unexpected error creating shift assignments", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tạo phân công ca làm việc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update a shift assignment
     * PUT /api/hr/shift-assignments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateShiftAssignment(@PathVariable Long id, 
                                                                   @Valid @RequestBody CreateShiftAssignmentDto updateDto) {
        try {
            log.info("Updating shift assignment with ID: {}", id);
            
            UserShiftAssignmentDto updatedAssignment = assignmentService.updateShiftAssignment(id, updateDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Phân công ca làm việc đã được cập nhật thành công");
            response.put("data", updatedAssignment);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error updating shift assignment: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Unexpected error updating shift assignment", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi cập nhật phân công ca làm việc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get shift assignment by ID
     * GET /api/hr/shift-assignments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAssignmentById(@PathVariable Long id) {
        try {
            UserShiftAssignmentDto assignment = assignmentService.getAssignmentById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", assignment);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error getting shift assignment by ID: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy thông tin phân công ca làm việc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all shift assignments with pagination
     * GET /api/hr/user-shift-assignments
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserShiftAssignmentDto> assignments;
            if (startDate != null && endDate != null) {
                assignments = assignmentService.getAssignmentsInDateRange(startDate, endDate, pageable);
            } else {
                assignments = assignmentService.getAssignments(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", assignments.getContent());
            response.put("currentPage", assignments.getNumber());
            response.put("totalItems", assignments.getTotalElements());
            response.put("totalPages", assignments.getTotalPages());
            response.put("pageSize", assignments.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting shift assignments", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy danh sách phân công ca làm việc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get assignments by user
     * GET /api/hr/user-shift-assignments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getAssignmentsByUser(@PathVariable Long userId,
                                                                  @RequestParam(defaultValue = "true") boolean activeOnly) {
        try {
            List<UserShiftAssignmentDto> assignments = assignmentService.getAssignmentsByUser(userId, activeOnly);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", assignments);
            response.put("count", assignments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting assignments by user: {}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy phân công ca của người dùng");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get assignments by shift
     * GET /api/hr/user-shift-assignments/shift/{shiftId}
     */
    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<Map<String, Object>> getAssignmentsByShift(@PathVariable Long shiftId,
                                                                   @RequestParam(defaultValue = "true") boolean activeOnly) {
        try {
            List<UserShiftAssignmentDto> assignments = assignmentService.getAssignmentsByShift(shiftId, activeOnly);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", assignments);
            response.put("count", assignments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting assignments by shift: {}", shiftId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy phân công ca của ca làm việc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get assignments for a specific date
     * GET /api/hr/shift-assignments/date/{date}
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getAssignmentsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<UserShiftAssignmentDto> assignments = assignmentService.getAssignmentsForDate(date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", assignments);
            response.put("count", assignments.size());
            response.put("date", date);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting assignments for date: {}", date, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy phân công ca theo ngày");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get eligible users for shift assignment
     * GET /api/hr/shift-assignments/eligible-users
     */
    @GetMapping("/eligible-users")
    public ResponseEntity<Map<String, Object>> getEligibleUsers() {
        try {
            List<Object> eligibleUsers = assignmentService.getEligibleUsersForShiftAssignment();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", eligibleUsers);
            response.put("count", eligibleUsers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting eligible users", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy danh sách người dùng có thể phân công");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Check for overlapping assignments
     * GET /api/hr/user-shift-assignments/check-overlap
     */
    @GetMapping("/check-overlap")
    public ResponseEntity<Map<String, Object>> checkOverlappingAssignments(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<UserShiftAssignmentDto> overlapping = assignmentService.checkOverlappingAssignments(
                userId, startDate, endDate, excludeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasOverlap", !overlapping.isEmpty());
            response.put("overlappingAssignments", overlapping);
            response.put("count", overlapping.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking overlapping assignments", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi kiểm tra trùng lặp phân công");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a shift assignment
     * DELETE /api/hr/user-shift-assignments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Phân công ca làm việc đã được xóa thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error deleting shift assignment", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa phân công ca làm việc");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Toggle assignment status
     * PATCH /api/hr/shift-assignments/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleAssignmentStatus(@PathVariable Long id, 
                                                                    @RequestParam boolean isActive) {
        try {
            UserShiftAssignmentDto updatedAssignment = assignmentService.toggleAssignmentStatus(id, isActive);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isActive ? "Phân công ca đã được kích hoạt" : "Phân công ca đã được vô hiệu hóa");
            response.put("data", updatedAssignment);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error toggling assignment status", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi thay đổi trạng thái phân công");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
