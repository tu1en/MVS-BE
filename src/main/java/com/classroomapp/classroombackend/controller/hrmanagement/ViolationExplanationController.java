package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateExplanationDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ViolationExplanationDto;
import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationExplanationService;
import com.classroomapp.classroombackend.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Violation Explanation management
 */
@RestController
@RequestMapping("/api/hr/explanations")
@RequiredArgsConstructor
@Slf4j
public class ViolationExplanationController {

    private final ViolationExplanationService explanationService;
    private final SecurityUtils securityUtils;
    
    /**
     * Submit explanation for a violation
     * POST /api/hr/explanations
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> submitExplanation(
            @RequestParam Long violationId,
            @RequestParam String explanationText,
            @RequestParam(required = false) List<MultipartFile> evidenceFiles,
            @RequestParam(required = false) List<String> evidenceDescriptions,
            @RequestParam(required = false) List<String> evidenceTypes) {
        
        log.info("Submitting explanation for violation: {}", violationId);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            // Create DTO
            CreateExplanationDto createDto = new CreateExplanationDto();
            createDto.setViolationId(violationId);
            createDto.setExplanationText(explanationText);
            createDto.setEvidenceFiles(evidenceFiles);
            createDto.setEvidenceDescriptions(evidenceDescriptions);
            createDto.setEvidenceTypes(evidenceTypes);
            
            // Validate DTO
            if (!explanationService.validateExplanationData(createDto)) {
                List<String> errors = createDto.getValidationErrors();
                log.warn("Validation failed for explanation submission: {}", errors);
                return ResponseEntity.badRequest().build();
            }
            
            ViolationExplanationDto explanation = explanationService.submitExplanation(createDto, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(explanation);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid explanation submission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting explanation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing explanation
     * PUT /api/hr/explanations/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> updateExplanation(
            @PathVariable Long id,
            @RequestParam Long violationId,
            @RequestParam String explanationText,
            @RequestParam(required = false) List<MultipartFile> evidenceFiles,
            @RequestParam(required = false) List<String> evidenceDescriptions,
            @RequestParam(required = false) List<String> evidenceTypes) {
        
        log.info("Updating explanation: {}", id);
        
        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            // Create DTO
            CreateExplanationDto updateDto = new CreateExplanationDto();
            updateDto.setViolationId(violationId);
            updateDto.setExplanationText(explanationText);
            updateDto.setEvidenceFiles(evidenceFiles);
            updateDto.setEvidenceDescriptions(evidenceDescriptions);
            updateDto.setEvidenceTypes(evidenceTypes);
            
            // Validate DTO
            if (!explanationService.validateExplanationData(updateDto)) {
                List<String> errors = updateDto.getValidationErrors();
                log.warn("Validation failed for explanation update: {}", errors);
                return ResponseEntity.badRequest().build();
            }
            
            ViolationExplanationDto explanation = explanationService.updateExplanation(id, updateDto, currentUserId);
            return ResponseEntity.ok(explanation);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid explanation update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating explanation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get explanation by ID
     * GET /api/hr/explanations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> getExplanationById(@PathVariable Long id) {
        log.info("Getting explanation by ID: {}", id);
        
        try {
            ViolationExplanationDto explanation = explanationService.getExplanationById(id);
            return ResponseEntity.ok(explanation);
            
        } catch (IllegalArgumentException e) {
            log.warn("Explanation not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get explanations by current user
     * GET /api/hr/explanations/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ViolationExplanationDto>> getMyExplanations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting explanations for current user - page: {}, size: {}", page, size);
        
        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ViolationExplanationDto> explanations = explanationService.getExplanationsByUser(currentUserId, pageable);
        return ResponseEntity.ok(explanations);
    }
    
    /**
     * Get explanations by violation
     * GET /api/hr/explanations/violation/{violationId}
     */
    @GetMapping("/violation/{violationId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ViolationExplanationDto>> getExplanationsByViolation(@PathVariable Long violationId) {
        log.info("Getting explanations for violation: {}", violationId);
        
        List<ViolationExplanationDto> explanations = explanationService.getExplanationsByViolation(violationId);
        return ResponseEntity.ok(explanations);
    }
    
