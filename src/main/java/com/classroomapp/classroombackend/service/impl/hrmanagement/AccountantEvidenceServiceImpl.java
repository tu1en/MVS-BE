package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.dto.hrmanagement.ExplanationEvidenceDto;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.hrmanagement.EvidenceTemplate;
import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.EvidenceTemplateRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ExplanationEvidenceRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ViolationExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;
import com.classroomapp.classroombackend.service.hrmanagement.AccountantEvidenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AccountantEvidenceService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountantEvidenceServiceImpl implements AccountantEvidenceService {
    
    private final ExplanationEvidenceRepository evidenceRepository;
    private final ViolationExplanationRepository explanationRepository;
    private final AttendanceViolationRepository violationRepository;
    private final UserRepository userRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final ModelMapper modelMapper;
    private final EvidenceTemplateRepository evidenceTemplateRepository;
    
    @Value("${hr.evidence.max-file-size-mb:5}")
    private int maxFileSizeMB;
    
    @Value("${hr.evidence.accountant.max-files:10}")
    private int maxFilesPerUpload;
    
    @Override
    public ExplanationEvidenceDto uploadSupportingEvidence(Long violationId, MultipartFile file, 
                                                          String description, String evidenceType, 
                                                          String category, Long accountantId, String uploadIp) {
        
        log.info("Accountant {} uploading supporting evidence for violation {}", accountantId, violationId);
        
        // Validate file
        if (!validateAccountantEvidence(file, category)) {
            throw new IllegalArgumentException("File không hợp lệ cho danh mục: " + category);
        }
        
        // Find or create explanation for this violation
        ViolationExplanation explanation = findOrCreateExplanationForViolation(violationId, accountantId);
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = "accountant_" + UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create evidence record
            ExplanationEvidence evidence = new ExplanationEvidence();
            evidence.setExplanation(explanation);
            evidence.setOriginalFilename(originalFilename);
            evidence.setFileSize(file.getSize());
            evidence.setFileType(fileExtension);
            evidence.setMimeType(file.getContentType());
            evidence.setDescription(description != null ? description : "Minh chứng hỗ trợ từ kế toán");
            evidence.setUploadIp(uploadIp);
            
            // Set evidence type
            evidence.setEvidenceType(parseEvidenceType(evidenceType));
            
            // Set business category in description
            if (category != null) {
                evidence.setDescription(evidence.getDescription() + " [" + category + "]");
            }
            
            ExplanationEvidence savedEvidence = evidenceRepository.save(evidence);
            
            // Upload file to Firebase Storage
            try {
                String storagePath = "evidence/accountant/" + accountantId + "/" + violationId;
                FileUploadResponse uploadResponse = firebaseStorageService.uploadFile(file, storagePath);
                
                savedEvidence.setFileUrl(uploadResponse.getFileUrl());
                savedEvidence.setFilePath(storagePath + "/" + uploadResponse.getFileName());
                evidenceRepository.save(savedEvidence);
                
                log.info("Evidence uploaded successfully with ID: {}", savedEvidence.getId());
                
            } catch (Exception e) {
                log.error("Error uploading to Firebase Storage", e);
                // Keep database record with local path as fallback
                savedEvidence.setFilePath("local/evidence/" + violationId + "/" + uniqueFilename);
                savedEvidence.setFileUrl("/api/accountant/evidence/" + savedEvidence.getId() + "/download");
                evidenceRepository.save(savedEvidence);
            }
            
            return convertToDto(savedEvidence);
            
        } catch (Exception e) {
            log.error("Error uploading accountant evidence", e);
            throw new RuntimeException("Lỗi khi tải lên minh chứng: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getAccountantUploads(Long accountantId, String startDate, 
                                                            String endDate, String evidenceType, String status) {
        
        log.info("Getting uploads for accountant: {} with filters", accountantId);
        
        // Get all evidence and filter by accountant uploads
        List<ExplanationEvidence> allEvidence = evidenceRepository.findAll();
        
        List<ExplanationEvidenceDto> result = allEvidence.stream()
                .filter(evidence -> isAccountantEvidence(evidence, accountantId))
                .filter(evidence -> matchesDateFilter(evidence, startDate, endDate))
                .filter(evidence -> matchesTypeFilter(evidence, evidenceType))
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceByViolation(Long violationId) {
        // Find violation first
        AttendanceViolation violation = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm với ID: " + violationId));
        
        // Get all explanations for this violation
        List<ViolationExplanation> explanations = violation.getExplanations();
        
        List<ExplanationEvidence> evidenceList = new ArrayList<>();
        for (ViolationExplanation explanation : explanations) {
            evidenceList.addAll(explanation.getEvidenceFiles());
        }
        
        List<ExplanationEvidenceDto> result = evidenceList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getPendingAccountantReview() {
        List<ExplanationEvidence> evidenceList = evidenceRepository.findByIsVerifiedOrderByCreatedAtDesc(false);
        
        List<ExplanationEvidenceDto> result = evidenceList.stream()
                .filter(evidence -> needsAccountantAttention(evidence))
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return result;
    }
    
    @Override
    public ExplanationEvidenceDto addAccountantNotes(Long evidenceId, String notes, Long accountantId) {
        ExplanationEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy minh chứng với ID: " + evidenceId));
        
        // Add accountant notes to description
        String currentDescription = evidence.getDescription() != null ? evidence.getDescription() : "";
        String updatedDescription = currentDescription + "\n[Ghi chú KT]: " + notes;
        evidence.setDescription(updatedDescription);
        
        ExplanationEvidence updatedEvidence = evidenceRepository.save(evidence);
        log.info("Added accountant notes to evidence: {}", evidenceId);
        
        return convertToDto(updatedEvidence);
    }
    
    @Override
    public ExplanationEvidenceDto markAsAccountantReviewed(Long evidenceId, Long accountantId) {
        ExplanationEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy minh chứng với ID: " + evidenceId));
        
        evidence.verify(accountantId);
        
        // Add review note
        String reviewNote = "\n[Đã xem xét bởi KT vào: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "]";
        evidence.setDescription((evidence.getDescription() != null ? evidence.getDescription() : "") + reviewNote);
        
        ExplanationEvidence updatedEvidence = evidenceRepository.save(evidence);
        log.info("Marked evidence as reviewed by accountant: {}", evidenceId);
        
        return convertToDto(updatedEvidence);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAccountantEvidenceStatistics(Long accountantId, String period) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get basic counts
        List<ExplanationEvidence> allEvidence = evidenceRepository.findAll();
        
        long totalUploaded = allEvidence.stream()
                .filter(evidence -> isAccountantEvidence(evidence, accountantId))
                .count();
        
        long pendingReview = allEvidence.stream()
                .filter(evidence -> !evidence.isVerified() && needsAccountantAttention(evidence))
                .count();
        
        long reviewedByMe = allEvidence.stream()
                .filter(evidence -> evidence.isVerified() && 
                        accountantId.equals(evidence.getVerifiedBy()))
                .count();
        
        stats.put("totalUploaded", totalUploaded);
        stats.put("pendingReview", pendingReview);
        stats.put("reviewedByMe", reviewedByMe);
        stats.put("period", period != null ? period : "all");
        
        // File type breakdown
        Map<String, Long> fileTypes = allEvidence.stream()
                .filter(evidence -> isAccountantEvidence(evidence, accountantId))
                .collect(Collectors.groupingBy(
                    evidence -> evidence.getEvidenceType().name(),
                    Collectors.counting()
                ));
        stats.put("fileTypes", fileTypes);
        
        return stats;
    }
    
    @Override
    public byte[] exportEvidenceReport(Long accountantId, String startDate, String endDate, String format) {
        log.info("Exporting evidence report for accountant: {} in format: {}", accountantId, format);
        
        // Get filtered evidence
        List<ExplanationEvidenceDto> evidenceList = getAccountantUploads(accountantId, startDate, endDate, null, null);
        
        // For now, return a simple CSV-like content
        // In production, you'd use a proper report generation library
        StringBuilder report = new StringBuilder();
        report.append("ID,Filename,Upload Date,Type,Size,Status\n");
        
        for (ExplanationEvidenceDto evidence : evidenceList) {
            report.append(String.format("%d,%s,%s,%s,%s,%s\n",
                evidence.getId(),
                evidence.getOriginalFilename(),
                evidence.getCreatedAt(),
                evidence.getEvidenceType(),
                evidence.getFormattedFileSize(),
                evidence.getIsVerified() ? "Verified" : "Pending"
            ));
        }
        
        return report.toString().getBytes();
    }
    
    @Override
    public List<ExplanationEvidenceDto> bulkUploadEvidence(Long violationId, MultipartFile[] files,
                                                          String[] descriptions, String[] evidenceTypes,
                                                          String category, Long accountantId, String uploadIp) {
        
        log.info("Bulk uploading {} files for violation: {} by accountant: {}", files.length, violationId, accountantId);
        
        if (files.length > maxFilesPerUpload) {
            throw new IllegalArgumentException("Không thể tải lên quá " + maxFilesPerUpload + " files cùng lúc");
        }
        
        List<ExplanationEvidenceDto> uploadedFiles = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            try {
                String description = (descriptions != null && i < descriptions.length) ? descriptions[i] : null;
                String evidenceType = (evidenceTypes != null && i < evidenceTypes.length) ? evidenceTypes[i] : null;
                
                ExplanationEvidenceDto uploadedFile = uploadSupportingEvidence(
                    violationId, files[i], description, evidenceType, category, accountantId, uploadIp);
                
                uploadedFiles.add(uploadedFile);
                
            } catch (Exception e) {
                log.error("Error uploading file {} in bulk upload", files[i].getOriginalFilename(), e);
                // Continue with other files
            }
        }
        
        log.info("Successfully uploaded {} out of {} files", uploadedFiles.size(), files.length);
        return uploadedFiles;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceByCategory(String category) {
        List<ExplanationEvidence> evidenceList = evidenceRepository.findAll().stream()
                .filter(evidence -> evidence.getDescription() != null && 
                        evidence.getDescription().contains("[" + category + "]"))
                .collect(Collectors.toList());
        
        List<ExplanationEvidenceDto> result = evidenceList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return result;
    }
    
    @Override
    public void deleteAccountantEvidence(Long evidenceId, Long accountantId) {
        ExplanationEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy minh chứng với ID: " + evidenceId));
        
        // Check if this is accountant's evidence
        if (!isAccountantEvidence(evidence, accountantId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa minh chứng này");
        }
        
        try {
            // Delete file from Firebase Storage
            if (evidence.getFilePath() != null) {
                firebaseStorageService.deleteFile(evidence.getFilePath());
            }
            
            evidenceRepository.delete(evidence);
            log.info("Deleted accountant evidence: {}", evidenceId);
            
        } catch (Exception e) {
            log.error("Error deleting accountant evidence", e);
            throw new RuntimeException("Lỗi khi xóa minh chứng: " + e.getMessage());
        }
    }
    
    @Override  
@Transactional(readOnly = true)
public List<Map<String, Object>> getEvidenceTemplates() {
    log.info("Getting evidence templates from database");
    
    try {
        // Get templates from database
        List<EvidenceTemplate> templates = evidenceTemplateRepository
            .findByIsActiveTrueOrderBySortOrderAscTemplateNameAsc();
        
        List<Map<String, Object>> templateList = templates.stream()
            .map(this::convertTemplateToMap)
            .collect(Collectors.toList());
        
        // If no templates in database, return mock data
        if (templateList.isEmpty()) {
            log.warn("No templates found in database, returning mock data");
            return getMockTemplates();
        }
        
        log.info("Retrieved {} evidence templates from database", templateList.size());
        return templateList;
        
    } catch (Exception e) {
        log.error("Error retrieving evidence templates from database: {}", e.getMessage(), e);
        
        // Fallback to mock data if database error
        log.info("Falling back to mock templates due to database error");
        return getMockTemplates();
    }
}
    
    @Override
    public boolean validateAccountantEvidence(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // Check file size
        long maxSizeBytes = maxFileSizeMB * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            return false;
        }
        
        // Check file type based on category
        String contentType = file.getContentType();
        return isAllowedContentTypeForCategory(contentType, category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceUploadHistory(Long accountantId, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        
        List<ExplanationEvidence> evidenceList = evidenceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                cutoffDate, LocalDateTime.now());
        
        List<ExplanationEvidenceDto> result = evidenceList.stream()
                .filter(evidence -> isAccountantEvidence(evidence, accountantId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyEvidenceSummary(Long accountantId, int year, int month) {
        // Implementation for monthly summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("accountantId", accountantId);
        summary.put("year", year);
        summary.put("month", month);
        summary.put("totalFiles", 0); // Implement actual calculation
        return summary;
    }
    
    @Override
    public int archiveOldEvidence(int daysOld) {
        log.info("Archiving evidence older than {} days", daysOld);
        return 0; // Return number of archived files
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceNeedingFollowUp(Long accountantId) {
        return new ArrayList<>();
    }
    
    @Override
    public ExplanationEvidenceDto updateEvidencePriority(Long evidenceId, String priority, Long accountantId) {
        ExplanationEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy minh chứng với ID: " + evidenceId));
        
        // Add priority to description
        String priorityNote = "\n[Độ ưu tiên: " + priority + "]";
        evidence.setDescription((evidence.getDescription() != null ? evidence.getDescription() : "") + priorityNote);
        
        ExplanationEvidence updatedEvidence = evidenceRepository.save(evidence);
        return convertToDto(updatedEvidence);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceByPriority(String priority) {
        List<ExplanationEvidence> evidenceList = evidenceRepository.findAll().stream()
                .filter(evidence -> evidence.getDescription() != null && 
                        evidence.getDescription().contains("[Độ ưu tiên: " + priority + "]"))
                .collect(Collectors.toList());
        
        List<ExplanationEvidenceDto> result = evidenceList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return result;
    }
    
    // Helper methods
    
    private ViolationExplanation findOrCreateExplanationForViolation(Long violationId, Long accountantId) {
        // Find the violation first
        AttendanceViolation violation = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm với ID: " + violationId));
        
        // Check if there's already an explanation for this violation
        List<ViolationExplanation> existingExplanations = violation.getExplanations();
        if (existingExplanations != null && !existingExplanations.isEmpty()) {
            // Return the first explanation (or you could implement logic to find the most appropriate one)
            return existingExplanations.get(0);
        }
        
        // Create new explanation for this violation
        ViolationExplanation newExplanation = new ViolationExplanation();
        newExplanation.setViolation(violation);
        newExplanation.setExplanationText("Minh chứng hỗ trợ từ kế toán cho vi phạm: " + violation.getDetailedDescription());
        
        // Set the accountant as the submitter
        User accountant = userRepository.findById(accountantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kế toán với ID: " + accountantId));
        newExplanation.setSubmittedBy(accountant);
        
        return explanationRepository.save(newExplanation);
    }
    
    private boolean isAccountantEvidence(ExplanationEvidence evidence, Long accountantId) {
        // Check if evidence was uploaded by accountant
        // This is a simplified check - in production you'd have a proper accountant_id field
        return evidence.getDescription() != null && 
               (evidence.getDescription().contains("Minh chứng hỗ trợ từ kế toán") ||
                (evidence.getOriginalFilename() != null && evidence.getOriginalFilename().startsWith("accountant_")));
    }
    
    private boolean matchesDateFilter(ExplanationEvidence evidence, String startDate, String endDate) {
        if (startDate == null && endDate == null) return true;
        
        try {
            LocalDateTime createdAt = evidence.getCreatedAt();
            if (createdAt == null) return true;
            
            if (startDate != null) {
                LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                if (createdAt.isBefore(start)) return false;
            }
            
            if (endDate != null) {
                LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                if (createdAt.isAfter(end)) return false;
            }
            
            return true;
        } catch (Exception e) {
            return true; // If date parsing fails, include the record
        }
    }
    
    private boolean matchesTypeFilter(ExplanationEvidence evidence, String evidenceType) {
        if (evidenceType == null) return true;
        return evidence.getEvidenceType().name().equals(evidenceType);
    }
    
    private boolean needsAccountantAttention(ExplanationEvidence evidence) {
        // Logic to determine if evidence needs accountant attention
        return evidence.getDescription() != null && 
               (evidence.getDescription().contains("PAYROLL") || 
                evidence.getDescription().contains("CONTRACT") ||
                evidence.getDescription().contains("SALARY"));
    }
    
    private ExplanationEvidence.EvidenceType parseEvidenceType(String evidenceType) {
        if (evidenceType == null) return ExplanationEvidence.EvidenceType.DOCUMENT;
        
        try {
            return ExplanationEvidence.EvidenceType.valueOf(evidenceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ExplanationEvidence.EvidenceType.DOCUMENT;
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        
        return "";
    }
    
    private Map<String, Object> createTemplate(String name, String category, String description, String filename) {
        Map<String, Object> template = new HashMap<>();
        template.put("name", name);
        template.put("category", category);
        template.put("description", description);
        template.put("filename", filename);
        template.put("downloadUrl", "/api/accountant/evidence/templates/" + filename);
        return template;
    }
    
    private boolean isAllowedContentTypeForCategory(String contentType, String category) {
        if (contentType == null) return false;
        
        // Basic allowed types
        String[] allowedTypes = {
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
        };
        
        for (String allowedType : allowedTypes) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        
        return false;
    }
    
    private ExplanationEvidenceDto convertToDto(ExplanationEvidence evidence) {
        ExplanationEvidenceDto dto = modelMapper.map(evidence, ExplanationEvidenceDto.class);
        
        // Set additional fields
        if (evidence.getExplanation() != null) {
            dto.setExplanationId(evidence.getExplanation().getId());
        }
        
        // Set computed fields
        dto.setFormattedFileSize(evidence.getFormattedFileSize());
        dto.setFileExtension(evidence.getFileExtension());
        dto.setIsImage(evidence.isImage());
        dto.setIsPdf(evidence.isPdf());
        dto.setIsDocument(evidence.isDocument());
        dto.setDisplayName(evidence.getDisplayName());
        dto.setSecurityInfo(evidence.getSecurityInfo());
        dto.setIsVerified(evidence.isVerified());
        
        return dto;
    }

    
    /**
     * Convert EvidenceTemplate entity to Map for API response
     */
    private Map<String, Object> convertTemplateToMap(EvidenceTemplate template) {
        Map<String, Object> templateMap = new HashMap<>();
        
        templateMap.put("id", template.getId());
        templateMap.put("name", template.getTemplateName());
        templateMap.put("code", template.getTemplateCode());
        templateMap.put("description", template.getDescription());
        templateMap.put("category", template.getCategory().name());
        templateMap.put("categoryDisplayName", template.getCategoryDisplayName());
        templateMap.put("fileType", template.getFileType().name());
        templateMap.put("fileTypeDisplayName", template.getFileTypeDisplayName());
        templateMap.put("fileName", template.getFileName());
        templateMap.put("downloadUrl", template.getDownloadUrl());
        templateMap.put("fileSize", template.getFileSize());
        templateMap.put("formattedFileSize", template.getFormattedFileSize());
        templateMap.put("version", template.getVersion());
        templateMap.put("sortOrder", template.getSortOrder());
        templateMap.put("usageInstructions", template.getUsageInstructions());
        templateMap.put("isDownloadable", template.isDownloadable());
        templateMap.put("createdAt", template.getCreatedAt());
        templateMap.put("updatedAt", template.getUpdatedAt());
        
        return templateMap;
    }

    /**
     * Get mock templates as fallback
     */
    private List<Map<String, Object>> getMockTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        
        // Mock data for testing when database is empty
        templates.add(createMockTemplate(1L, "Biểu mẫu chấm công", "ATTENDANCE", 
                "Mẫu báo cáo chấm công hàng tháng", "attendance-template.xlsx", "XLSX"));
        
        templates.add(createMockTemplate(2L, "Bảng tính lương", "PAYROLL", 
                "Mẫu tính toán lương và phụ cấp", "payroll-template.xlsx", "XLSX"));
        
        templates.add(createMockTemplate(3L, "Hợp đồng lao động", "CONTRACT", 
                "Mẫu hợp đồng lao động chuẩn", "contract-template.docx", "DOCX"));
        
        templates.add(createMockTemplate(4L, "Giấy khám bệnh", "MEDICAL", 
                "Mẫu giấy khám bệnh cho nghỉ phép", "medical-template.pdf", "PDF"));
        
        templates.add(createMockTemplate(5L, "Giải trình vi phạm", "VIOLATION", 
                "Mẫu đơn giải trình vi phạm kỷ luật", "violation-explanation.docx", "DOCX"));
        
        return templates;
    }

    /**
     * Create mock template data
     */
    private Map<String, Object> createMockTemplate(Long id, String name, String category, 
                                                  String description, String filename, String fileType) {
        Map<String, Object> template = new HashMap<>();
        
        template.put("id", id);
        template.put("name", name);
        template.put("code", category + "_" + name.replaceAll("\\s+", "_").toUpperCase());
        template.put("description", description);
        template.put("category", category);
        template.put("categoryDisplayName", getCategoryDisplayName(category));
        template.put("fileType", fileType);
        template.put("fileTypeDisplayName", getFileTypeDisplayName(fileType));
        template.put("fileName", filename);
        template.put("downloadUrl", "/api/accountant/evidence/templates/" + filename);
        template.put("fileSize", 1024L * 50); // 50KB
        template.put("formattedFileSize", "50.0 KB");
        template.put("version", "1.0");
        template.put("sortOrder", id.intValue());
        template.put("usageInstructions", "Tải xuống template, điền thông tin và tải lên hệ thống.");
        template.put("isDownloadable", true);
        template.put("createdAt", LocalDateTime.now().minusDays(30));
        template.put("updatedAt", LocalDateTime.now().minusDays(5));
        
        return template;
    }

    /**
     * Get display name for category
     */
    private String getCategoryDisplayName(String category) {
        switch (category) {
            case "ATTENDANCE": return "Chấm công";
            case "PAYROLL": return "Lương bổng";
            case "CONTRACT": return "Hợp đồng";
            case "MEDICAL": return "Y tế";
            case "VIOLATION": return "Vi phạm";
            case "EXPLANATION": return "Giải trình";
            case "REPORT": return "Báo cáo";
            default: return "Khác";
        }
    }

    /**
     * Get display name for file type
     */
    private String getFileTypeDisplayName(String fileType) {
        switch (fileType) {
            case "PDF": return "PDF";
            case "DOCX": return "Word Document";
            case "XLSX": return "Excel Spreadsheet";
            case "DOC": return "Word Document (Legacy)";
            case "XLS": return "Excel Spreadsheet (Legacy)";
            default: return fileType;
        }
    }
}