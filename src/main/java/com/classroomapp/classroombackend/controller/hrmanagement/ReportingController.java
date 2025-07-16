package com.classroomapp.classroombackend.controller.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.GeneratedReport;
import com.classroomapp.classroombackend.model.hrmanagement.ReportTemplate;
import com.classroomapp.classroombackend.service.hrmanagement.ReportingService;
import com.classroomapp.classroombackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for reporting operations
 */
@RestController
@RequestMapping("/api/hr/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportingController {
    
    private final ReportingService reportingService;
    private final SecurityUtils securityUtils;
    
    // Template management endpoints
    
    /**
     * Create new report template
     * POST /api/hr/reports/templates
     */
    @PostMapping("/templates")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ReportTemplate> createTemplate(@RequestBody ReportTemplate template) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Creating report template: {} by user {}", template.getTemplateName(), currentUserId);
        
        try {
            ReportTemplate createdTemplate = reportingService.createTemplate(template, currentUserId);
            return ResponseEntity.ok(createdTemplate);
            
        } catch (Exception e) {
            log.error("Error creating report template", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update existing template
     * PUT /api/hr/reports/templates/{templateId}
     */
    @PutMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ReportTemplate> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody ReportTemplate template) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Updating report template: {} by user {}", templateId, currentUserId);
        
        try {
            ReportTemplate updatedTemplate = reportingService.updateTemplate(templateId, template, currentUserId);
            return ResponseEntity.ok(updatedTemplate);
            
        } catch (Exception e) {
            log.error("Error updating report template: {}", templateId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete template
     * DELETE /api/hr/reports/templates/{templateId}
     */
    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Deleting report template: {} by user {}", templateId, currentUserId);
        
        try {
            reportingService.deleteTemplate(templateId, currentUserId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error deleting report template: {}", templateId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get template by ID
     * GET /api/hr/reports/templates/{templateId}
     */
    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ReportTemplate> getTemplateById(@PathVariable Long templateId) {
        
        log.info("Getting report template: {}", templateId);
        
        try {
            ReportTemplate template = reportingService.getTemplateById(templateId);
            return ResponseEntity.ok(template);
            
        } catch (Exception e) {
            log.error("Error getting report template: {}", templateId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all active templates
     * GET /api/hr/reports/templates
     */
    @GetMapping("/templates")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ReportTemplate>> getActiveTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Getting active report templates");
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ReportTemplate> templates = reportingService.getActiveTemplates(pageable);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            log.error("Error getting active report templates", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get accessible templates for current user
     * GET /api/hr/reports/templates/accessible
     */
    @GetMapping("/templates/accessible")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ReportTemplate>> getAccessibleTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        String currentUserRole = securityUtils.getCurrentUserRole();
        log.info("Getting accessible report templates for user: {}", currentUserId);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ReportTemplate> templates = reportingService.getAccessibleTemplates(
                currentUserId, currentUserRole, pageable);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            log.error("Error getting accessible report templates for user: {}", currentUserId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get templates by category
     * GET /api/hr/reports/templates/category/{category}
     */
    @GetMapping("/templates/category/{category}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ReportTemplate>> getTemplatesByCategory(
            @PathVariable ReportTemplate.ReportCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Getting report templates by category: {}", category);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ReportTemplate> templates = reportingService.getTemplatesByCategory(category, pageable);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            log.error("Error getting report templates by category: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search templates
     * GET /api/hr/reports/templates/search
     */
    @GetMapping("/templates/search")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ReportTemplate>> searchTemplates(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "templateName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Searching report templates with term: {}", searchTerm);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ReportTemplate> templates = reportingService.searchTemplates(searchTerm, pageable);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            log.error("Error searching report templates with term: {}", searchTerm, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Clone template for customization
     * POST /api/hr/reports/templates/{templateId}/clone
     */
    @PostMapping("/templates/{templateId}/clone")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ReportTemplate> cloneTemplate(
            @PathVariable Long templateId,
            @RequestParam String newName,
            @RequestParam String newCode) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Cloning report template: {} to {} by user {}", templateId, newName, currentUserId);
        
        try {
            ReportTemplate clonedTemplate = reportingService.cloneTemplate(templateId, newName, newCode, currentUserId);
            return ResponseEntity.ok(clonedTemplate);
            
        } catch (Exception e) {
            log.error("Error cloning report template: {}", templateId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Report generation endpoints
    
    /**
     * Generate report from template
     * POST /api/hr/reports/generate
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GeneratedReport> generateReport(
            @RequestParam Long templateId,
            @RequestParam(defaultValue = "JSON") GeneratedReport.ReportFormat format,
            @RequestBody(required = false) Map<String, Object> parameters) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Generating report from template: {} by user {}", templateId, currentUserId);
        
        try {
            GeneratedReport report = reportingService.generateReport(templateId, parameters, format, currentUserId);
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            log.error("Error generating report from template: {}", templateId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generate report asynchronously
     * POST /api/hr/reports/generate-async
     */
    @PostMapping("/generate-async")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GeneratedReport> generateReportAsync(
            @RequestParam Long templateId,
            @RequestParam(defaultValue = "JSON") GeneratedReport.ReportFormat format,
            @RequestBody(required = false) Map<String, Object> parameters) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Generating report asynchronously from template: {} by user {}", templateId, currentUserId);
        
        try {
            GeneratedReport report = reportingService.generateReportAsync(templateId, parameters, format, currentUserId);
            return ResponseEntity.accepted().body(report);
            
        } catch (Exception e) {
            log.error("Error generating report asynchronously from template: {}", templateId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get generated report by ID
     * GET /api/hr/reports/{reportId}
     */
    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GeneratedReport> getGeneratedReportById(@PathVariable Long reportId) {
        
        log.info("Getting generated report: {}", reportId);
        
        try {
            GeneratedReport report = reportingService.getGeneratedReportById(reportId);
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            log.error("Error getting generated report: {}", reportId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get reports by current user
     * GET /api/hr/reports/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<GeneratedReport>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting reports for current user: {}", currentUserId);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<GeneratedReport> reports = reportingService.getReportsByUser(currentUserId, pageable);
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            log.error("Error getting reports for user: {}", currentUserId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get completed reports
     * GET /api/hr/reports/completed
     */
    @GetMapping("/completed")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<GeneratedReport>> getCompletedReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "completedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting completed reports");
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<GeneratedReport> reports = reportingService.getCompletedReports(pageable);
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            log.error("Error getting completed reports", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cancel report generation
     * DELETE /api/hr/reports/{reportId}/cancel
     */
    @DeleteMapping("/{reportId}/cancel")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelReportGeneration(@PathVariable Long reportId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Cancelling report generation: {} by user {}", reportId, currentUserId);
        
        try {
            reportingService.cancelReportGeneration(reportId, currentUserId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error cancelling report generation: {}", reportId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete generated report
     * DELETE /api/hr/reports/{reportId}
     */
    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGeneratedReport(@PathVariable Long reportId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Deleting generated report: {} by user {}", reportId, currentUserId);
        
        try {
            reportingService.deleteGeneratedReport(reportId, currentUserId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error deleting generated report: {}", reportId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get report data
     * GET /api/hr/reports/{reportId}/data
     */
    @GetMapping("/{reportId}/data")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ReportingService.ReportData> getReportData(@PathVariable Long reportId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Getting report data: {} by user {}", reportId, currentUserId);
        
        try {
            ReportingService.ReportData reportData = reportingService.getReportData(reportId, currentUserId);
            return ResponseEntity.ok(reportData);
            
        } catch (Exception e) {
            log.error("Error getting report data: {}", reportId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generate download URL for report
     * GET /api/hr/reports/{reportId}/download-url
     */
    @GetMapping("/{reportId}/download-url")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> generateDownloadUrl(@PathVariable Long reportId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Generating download URL for report: {} by user {}", reportId, currentUserId);
        
        try {
            String downloadUrl = reportingService.generateDownloadUrl(reportId, currentUserId);
            return ResponseEntity.ok(downloadUrl);
            
        } catch (Exception e) {
            log.error("Error generating download URL for report: {}", reportId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Download report file
     * GET /api/hr/reports/{reportId}/download
     */
    @GetMapping("/{reportId}/download")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadReportFile(@PathVariable Long reportId) {
        
        Long currentUserId = securityUtils.getCurrentUserIdOrDefault();
        log.info("Downloading report file: {} by user {}", reportId, currentUserId);
        
        try {
            GeneratedReport report = reportingService.getGeneratedReportById(reportId);
            byte[] fileContent = reportingService.downloadReportFile(reportId, currentUserId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(report.getFormat().getMimeType()));
            headers.setContentDispositionFormData("attachment", report.getSuggestedFilename());
            headers.setContentLength(fileContent.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
            
        } catch (Exception e) {
            log.error("Error downloading report file: {}", reportId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
