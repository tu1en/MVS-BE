package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.AttendanceViolationService;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationExplanationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AttendanceViolationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceViolationServiceImpl implements AttendanceViolationService {
    
    private final AttendanceViolationRepository violationRepository;
    private final UserRepository userRepository;
    private final ViolationExplanationService explanationService;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional(readOnly = true)
    public AttendanceViolationDto getViolationById(Long id) {
        AttendanceViolation violation = violationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm với ID: " + id));
        
        return convertToDto(violation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsByUser(Long userId, Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findByUserIdOrderByViolationDateDesc(userId, pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsByStatus(AttendanceViolation.ViolationStatus status, Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findByStatusOrderByViolationDateDesc(status, pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsByType(AttendanceViolation.ViolationType violationType, Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findByViolationTypeOrderByViolationDateDesc(violationType, pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findByViolationDateBetweenOrderByViolationDateDesc(startDate, endDate, pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceViolationDto> getViolationsForDate(LocalDate date) {
        List<AttendanceViolation> violations = violationRepository.findByViolationDateOrderByUserIdAsc(date);
        return violations.stream()
                .map((AttendanceViolation violation) -> convertToDto(violation))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsNeedingExplanation(Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findViolationsNeedingExplanation(pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsPendingReview(Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findViolationsPendingReview(pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceViolationDto> getOverdueViolations(int daysSince) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysSince);
        List<AttendanceViolation> violations = violationRepository.findOverdueViolations(cutoffDate);
        
        return violations.stream()
                .map((AttendanceViolation violation) -> convertToDto(violation))
                .collect(Collectors.toList());
    }
    
    @Override
    public AttendanceViolationDto resolveViolation(Long id, Long resolvedBy, String resolutionNotes) {
        log.info("Resolving violation {} by user {}", id, resolvedBy);
        
        AttendanceViolation violation = violationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm với ID: " + id));
        
        // Check if user can resolve violation
        if (!canResolveViolation(id, resolvedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền giải quyết vi phạm này");
        }
        
        // Update violation
        violation.setStatus(AttendanceViolation.ViolationStatus.RESOLVED);
        violation.setResolvedAt(LocalDateTime.now());
        violation.setResolvedBy(resolvedBy);
        violation.setResolutionNotes(resolutionNotes);
        
        AttendanceViolation updatedViolation = violationRepository.save(violation);
        
        log.info("Violation resolved successfully: {}", id);
        return convertToDto(updatedViolation);
    }
    
    @Override
    public AttendanceViolationDto escalateViolation(Long id, Long escalatedBy, String escalationNotes) {
        log.info("Escalating violation {} by user {}", id, escalatedBy);
        
        AttendanceViolation violation = violationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm với ID: " + id));
        
        // Check if user can escalate violation
        if (!canResolveViolation(id, escalatedBy)) {
            throw new IllegalArgumentException("Bạn không có quyền chuyển vi phạm này lên cấp trên");
        }
        
        // Update violation
        violation.setStatus(AttendanceViolation.ViolationStatus.ESCALATED);
        violation.setResolvedBy(escalatedBy);
        violation.setResolutionNotes("Chuyển lên cấp trên: " + (escalationNotes != null ? escalationNotes : ""));
        
        AttendanceViolation updatedViolation = violationRepository.save(violation);
        
        log.info("Violation escalated successfully: {}", id);
        return convertToDto(updatedViolation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getViolationStatisticsByType(LocalDate startDate, LocalDate endDate) {
        return violationRepository.getViolationStatisticsByType(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getViolationStatisticsByUser(LocalDate startDate, LocalDate endDate) {
        return violationRepository.getViolationStatisticsByUser(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getMonthlyViolationSummary(int year, int month) {
        return violationRepository.getMonthlyViolationSummary(year, month);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countViolationsByUser(Long userId) {
        return violationRepository.countByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countViolationsByStatus(AttendanceViolation.ViolationStatus status) {
        return violationRepository.countByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countViolationsByUserInDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return violationRepository.countByUserIdAndViolationDateBetween(userId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsBySeverity(AttendanceViolation.ViolationSeverity severity, Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findBySeverityOrderByViolationDateDesc(severity, pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceViolationDto> getViolationsByAutoDetected(Boolean autoDetected, Pageable pageable) {
        Page<AttendanceViolation> violations = violationRepository.findByAutoDetectedOrderByDetectionTimeDesc(autoDetected, pageable);
        return violations.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceViolationDto> getViolationsResolvedBy(Long resolvedBy) {
        List<AttendanceViolation> violations = violationRepository.findByResolvedByOrderByResolvedAtDesc(resolvedBy);
        return violations.stream()
                .map((AttendanceViolation violation) -> convertToDto(violation))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceViolationDto> getViolationsByShiftAssignment(Long shiftAssignmentId) {
        List<AttendanceViolation> violations = violationRepository.findByShiftAssignmentIdOrderByViolationDateDesc(shiftAssignmentId);
        return violations.stream()
                .map((AttendanceViolation violation) -> convertToDto(violation))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canViewViolation(Long violationId, Long userId) {
        AttendanceViolation violation = violationRepository.findById(violationId).orElse(null);
        if (violation == null) {
            return false;
        }
        
        // User can view if they are:
        // 1. The owner of the violation
        // 2. Manager or Admin
        
        // Check if user is the owner
        if (violation.getUser().getId().equals(userId)) {
            return true;
        }
        
        // Check if user is Manager or Admin
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        String role = user.getRole();
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canResolveViolation(Long violationId, Long userId) {
        AttendanceViolation violation = violationRepository.findById(violationId).orElse(null);
        if (violation == null) {
            return false;
        }
        
        // Only Manager and Admin can resolve violations
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        String role = user.getRole();
        if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            return false;
        }
        
        // Cannot resolve own violations
        if (violation.getUser().getId().equals(userId)) {
            return false;
        }
        
        // Check if violation can be resolved
        return !violation.isResolved();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getViolationDashboardData(Long userId) {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Get violation counts by status
        long pendingExplanation = violationRepository.countByUserIdAndStatus(userId, AttendanceViolation.ViolationStatus.PENDING_EXPLANATION);
        long explanationSubmitted = violationRepository.countByUserIdAndStatus(userId, AttendanceViolation.ViolationStatus.EXPLANATION_SUBMITTED);
        long underReview = violationRepository.countByUserIdAndStatus(userId, AttendanceViolation.ViolationStatus.UNDER_REVIEW);
        long approved = violationRepository.countByUserIdAndStatus(userId, AttendanceViolation.ViolationStatus.APPROVED);
        long rejected = violationRepository.countByUserIdAndStatus(userId, AttendanceViolation.ViolationStatus.REJECTED);
        long resolved = violationRepository.countByUserIdAndStatus(userId, AttendanceViolation.ViolationStatus.RESOLVED);
        
        dashboardData.put("pendingExplanation", pendingExplanation);
        dashboardData.put("explanationSubmitted", explanationSubmitted);
        dashboardData.put("underReview", underReview);
        dashboardData.put("approved", approved);
        dashboardData.put("rejected", rejected);
        dashboardData.put("resolved", resolved);
        dashboardData.put("total", pendingExplanation + explanationSubmitted + underReview + approved + rejected + resolved);
        
        // Get recent violations
        List<AttendanceViolation> recentViolations = violationRepository.findByUserIdOrderByViolationDateDesc(userId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        
        dashboardData.put("recentViolations", recentViolations.stream()
                .map((AttendanceViolation violation) -> convertToDto(violation))
                .collect(Collectors.toList()));
        
        return dashboardData;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getManagerViolationDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Get violation counts by status (all users)
        long pendingExplanation = violationRepository.findViolationsNeedingExplanation().size();
        long pendingReview = violationRepository.findViolationsPendingReview().size();
        long overdue = getOverdueViolations(3).size(); // 3 days overdue
        
        dashboardData.put("pendingExplanation", pendingExplanation);
        dashboardData.put("pendingReview", pendingReview);
        dashboardData.put("overdue", overdue);
        
        // Get recent violations needing attention
        List<AttendanceViolation> recentViolations = violationRepository.findViolationsPendingReview()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        
        dashboardData.put("recentViolations", recentViolations.stream()
                .map((AttendanceViolation violation) -> convertToDto(violation))
                .collect(Collectors.toList()));
        
        return dashboardData;
    }

    /**
     * Convert AttendanceViolation entity to DTO
     */
    private AttendanceViolationDto convertToDto(AttendanceViolation violation) {
        AttendanceViolationDto dto = modelMapper.map(violation, AttendanceViolationDto.class);

        // Set additional fields
        if (violation.getUser() != null) {
            dto.setUserId(violation.getUser().getId());
            dto.setUserFullName(violation.getUser().getFullName());
            dto.setUserEmail(violation.getUser().getEmail());
            // dto.setUserDepartment(violation.getUser().getDepartment()); // If department field exists
        }

        if (violation.getShiftAssignment() != null) {
            dto.setShiftAssignmentId(violation.getShiftAssignment().getId());
            if (violation.getShiftAssignment().getWorkShift() != null) {
                dto.setShiftName(violation.getShiftAssignment().getWorkShift().getShiftName());
                dto.setShiftTimeRange(violation.getShiftAssignment().getWorkShift().getStartTime() +
                                    " - " + violation.getShiftAssignment().getWorkShift().getEndTime());
            }
        }

        if (violation.getAttendanceLog() != null) {
            dto.setAttendanceLogId(violation.getAttendanceLog().getId());
        }

        // Set computed fields
        dto.setNeedsExplanation(violation.needsExplanation());
        dto.setIsResolved(violation.isResolved());
        dto.setCanBeExplained(violation.canBeExplained());
        dto.setDaysSinceViolation(violation.getDaysSinceViolation());
        dto.setIsOverdueForExplanation(violation.isOverdueForExplanation(3)); // 3 days threshold

        // Set explanation info
        try {
            var latestExplanation = explanationService.getLatestExplanationForViolation(violation.getId());
            dto.setHasExplanation(latestExplanation != null);
            if (latestExplanation != null) {
                dto.setLatestExplanationStatus(latestExplanation.getStatus().name());
            }

            // Count explanations
            var explanations = explanationService.getExplanationsByViolation(violation.getId());
            dto.setExplanationCount(explanations.size());

        } catch (Exception e) {
            log.warn("Error loading explanation info for violation {}", violation.getId());
            dto.setHasExplanation(false);
            dto.setExplanationCount(0);
        }

        return dto;
    }
}
