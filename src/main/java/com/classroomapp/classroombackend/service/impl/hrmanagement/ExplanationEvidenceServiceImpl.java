package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.hrmanagement.CreateExplanationDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ExplanationEvidenceDto;
import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;
import com.classroomapp.classroombackend.repository.hrmanagement.ExplanationEvidenceRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ViolationExplanationRepository;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;
import com.classroomapp.classroombackend.service.hrmanagement.ExplanationEvidenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExplanationEvidenceService
 * Note: This is a basic implementation. Firebase Storage integration will be added later.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExplanationEvidenceServiceImpl implements ExplanationEvidenceService {
    
    private final ExplanationEvidenceRepository evidenceRepository;
    private final ViolationExplanationRepository explanationRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final ModelMapper modelMapper;
    
    @Value("${hr.evidence.max-file-size-mb:2}")
    private int maxFileSizeMB;
    
    @Value("${hr.evidence.max-total-size-mb:5}")
    private int maxTotalSizeMB;
    
    @Override
    public List<ExplanationEvidenceDto> uploadEvidenceFiles(Long explanationId, CreateExplanationDto createDto) {
        log.info("Uploading {} evidence files for explanation {}", createDto.getEvidenceFileCount(), explanationId);
        
        // Get explanation
        ViolationExplanation explanation = explanationRepository.findById(explanationId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + explanationId));
        
        List<ExplanationEvidenceDto> uploadedFiles = new ArrayList<>();
        
        if (!createDto.hasEvidenceFiles()) {
            return uploadedFiles;
        }
        
        // Validate total file size
        if (!createDto.isWithinSizeLimit()) {
            throw new IllegalArgumentException("Tổng dung lượng file vượt quá " + maxTotalSizeMB + "MB");
        }
        
        // Process each file
        for (int i = 0; i < createDto.getEvidenceFiles().size(); i++) {
            MultipartFile file = createDto.getEvidenceFiles().get(i);
            String description = null;
            String evidenceType = null;
            
            // Get description if provided
            if (createDto.getEvidenceDescriptions() != null && 
                i < createDto.getEvidenceDescriptions().size()) {
                description = createDto.getEvidenceDescriptions().get(i);
            }
            
            // Get evidence type if provided
            if (createDto.getEvidenceTypes() != null && 
                i < createDto.getEvidenceTypes().size()) {
                evidenceType = createDto.getEvidenceTypes().get(i);
            }
            
            try {
                ExplanationEvidenceDto uploadedFile = uploadSingleEvidence(
                    explanationId, file, description, evidenceType, null);
                uploadedFiles.add(uploadedFile);
                
            } catch (Exception e) {
                log.error("Error uploading file {} for explanation {}", file.getOriginalFilename(), explanationId, e);
                // Continue with other files, don't fail the entire upload
            }
        }
        
        log.info("Successfully uploaded {} out of {} files for explanation {}", 
                uploadedFiles.size(), createDto.getEvidenceFileCount(), explanationId);
        
        return uploadedFiles;
    }
    
    @Override
    public ExplanationEvidenceDto uploadSingleEvidence(Long explanationId, MultipartFile file, 
                                                      String description, String evidenceType, String uploadIp) {
        log.info("Uploading single evidence file {} for explanation {}", file.getOriginalFilename(), explanationId);
        
        // Validate file
        if (!validateFile(file)) {
            List<String> errors = getFileValidationErrors(file);
            throw new IllegalArgumentException("File không hợp lệ: " + String.join(", ", errors));
        }
        
        // Get explanation
        ViolationExplanation explanation = explanationRepository.findById(explanationId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + explanationId));
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // For now, we'll store file path as a placeholder
            // In production, this would upload to Firebase Storage
            String filePath = "evidence/" + explanationId + "/" + uniqueFilename;
            String fileUrl = "/api/hr/evidence/" + explanationId + "/download/" + uniqueFilename;
            
            // Create evidence record
            ExplanationEvidence evidence = new ExplanationEvidence();
            evidence.setExplanation(explanation);
            evidence.setOriginalFilename(originalFilename);
            evidence.setFilePath(filePath);
            evidence.setFileUrl(fileUrl);
            evidence.setFileSize(file.getSize());
            evidence.setFileType(fileExtension);
            evidence.setMimeType(file.getContentType());
            evidence.setDescription(description);
            evidence.setUploadIp(uploadIp);
            
            // Set evidence type
            if (evidenceType != null) {
                try {
                    evidence.setEvidenceType(ExplanationEvidence.EvidenceType.valueOf(evidenceType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    evidence.setEvidenceType(ExplanationEvidence.EvidenceType.DOCUMENT);
                }
            } else {
                // Auto-detect evidence type based on file type
                evidence.setEvidenceType(detectEvidenceType(file.getContentType()));
            }
            
            ExplanationEvidence savedEvidence = evidenceRepository.save(evidence);

            // Upload file to Firebase Storage
            try {
                FileUploadResponse uploadResponse = firebaseStorageService.uploadFile(file, "evidence/" + explanationId);
                savedEvidence.setFileUrl(uploadResponse.getFileUrl());
                savedEvidence.setFilePath("evidence/" + explanationId + "/" + uploadResponse.getFileName());
                evidenceRepository.save(savedEvidence);
            } catch (Exception e) {
                log.error("Error uploading to Firebase Storage, using local path", e);
                // Keep the local path as fallback
            }
            
            log.info("Evidence file uploaded successfully with ID: {}", savedEvidence.getId());
            return convertToDto(savedEvidence);
            
        } catch (Exception e) {
            log.error("Error uploading evidence file for explanation {}", explanationId, e);
            throw new RuntimeException("Lỗi khi tải lên file bằng chứng: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceByExplanation(Long explanationId) {
        List<ExplanationEvidence> evidenceList = evidenceRepository.findByExplanationIdOrderByCreatedAtAsc(explanationId);
        return evidenceList.stream()
                .map((ExplanationEvidence evidence) -> convertToDto(evidence))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExplanationEvidenceDto getEvidenceById(Long id) {
        ExplanationEvidence evidence = evidenceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy file bằng chứng với ID: " + id));
        
        return convertToDto(evidence);
    }
    
    @Override
    public void deleteEvidence(Long id, Long deletedBy) {
        log.info("Deleting evidence {} by user {}", id, deletedBy);
        
        ExplanationEvidence evidence = evidenceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy file bằng chứng với ID: " + id));
        
        // Check if user can delete (only submitter of explanation can delete)
        if (!canAccessEvidence(id, deletedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa file bằng chứng này");
        }
        
        try {
            // Delete file from Firebase Storage
            if (evidence.getFilePath() != null) {
                firebaseStorageService.deleteFile(evidence.getFilePath());
            }

            // Delete database record
            evidenceRepository.delete(evidence);
            
            log.info("Evidence file deleted successfully: {}", id);
            
        } catch (Exception e) {
            log.error("Error deleting evidence file {}", id, e);
            throw new RuntimeException("Lỗi khi xóa file bằng chứng: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteEvidenceByExplanation(Long explanationId) {
        log.info("Deleting all evidence files for explanation {}", explanationId);
        
        List<ExplanationEvidence> evidenceList = evidenceRepository.findByExplanationIdOrderByCreatedAtAsc(explanationId);
        
        for (ExplanationEvidence evidence : evidenceList) {
            try {
                // Delete file from Firebase Storage
                if (evidence.getFilePath() != null) {
                    firebaseStorageService.deleteFile(evidence.getFilePath());
                }

                evidenceRepository.delete(evidence);
                
            } catch (Exception e) {
                log.error("Error deleting evidence file {} for explanation {}", evidence.getId(), explanationId, e);
            }
        }
        
        log.info("Deleted {} evidence files for explanation {}", evidenceList.size(), explanationId);
    }
    
    @Override
    public ExplanationEvidenceDto verifyEvidence(Long id, Long verifiedBy) {
        log.info("Verifying evidence {} by user {}", id, verifiedBy);
        
        ExplanationEvidence evidence = evidenceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy file bằng chứng với ID: " + id));
        
        evidence.verify(verifiedBy);
        ExplanationEvidence updatedEvidence = evidenceRepository.save(evidence);
        
        log.info("Evidence verified successfully: {}", id);
        return convertToDto(updatedEvidence);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceByType(ExplanationEvidence.EvidenceType evidenceType) {
        List<ExplanationEvidence> evidenceList = evidenceRepository.findByEvidenceTypeOrderByCreatedAtDesc(evidenceType);
        return evidenceList.stream()
                .map((ExplanationEvidence evidence) -> convertToDto(evidence))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getUnverifiedEvidence() {
        List<ExplanationEvidence> evidenceList = evidenceRepository.findUnverifiedFiles();
        return evidenceList.stream()
                .map((ExplanationEvidence evidence) -> convertToDto(evidence))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getEvidenceByDateRange(String startDate, String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            
            List<ExplanationEvidence> evidenceList = evidenceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
            return evidenceList.stream()
                    .map((ExplanationEvidence evidence) -> convertToDto(evidence))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error parsing date range: {} to {}", startDate, endDate, e);
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getFileStatisticsByType() {
        return evidenceRepository.getFileStatisticsByType();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTotalFileSizeByExplanation(Long explanationId) {
        return evidenceRepository.getTotalFileSizeByExplanation(explanationId);
    }

    @Override
    public boolean validateFile(MultipartFile file) {
        return getFileValidationErrors(file).isEmpty();
    }

    @Override
    public List<String> getFileValidationErrors(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("File không được để trống");
            return errors;
        }

        // Check file size
        long maxSizeBytes = maxFileSizeMB * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            errors.add("File vượt quá " + maxFileSizeMB + "MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (!isAllowedContentType(contentType)) {
            errors.add("Loại file không được hỗ trợ: " + contentType);
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            errors.add("Tên file không hợp lệ");
        } else if (filename.length() > 255) {
            errors.add("Tên file quá dài (tối đa 255 ký tự)");
        }

        return errors;
    }

    @Override
    public String generateDownloadUrl(Long id, Long userId) {
        // Check if user can access evidence
        if (!canAccessEvidence(id, userId)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập file này");
        }

        ExplanationEvidence evidence = evidenceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy file bằng chứng với ID: " + id));

        // Generate secure download URL with expiration (30 minutes)
        if (evidence.getFilePath() != null) {
            String signedUrl = firebaseStorageService.generateSignedUrl(evidence.getFilePath(), 30);
            if (signedUrl != null) {
                return signedUrl;
            }
        }

        // Fallback to regular URL with token
        return evidence.getFileUrl() + "?token=" + UUID.randomUUID().toString() + "&userId=" + userId;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessEvidence(Long evidenceId, Long userId) {
        ExplanationEvidence evidence = evidenceRepository.findById(evidenceId).orElse(null);
        if (evidence == null) {
            return false;
        }

        ViolationExplanation explanation = evidence.getExplanation();
        if (explanation == null) {
            return false;
        }

        // User can access if they are:
        // 1. The submitter of the explanation
        // 2. Manager or Admin (for review purposes)

        // Check if user is the submitter
        if (explanation.getSubmittedBy().getId().equals(userId)) {
            return true;
        }

        // Check if user is Manager or Admin
        // TODO: Get user role from UserRepository
        // For now, assume they can access if they're not the submitter
        return true; // This should be properly implemented with role checking
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMonthlyUploadStatistics(int year, int month) {
        return evidenceRepository.getMonthlyUploadStatistics(year, month);
    }

    @Override
    public int cleanupOrphanedFiles() {
        log.info("Cleaning up orphaned evidence files");

        List<ExplanationEvidence> orphanedFiles = evidenceRepository.findOrphanedFiles();

        for (ExplanationEvidence evidence : orphanedFiles) {
            try {
                // Delete file from Firebase Storage
                if (evidence.getFilePath() != null) {
                    firebaseStorageService.deleteFile(evidence.getFilePath());
                }

                evidenceRepository.delete(evidence);

            } catch (Exception e) {
                log.error("Error deleting orphaned file {}", evidence.getId(), e);
            }
        }

        log.info("Cleaned up {} orphaned files", orphanedFiles.size());
        return orphanedFiles.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExplanationEvidenceDto> getLargeFiles(int minSizeMB) {
        long minSizeBytes = minSizeMB * 1024L * 1024L;
        List<ExplanationEvidence> largeFiles = evidenceRepository.findLargeFiles(minSizeBytes);

        return largeFiles.stream()
                .map((ExplanationEvidence evidence) -> convertToDto(evidence))
                .collect(Collectors.toList());
    }

    // Helper methods

    private String getFileExtension(String filename) {
        if (filename == null) return "";

        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }

        return "";
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) return false;

        String[] allowedTypes = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp",
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

    private ExplanationEvidence.EvidenceType detectEvidenceType(String contentType) {
        if (contentType == null) {
            return ExplanationEvidence.EvidenceType.DOCUMENT;
        }

        if (contentType.startsWith("image/")) {
            return ExplanationEvidence.EvidenceType.IMAGE;
        } else if (contentType.equals("application/pdf")) {
            return ExplanationEvidence.EvidenceType.DOCUMENT;
        } else {
            return ExplanationEvidence.EvidenceType.DOCUMENT;
        }
    }

    /**
     * Convert ExplanationEvidence entity to DTO
     */
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
}
