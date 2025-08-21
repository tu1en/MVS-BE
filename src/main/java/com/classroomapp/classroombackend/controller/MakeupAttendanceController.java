package com.classroomapp.classroombackend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.classroomapp.classroombackend.dto.attendancemanagement.CreateMakeupAttendanceRequestDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MakeupAttendanceApprovalDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MakeupAttendanceRequestDto;
import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest.RequestStatus;
import com.classroomapp.classroombackend.service.MakeupAttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing makeup attendance requests
 */
@RestController
@RequestMapping("/api/makeup-attendance")
@RequiredArgsConstructor
@Slf4j
public class MakeupAttendanceController {
    
    private final MakeupAttendanceService makeupAttendanceService;
    
    /**
     * Create a new makeup attendance request
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<MakeupAttendanceRequestDto> createRequest(
            @Valid @RequestBody CreateMakeupAttendanceRequestDto dto,
            Principal principal) {
        
        log.info("Creating makeup attendance request for teacher: {}", principal.getName());
        MakeupAttendanceRequestDto result = makeupAttendanceService.createRequest(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Get all requests for the current teacher
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<MakeupAttendanceRequestDto>> getMyRequests(Principal principal) {
        List<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getMyRequests(principal.getName());
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all requests for the current teacher with pagination
     */
    @GetMapping("/my-requests/paged")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Page<MakeupAttendanceRequestDto>> getMyRequestsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getMyRequests(principal.getName(), pageable);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all pending requests (for manager acknowledgment)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<MakeupAttendanceRequestDto>> getPendingRequests() {
        List<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all pending requests with pagination (for manager acknowledgment)
     */
    @GetMapping("/pending/paged")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<MakeupAttendanceRequestDto>> getPendingRequestsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getPendingRequests(pageable);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get requests by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<MakeupAttendanceRequestDto>> getRequestsByStatus(
            @PathVariable RequestStatus status) {
        
        List<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get a specific request by ID
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MakeupAttendanceRequestDto> getRequestById(@PathVariable Long requestId) {
        MakeupAttendanceRequestDto request = makeupAttendanceService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }
    
    /**
     * Acknowledge a makeup attendance request
     */
    @PostMapping("/{requestId}/acknowledge")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<MakeupAttendanceRequestDto> acknowledgeRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody MakeupAttendanceApprovalDto approvalDto,
            Principal principal) {

        log.info("Acknowledging makeup attendance request {} by manager: {}", requestId, principal.getName());
        MakeupAttendanceRequestDto result = makeupAttendanceService.acknowledgeRequest(
                requestId,
                principal.getName(),
                approvalDto.getManagerNotes()
        );
        return ResponseEntity.ok(result);
    }
    
    /**
     * Check if a teacher can create a makeup request for a specific lecture
     */
    @GetMapping("/can-create/{lectureId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Boolean>> canCreateMakeupRequest(
            @PathVariable Long lectureId,
            Principal principal) {
        
        boolean canCreate = makeupAttendanceService.canCreateMakeupRequest(lectureId, principal.getName());
        return ResponseEntity.ok(Map.of("canCreate", canCreate));
    }
    
    /**
     * Get acknowledged requests for the current teacher
     */
    @GetMapping("/acknowledged")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<MakeupAttendanceRequestDto>> getAcknowledgedRequests(Principal principal) {
        List<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getAcknowledgedRequestsForTeacher(principal.getName());
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Mark a request as completed (internal use when makeup attendance is taken)
     */
    @PostMapping("/{requestId}/complete")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> markRequestAsCompleted(@PathVariable Long requestId) {
        log.info("Marking makeup attendance request {} as completed", requestId);
        makeupAttendanceService.markRequestAsCompleted(requestId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get statistics for the current teacher
     */
    @GetMapping("/my-statistics")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Long>> getMyStatistics(Principal principal) {
        Map<String, Long> statistics = makeupAttendanceService.getTeacherStatistics(principal.getName());
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get overall statistics (for managers/admins)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getOverallStatistics() {
        Map<String, Long> statistics = makeupAttendanceService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get recent requests for dashboard
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<MakeupAttendanceRequestDto>> getRecentRequests() {
        List<MakeupAttendanceRequestDto> requests = makeupAttendanceService.getRecentRequests();
        return ResponseEntity.ok(requests);
    }
}