    /**
     * Get explanations pending review
     * GET /api/hr/explanations/pending-review
     */
    @GetMapping("/pending-review")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ViolationExplanationDto>> getPendingReviewExplanations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting explanations pending review - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").ascending());
        Page<ViolationExplanationDto> explanations = explanationService.getPendingReviewExplanations(pageable);
        return ResponseEntity.ok(explanations);
    }
    
    /**
     * Get explanations by status
     * GET /api/hr/explanations/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ViolationExplanationDto>> getExplanationsByStatus(
            @PathVariable ViolationExplanation.ExplanationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting explanations by status: {} - page: {}, size: {}", status, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ViolationExplanationDto> explanations = explanationService.getExplanationsByStatus(status, pageable);
        return ResponseEntity.ok(explanations);
    }
    
    /**
     * Approve an explanation
     * PATCH /api/hr/explanations/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> approveExplanation(
            @PathVariable Long id,
            @RequestBody(required = false) String reviewNotes) {
        
        log.info("Approving explanation: {} with notes: {}", id, reviewNotes);
        
        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            ViolationExplanationDto explanation = explanationService.approveExplanation(id, currentUserId, reviewNotes);
            return ResponseEntity.ok(explanation);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot approve explanation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error approving explanation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Reject an explanation
     * PATCH /api/hr/explanations/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> rejectExplanation(
            @PathVariable Long id,
            @RequestBody String reviewNotes) {
        
        log.info("Rejecting explanation: {} with notes: {}", id, reviewNotes);
        
        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            ViolationExplanationDto explanation = explanationService.rejectExplanation(id, currentUserId, reviewNotes);
            return ResponseEntity.ok(explanation);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot reject explanation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error rejecting explanation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Request more information for an explanation
     * PATCH /api/hr/explanations/{id}/request-more-info
     */
    @PatchMapping("/{id}/request-more-info")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> requestMoreInfo(
            @PathVariable Long id,
            @RequestBody String reviewNotes) {
        
        log.info("Requesting more info for explanation: {} with notes: {}", id, reviewNotes);
        
        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            ViolationExplanationDto explanation = explanationService.requestMoreInfo(id, currentUserId, reviewNotes);
            return ResponseEntity.ok(explanation);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot request more info for explanation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error requesting more info for explanation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete an explanation
     * DELETE /api/hr/explanations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExplanation(@PathVariable Long id) {
        log.info("Deleting explanation: {}", id);
        
        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            explanationService.deleteExplanation(id, currentUserId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete explanation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot delete explanation in current state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting explanation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get overdue explanations
     * GET /api/hr/explanations/overdue
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ViolationExplanationDto>> getOverdueExplanations(
            @RequestParam(defaultValue = "2") int daysSince) {

        log.info("Getting overdue explanations - days since: {}", daysSince);

        List<ViolationExplanationDto> explanations = explanationService.getOverdueExplanations(daysSince);
        return ResponseEntity.ok(explanations);
    }

    /**
     * Get explanation statistics by status
     * GET /api/hr/explanations/statistics/by-status
     */
    @GetMapping("/statistics/by-status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getExplanationStatisticsByStatus(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Getting explanation statistics by status from {} to {}", startDate, endDate);

        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            Object statistics = explanationService.getExplanationStatisticsByStatus(start, end);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error parsing date parameters", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check if user can submit explanation for violation
     * GET /api/hr/explanations/can-submit/{violationId}
     */
    @GetMapping("/can-submit/{violationId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> canSubmitExplanation(@PathVariable Long violationId) {
        log.info("Checking if user can submit explanation for violation: {}", violationId);

        // TODO: Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();

        boolean canSubmit = explanationService.canSubmitExplanation(violationId, currentUserId);
        return ResponseEntity.ok(canSubmit);
    }

    /**
     * Get latest explanation for violation
     * GET /api/hr/explanations/latest/{violationId}
     */
    @GetMapping("/latest/{violationId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ViolationExplanationDto> getLatestExplanationForViolation(@PathVariable Long violationId) {
        log.info("Getting latest explanation for violation: {}", violationId);

        ViolationExplanationDto explanation = explanationService.getLatestExplanationForViolation(violationId);
        if (explanation != null) {
            return ResponseEntity.ok(explanation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
