package com.classroomapp.classroombackend.controller.hrmanagement;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.hrmanagement.ExplanationEvidenceDto;
import com.classroomapp.classroombackend.service.hrmanagement.AccountantEvidenceService;
import com.classroomapp.classroombackend.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Accountant Evidence management
 * Specialized controller for accountant role with additional features
 */
@RestController
@RequestMapping("/api/accountant/evidence")
@CrossOrigin(
    origins = {"http://localhost:3000", "http://localhost:3001"},
    allowedHeaders = "*",
    methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS },
    allowCredentials = "true",
    maxAge = 3600
)
@RequiredArgsConstructor
@Slf4j
public class AccountantEvidenceController {

    private final AccountantEvidenceService accountantEvidenceService;
    private final SecurityUtils securityUtils;
    
    /**
     * Upload supporting evidence for attendance violations (Accountant specific)
     * POST /api/accountant/evidence/upload-supporting
     */
    @PostMapping(value = "/upload-supporting", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ExplanationEvidenceDto> uploadSupportingEvidence(
            @RequestParam Long violationId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String evidenceType,
            @RequestParam(required = false) String category, // NEW: Business category
            HttpServletRequest request) {
        
        log.info("Accountant uploading supporting evidence for violation: {}", violationId);
        
        try {
            // Get current accountant ID
            Long accountantId = securityUtils.getCurrentUserIdOrDefault();
            String uploadIp = getClientIpAddress(request);
            
            ExplanationEvidenceDto evidence = accountantEvidenceService.uploadSupportingEvidence(
                violationId, file, description, evidenceType, category, accountantId, uploadIp);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid supporting evidence upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading supporting evidence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get my uploaded evidence files (Accountant only)
     * GET /api/accountant/evidence/my-uploads
     */
    @GetMapping("/my-uploads")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getMyUploads(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String evidenceType,
            @RequestParam(required = false) String status) {
        
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting uploaded evidence files for accountant: {}", accountantId);
        
        List<ExplanationEvidenceDto> evidenceList = accountantEvidenceService.getAccountantUploads(
            accountantId, startDate, endDate, evidenceType, status);
        
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Get evidence files by violation ID (Accountant view)
     * GET /api/accountant/evidence/violation/{violationId}
     */
    @GetMapping("/violation/{violationId}")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getEvidenceByViolation(@PathVariable Long violationId) {
        log.info("Getting evidence files for violation: {}", violationId);
        
        List<ExplanationEvidenceDto> evidenceList = accountantEvidenceService.getEvidenceByViolation(violationId);
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Get pending evidence files that need accountant attention
     * GET /api/accountant/evidence/pending-review
     */
    @GetMapping("/pending-review")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getPendingReview() {
        log.info("Getting evidence files pending accountant review");
        
        List<ExplanationEvidenceDto> evidenceList = accountantEvidenceService.getPendingAccountantReview();
        return ResponseEntity.ok(evidenceList);
    }

    /**
     * Get evidence files reviewed by current accountant
     * GET /api/accountant/evidence/reviewed-by-me
     */
    @GetMapping("/reviewed-by-me")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getReviewedByMe() {
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting evidence reviewed by accountant: {}", accountantId);
        List<ExplanationEvidenceDto> evidenceList = accountantEvidenceService.getReviewedByMe(accountantId);
        return ResponseEntity.ok(evidenceList);
    }

    /**
     * Get all reviewed evidence (read-only overview)
     * GET /api/accountant/evidence/all-reviewed
     */
    @GetMapping("/all-reviewed")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getAllReviewed() {
        log.info("Getting all reviewed evidence for accountant overview");
        List<ExplanationEvidenceDto> evidenceList = accountantEvidenceService.getAllReviewed();
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Add accountant notes to evidence file
     * PATCH /api/accountant/evidence/{id}/add-notes
     */
    @PatchMapping("/{id}/add-notes")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ExplanationEvidenceDto> addAccountantNotes(
            @PathVariable Long id,
            @RequestParam String notes) {
        
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Adding accountant notes to evidence: {} by accountant: {}", id, accountantId);
        
        try {
            ExplanationEvidenceDto evidence = accountantEvidenceService.addAccountantNotes(id, notes, accountantId);
            return ResponseEntity.ok(evidence);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot add notes to evidence: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Mark evidence as reviewed by accountant
     * PATCH /api/accountant/evidence/{id}/mark-reviewed
     */
    @PatchMapping("/{id}/mark-reviewed")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ExplanationEvidenceDto> markAsReviewed(@PathVariable Long id) {
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Marking evidence as reviewed: {} by accountant: {}", id, accountantId);
        
        try {
            ExplanationEvidenceDto evidence = accountantEvidenceService.markAsAccountantReviewed(id, accountantId);
            return ResponseEntity.ok(evidence);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot mark evidence as reviewed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get evidence statistics for accountant dashboard
     * GET /api/accountant/evidence/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getEvidenceStatistics(
            @RequestParam(required = false) String period) {
        
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting evidence statistics for accountant: {}", accountantId);
        
        Map<String, Object> statistics = accountantEvidenceService.getAccountantEvidenceStatistics(accountantId, period);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Export evidence report for accounting purposes
     * GET /api/accountant/evidence/export-report
     */
    @GetMapping("/export-report")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<byte[]> exportEvidenceReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "pdf") String format) {
        
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Exporting evidence report for accountant: {} in format: {}", accountantId, format);
        
        try {
            byte[] reportData = accountantEvidenceService.exportEvidenceReport(
                accountantId, startDate, endDate, format);
            
            String filename = "evidence-report." + format;
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", format.equals("pdf") ? "application/pdf" : "application/vnd.ms-excel")
                .body(reportData);
                
        } catch (Exception e) {
            log.error("Error exporting evidence report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Bulk upload multiple evidence files
     * POST /api/accountant/evidence/bulk-upload
     */
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> bulkUploadEvidence(
            @RequestParam Long violationId,
            @RequestParam MultipartFile[] files,
            @RequestParam(required = false) String[] descriptions,
            @RequestParam(required = false) String[] evidenceTypes,
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        
        log.info("Bulk uploading {} evidence files for violation: {}", files.length, violationId);
        
        try {
            Long accountantId = securityUtils.getCurrentUserIdOrDefault();
            String uploadIp = getClientIpAddress(request);
            
            List<ExplanationEvidenceDto> uploadedFiles = accountantEvidenceService.bulkUploadEvidence(
                violationId, files, descriptions, evidenceTypes, category, accountantId, uploadIp);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFiles);
            
        } catch (Exception e) {
            log.error("Error in bulk upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get evidence files by category (Accounting specific)
     * GET /api/accountant/evidence/by-category/{category}
     */
    @GetMapping("/by-category/{category}")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<ExplanationEvidenceDto>> getEvidenceByCategory(@PathVariable String category) {
        log.info("Getting evidence files by category: {}", category);
        
        List<ExplanationEvidenceDto> evidenceList = accountantEvidenceService.getEvidenceByCategory(category);
        return ResponseEntity.ok(evidenceList);
    }
    
    /**
     * Delete my uploaded evidence (Accountant only)
     * DELETE /api/accountant/evidence/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<Void> deleteMyEvidence(@PathVariable Long id) {
        Long accountantId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Accountant {} deleting evidence: {}", accountantId, id);
        
        try {
            accountantEvidenceService.deleteAccountantEvidence(id, accountantId);
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
     * Get evidence file templates for accountants
     * GET /api/accountant/evidence/templates
     */
    @GetMapping("/templates")
    @PreAuthorize("hasRole('ACCOUNTANT')")  
    public ResponseEntity<List<Map<String, Object>>> getEvidenceTemplates() {
        log.info("Getting evidence templates for accountant");
        
        List<Map<String, Object>> templates = accountantEvidenceService.getEvidenceTemplates();
        return ResponseEntity.ok(templates);
    }
    
    // Helper method
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
}