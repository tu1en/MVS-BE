package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateExplanationDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ViolationExplanationDto;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ViolationExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.ExplanationEvidenceService;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationExplanationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ViolationExplanationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViolationExplanationServiceImpl implements ViolationExplanationService {
    
    private final ViolationExplanationRepository explanationRepository;
    private final AttendanceViolationRepository violationRepository;
    private final UserRepository userRepository;
    private final ExplanationEvidenceService evidenceService;
    private final ModelMapper modelMapper;
    
    @Override
    public ViolationExplanationDto submitExplanation(CreateExplanationDto createDto, Long submittedBy) {
        log.info("Submitting explanation for violation {} by user {}", createDto.getViolationId(), submittedBy);
        
        // Validate input
        if (!validateExplanationData(createDto)) {
            throw new IllegalArgumentException("Dữ liệu giải trình không hợp lệ");
        }
        
        // Check if user can submit explanation
        if (!canSubmitExplanation(createDto.getViolationId(), submittedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền gửi giải trình cho vi phạm này");
        }
        
        // Get violation and user
        AttendanceViolation violation = violationRepository.findById(createDto.getViolationId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm với ID: " + createDto.getViolationId()));
        
        User user = userRepository.findById(submittedBy)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + submittedBy));
        
        // Create explanation
        ViolationExplanation explanation = new ViolationExplanation();
        explanation.setViolation(violation);
        explanation.setSubmittedBy(user);
        explanation.setExplanationText(createDto.getExplanationText().trim());
        explanation.setStatus(ViolationExplanation.ExplanationStatus.SUBMITTED);
        
        ViolationExplanation savedExplanation = explanationRepository.save(explanation);
        
        // Update violation status
        violation.setStatus(AttendanceViolation.ViolationStatus.EXPLANATION_SUBMITTED);
        violationRepository.save(violation);
        
        // Handle evidence files if provided
        if (createDto.hasEvidenceFiles()) {
            try {
                evidenceService.uploadEvidenceFiles(savedExplanation.getId(), createDto);
            } catch (Exception e) {
                log.error("Error uploading evidence files for explanation {}", savedExplanation.getId(), e);
                // Don't fail the explanation submission, just log the error
            }
        }
        
        log.info("Explanation submitted successfully with ID: {}", savedExplanation.getId());
        return convertToDto(savedExplanation);
    }
    
    @Override
    public ViolationExplanationDto updateExplanation(Long id, CreateExplanationDto updateDto, Long submittedBy) {
        log.info("Updating explanation {} by user {}", id, submittedBy);
        
        ViolationExplanation explanation = explanationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + id));
        
        // Check if user can edit explanation
        if (!canEditExplanation(id, submittedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa giải trình này");
        }
        
        // Validate input
        if (!validateExplanationData(updateDto)) {
            throw new IllegalArgumentException("Dữ liệu giải trình không hợp lệ");
        }
        
        // Update explanation
        explanation.setExplanationText(updateDto.getExplanationText().trim());
        
        // Reset status if it was rejected or requires more info
        if (explanation.getStatus() == ViolationExplanation.ExplanationStatus.REJECTED ||
            explanation.getStatus() == ViolationExplanation.ExplanationStatus.REQUIRES_MORE_INFO) {
            explanation.setStatus(ViolationExplanation.ExplanationStatus.SUBMITTED);
            explanation.setReviewedAt(null);
            explanation.setReviewedBy(null);
            explanation.setReviewNotes(null);
            explanation.setIsValid(null);
        }
        
        ViolationExplanation updatedExplanation = explanationRepository.save(explanation);
        
        // Handle evidence files if provided
        if (updateDto.hasEvidenceFiles()) {
            try {
                evidenceService.uploadEvidenceFiles(updatedExplanation.getId(), updateDto);
            } catch (Exception e) {
                log.error("Error uploading evidence files for explanation {}", updatedExplanation.getId(), e);
            }
        }
        
        log.info("Explanation updated successfully: {}", updatedExplanation.getId());
        return convertToDto(updatedExplanation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ViolationExplanationDto getExplanationById(Long id) {
        ViolationExplanation explanation = explanationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + id));
        
        return convertToDto(explanation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ViolationExplanationDto> getExplanationsByUser(Long userId, Pageable pageable) {
        Page<ViolationExplanation> explanations = explanationRepository
            .findBySubmittedByIdOrderBySubmittedAtDesc(userId, pageable);
        
        return explanations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ViolationExplanationDto> getExplanationsByViolation(Long violationId) {
        List<ViolationExplanation> explanations = explanationRepository
            .findByViolationIdOrderBySubmittedAtDesc(violationId);
        
        return explanations.stream()
                .map((ViolationExplanation explanation) -> convertToDto(explanation))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ViolationExplanationDto> getPendingReviewExplanations(Pageable pageable) {
        Page<ViolationExplanation> explanations = explanationRepository.findPendingReview(pageable);
        return explanations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ViolationExplanationDto> getExplanationsByStatus(ViolationExplanation.ExplanationStatus status, Pageable pageable) {
        Page<ViolationExplanation> explanations = explanationRepository
            .findByStatusOrderBySubmittedAtDesc(status, pageable);
        
        return explanations.map(this::convertToDto);
    }
    
    @Override
    public ViolationExplanationDto approveExplanation(Long id, Long reviewedBy, String reviewNotes) {
        log.info("Approving explanation {} by user {}", id, reviewedBy);
        
        ViolationExplanation explanation = explanationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + id));
        
        // Check if user can review explanation
        if (!canReviewExplanation(id, reviewedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền xem xét giải trình này");
        }
        
        User reviewer = userRepository.findById(reviewedBy)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + reviewedBy));
        
        // Approve explanation
        explanation.approve(reviewer, reviewNotes);
        ViolationExplanation updatedExplanation = explanationRepository.save(explanation);
        
        // Update violation status
        AttendanceViolation violation = explanation.getViolation();
        violation.setStatus(AttendanceViolation.ViolationStatus.APPROVED);
        violation.setResolvedAt(LocalDateTime.now());
        violation.setResolvedBy(reviewedBy);
        violation.setResolutionNotes("Giải trình được chấp nhận: " + (reviewNotes != null ? reviewNotes : ""));
        violationRepository.save(violation);
        
        log.info("Explanation approved successfully: {}", updatedExplanation.getId());
        return convertToDto(updatedExplanation);
    }
    
    @Override
    public ViolationExplanationDto rejectExplanation(Long id, Long reviewedBy, String reviewNotes) {
        log.info("Rejecting explanation {} by user {}", id, reviewedBy);
        
        ViolationExplanation explanation = explanationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + id));
        
        // Check if user can review explanation
        if (!canReviewExplanation(id, reviewedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền xem xét giải trình này");
        }
        
        User reviewer = userRepository.findById(reviewedBy)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + reviewedBy));
        
        // Reject explanation
        explanation.reject(reviewer, reviewNotes);
        ViolationExplanation updatedExplanation = explanationRepository.save(explanation);
        
        // Update violation status
        AttendanceViolation violation = explanation.getViolation();
        violation.setStatus(AttendanceViolation.ViolationStatus.REJECTED);
        violationRepository.save(violation);
        
        log.info("Explanation rejected successfully: {}", updatedExplanation.getId());
        return convertToDto(updatedExplanation);
    }
    
    @Override
    public ViolationExplanationDto requestMoreInfo(Long id, Long reviewedBy, String reviewNotes) {
        log.info("Requesting more info for explanation {} by user {}", id, reviewedBy);
        
        ViolationExplanation explanation = explanationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + id));
        
        // Check if user can review explanation
        if (!canReviewExplanation(id, reviewedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền xem xét giải trình này");
        }
        
        User reviewer = userRepository.findById(reviewedBy)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + reviewedBy));
        
        // Request more info
        explanation.requestMoreInfo(reviewer, reviewNotes);
        ViolationExplanation updatedExplanation = explanationRepository.save(explanation);
        
        log.info("More info requested for explanation: {}", updatedExplanation.getId());
        return convertToDto(updatedExplanation);
    }

    @Override
    public void deleteExplanation(Long id, Long deletedBy) {
        log.info("Deleting explanation {} by user {}", id, deletedBy);

        ViolationExplanation explanation = explanationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giải trình với ID: " + id));

        // Check if user can edit explanation (only submitter can delete)
        if (!canEditExplanation(id, deletedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa giải trình này");
        }

        // Only allow deletion if explanation is in editable state
        if (!explanation.canBeEdited()) {
            throw new IllegalStateException("Không thể xóa giải trình đã được xem xét");
        }

        // Delete evidence files first
        try {
            evidenceService.deleteEvidenceByExplanation(id);
        } catch (Exception e) {
            log.error("Error deleting evidence files for explanation {}", id, e);
        }

        // Delete explanation
        explanationRepository.delete(explanation);

        // Reset violation status if this was the only explanation
        AttendanceViolation violation = explanation.getViolation();
        boolean hasOtherExplanations = explanationRepository.existsByViolationId(violation.getId());

        if (!hasOtherExplanations) {
            violation.setStatus(AttendanceViolation.ViolationStatus.PENDING_EXPLANATION);
            violationRepository.save(violation);
        }

        log.info("Explanation deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViolationExplanationDto> getOverdueExplanations(int daysSince) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSince);
        List<ViolationExplanation> explanations = explanationRepository.findOverdueForReview(cutoffDate);

        return explanations.stream()
                .map((ViolationExplanation explanation) -> convertToDto(explanation))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Object getExplanationStatisticsByStatus(LocalDateTime startDate, LocalDateTime endDate) {
        return explanationRepository.getExplanationStatisticsByStatus(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getExplanationStatisticsByUser(LocalDateTime startDate, LocalDateTime endDate) {
        return explanationRepository.getExplanationStatisticsByUser(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubmitExplanation(Long violationId, Long userId) {
        // Get violation
        AttendanceViolation violation = violationRepository.findById(violationId).orElse(null);
        if (violation == null) {
            return false;
        }

        // Only the user who has the violation can submit explanation
        if (!violation.getUser().getId().equals(userId)) {
            return false;
        }

        // Check if violation can be explained
        return violation.canBeExplained();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditExplanation(Long explanationId, Long userId) {
        ViolationExplanation explanation = explanationRepository.findById(explanationId).orElse(null);
        if (explanation == null) {
            return false;
        }

        // Only the submitter can edit
        if (!explanation.getSubmittedBy().getId().equals(userId)) {
            return false;
        }

        // Check if explanation can be edited
        return explanation.canBeEdited();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReviewExplanation(Long explanationId, Long userId) {
        ViolationExplanation explanation = explanationRepository.findById(explanationId).orElse(null);
        if (explanation == null) {
            return false;
        }

        // Get user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Only Manager and Admin can review
        String role = user.getRole();
        if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            return false;
        }

        // Cannot review own explanation
        if (explanation.getSubmittedBy().getId().equals(userId)) {
            return false;
        }

        // Check if explanation is pending review
        return explanation.isPendingReview();
    }

    @Override
    @Transactional(readOnly = true)
    public ViolationExplanationDto getLatestExplanationForViolation(Long violationId) {
        return explanationRepository.findFirstByViolationIdOrderBySubmittedAtDesc(violationId)
                .map((ViolationExplanation explanation) -> convertToDto(explanation))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countExplanationsByStatus(ViolationExplanation.ExplanationStatus status) {
        return explanationRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countExplanationsByUser(Long userId) {
        return explanationRepository.countBySubmittedById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViolationExplanationDto> getExplanationsWithEvidence() {
        List<ViolationExplanation> explanations = explanationRepository.findExplanationsWithEvidence();
        return explanations.stream()
                .map((ViolationExplanation explanation) -> convertToDto(explanation))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViolationExplanationDto> getExplanationsWithoutEvidence() {
        List<ViolationExplanation> explanations = explanationRepository.findExplanationsWithoutEvidence();
        return explanations.stream()
                .map((ViolationExplanation explanation) -> convertToDto(explanation))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateExplanationData(CreateExplanationDto createDto) {
        if (createDto == null) {
            return false;
        }

        // Check required fields
        if (createDto.getViolationId() == null) {
            return false;
        }

        if (createDto.getExplanationText() == null || createDto.getExplanationText().trim().isEmpty()) {
            return false;
        }

        if (createDto.getExplanationText().trim().length() < 10 || createDto.getExplanationText().trim().length() > 2000) {
            return false;
        }

        // Validate evidence data consistency
        if (!createDto.isEvidenceDataConsistent()) {
            return false;
        }

        // Validate file constraints
        if (!createDto.isValid()) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMonthlyExplanationSummary(int year, int month) {
        return explanationRepository.getMonthlyExplanationSummary(year, month);
    }

    /**
     * Convert ViolationExplanation entity to DTO
     */
    private ViolationExplanationDto convertToDto(ViolationExplanation explanation) {
        ViolationExplanationDto dto = modelMapper.map(explanation, ViolationExplanationDto.class);

        // Set additional fields
        if (explanation.getSubmittedBy() != null) {
            dto.setSubmittedBy(explanation.getSubmittedBy().getId());
            dto.setSubmittedByName(explanation.getSubmittedBy().getFullName());
            dto.setSubmittedByEmail(explanation.getSubmittedBy().getEmail());
        }

        if (explanation.getReviewedBy() != null) {
            dto.setReviewedBy(explanation.getReviewedBy().getId());
            dto.setReviewedByName(explanation.getReviewedBy().getFullName());
        }

        if (explanation.getViolation() != null) {
            dto.setViolationId(explanation.getViolation().getId());
        }

        // Set evidence files (will be populated by evidence service)
        try {
            dto.setEvidenceFiles(evidenceService.getEvidenceByExplanation(explanation.getId()));
        } catch (Exception e) {
            log.warn("Error loading evidence files for explanation {}", explanation.getId());
            dto.setEvidenceFiles(List.of());
        }

        // Set computed fields
        dto.setIsPendingReview(explanation.isPendingReview());
        dto.setIsApproved(explanation.isApproved());
        dto.setIsRejected(explanation.isRejected());
        dto.setCanBeEdited(explanation.canBeEdited());
        dto.setDaysSinceSubmission(explanation.getDaysSinceSubmission());
        dto.setIsOverdueForReview(explanation.isOverdueForReview(2));

        return dto;
    }
}
