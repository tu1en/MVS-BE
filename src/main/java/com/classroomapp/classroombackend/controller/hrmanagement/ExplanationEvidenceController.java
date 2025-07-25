package com.classroomapp.classroombackend.controller.hrmanagement;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.hrmanagement.ExplanationEvidenceDto;
import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import com.classroomapp.classroombackend.service.hrmanagement.ExplanationEvidenceService;
import com.classroomapp.classroombackend.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Explanation Evidence management
 */
@RestController
@RequestMapping("/api/hr/evidence")
@RequiredArgsConstructor
@Slf4j
public class ExplanationEvidenceController {

    private final ExplanationEvidenceService evidenceService;
    private final SecurityUtils securityUtils;
    
    /**
     * Upload single evidence file
     * POST /api/hr/evidence/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ExplanationEvidenceDto> uploadEvidence(
            @RequestParam Long explanationId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String evidenceType,
            HttpServletRequest request) {
        
        log.info("Uploading evidence file for explanation: {}", explanationId);
        
        try {
            // Get client IP
            String uploadIp = getClientIpAddress(request);
            
            // Validate file
            if (!evidenceService.validateFile(file)) {
                List<String> errors = evidenceService.getFileValidationErrors(file);
                log.warn("File validation failed: {}", errors);
                return ResponseEntity.badRequest().build();
            }
            
            ExplanationEvidenceDto evidence = evidenceService.uploadSingleEvidence(
                explanationId, file, description, evidenceType, uploadIp);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid evidence upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading evidence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get evidence by ID
     * GET /api/hr/evidence/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ExplanationEvidenceDto> getEvidenceById(@PathVariable Long id) {
        log.info("Getting evidence by ID: {}", id);
        
        try {
            // Check if current user can access this evidence
            Long currentUserId = securityUtils.getCurrentUserId();
            if (currentUserId != null && !evidenceService.canAccessEvidence(id, currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            ExplanationEvidenceDto evidence = evidenceService.getEvidenceById(id);
            return ResponseEntity.ok(evidence);
            
        } catch (IllegalArgumentException e) {
            log.warn("Evidence not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get evidence files by explanation
     * GET /api/hr/evidence/explanation/{explanationId}
     */
    @GetMapping("/explanation/{explanationId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getEvidenceByExplanation(@PathVariable Long explanationId) {
        log.info("Getting evidence files for explanation: {}", explanationId);
        
        List<ExplanationEvidenceDto> evidenceList = evidenceService.getEvidenceByExplanation(explanationId);
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Get evidence files by type
     * GET /api/hr/evidence/type/{evidenceType}
     */
    @GetMapping("/type/{evidenceType}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getEvidenceByType(
            @PathVariable ExplanationEvidence.EvidenceType evidenceType) {
        
        log.info("Getting evidence files by type: {}", evidenceType);
        
        List<ExplanationEvidenceDto> evidenceList = evidenceService.getEvidenceByType(evidenceType);
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Get unverified evidence files
     * GET /api/hr/evidence/unverified
     */
    @GetMapping("/unverified")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getUnverifiedEvidence() {
        log.info("Getting unverified evidence files");
        
        List<ExplanationEvidenceDto> evidenceList = evidenceService.getUnverifiedEvidence();
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Get evidence files by date range
     * GET /api/hr/evidence/date-range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getEvidenceByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        log.info("Getting evidence files by date range: {} to {}", startDate, endDate);
        
        try {
            List<ExplanationEvidenceDto> evidenceList = evidenceService.getEvidenceByDateRange(startDate, endDate);
            return ResponseEntity.ok(evidenceList);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Verify evidence file
     * PATCH /api/hr/evidence/{id}/verify
     */
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ExplanationEvidenceDto> verifyEvidence(@PathVariable Long id) {
        log.info("Verifying evidence: {}", id);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            ExplanationEvidenceDto evidence = evidenceService.verifyEvidence(id, currentUserId);
            return ResponseEntity.ok(evidence);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot verify evidence: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error verifying evidence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete evidence file
     * DELETE /api/hr/evidence/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id) {
        log.info("Deleting evidence: {}", id);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            evidenceService.deleteEvidence(id, currentUserId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete evidence: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting evidence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Generate download URL for evidence file
     * GET /api/hr/evidence/{id}/download-url
     */
    @GetMapping("/{id}/download-url")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> generateDownloadUrl(@PathVariable Long id) {
        log.info("Generating download URL for evidence: {}", id);
        
        // Get current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        
        try {
            String downloadUrl = evidenceService.generateDownloadUrl(id, currentUserId);
            return ResponseEntity.ok(downloadUrl);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot generate download URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error generating download URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get file statistics by type
     * GET /api/hr/evidence/statistics/by-type
     */
    @GetMapping("/statistics/by-type")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getFileStatisticsByType() {
        log.info("Getting file statistics by type");
        
        Object statistics = evidenceService.getFileStatisticsByType();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get total file size for explanation
     * GET /api/hr/evidence/explanation/{explanationId}/total-size
     */
    @GetMapping("/explanation/{explanationId}/total-size")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalFileSizeByExplanation(@PathVariable Long explanationId) {
        log.info("Getting total file size for explanation: {}", explanationId);
        
        Long totalSize = evidenceService.getTotalFileSizeByExplanation(explanationId);
        return ResponseEntity.ok(totalSize);
    }
    
    /**
     * Get monthly upload statistics
     * GET /api/hr/evidence/statistics/monthly
     */
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getMonthlyUploadStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        
        log.info("Getting monthly upload statistics for {}/{}", year, month);
        
        Object statistics = evidenceService.getMonthlyUploadStatistics(year, month);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get large files
     * GET /api/hr/evidence/large-files
     */
    @GetMapping("/large-files")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getLargeFiles(
            @RequestParam(defaultValue = "2") int minSizeMB) {
        
        log.info("Getting large files (min size: {}MB)", minSizeMB);
        
        List<ExplanationEvidenceDto> largeFiles = evidenceService.getLargeFiles(minSizeMB);
        return ResponseEntity.ok(largeFiles);
    }
    
    /**
     * Cleanup orphaned files
     * POST /api/hr/evidence/cleanup-orphaned
     */
    @PostMapping("/cleanup-orphaned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> cleanupOrphanedFiles() {
        log.info("Cleaning up orphaned files");
        
        try {
            int cleanedCount = evidenceService.cleanupOrphanedFiles();
            return ResponseEntity.ok(cleanedCount);
            
        } catch (Exception e) {
            log.error("Error cleaning up orphaned files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Validate file before upload
     * POST /api/hr/evidence/validate
     */
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Object> validateFile(@RequestParam MultipartFile file) {
        log.info("Validating file: {}", file.getOriginalFilename());
        
        boolean isValid = evidenceService.validateFile(file);
        List<String> errors = evidenceService.getFileValidationErrors(file);
        
        return ResponseEntity.ok(new FileValidationResult(isValid, errors));
    }
    
    // Helper methods
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    // Inner class for validation result
    public static class FileValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public FileValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}
