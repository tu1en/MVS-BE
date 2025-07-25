package com.classroomapp.classroombackend.service.hrmanagement.shift.impl;

import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftSwapRequestRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftSwapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation cá»§a ShiftSwapService
 * Xá»­ lÃ½ business logic cho shift swap request management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftSwapServiceImpl implements ShiftSwapService {

    private final ShiftSwapRequestRepository swapRequestRepository;
    private final ShiftConflictDetectionService conflictDetectionService;

    @Override
    public ShiftSwapRequest createSwapRequest(ShiftSwapRequest request) {
        log.info("Táº¡o swap request má»›i tá»« employee {} Ä‘áº¿n employee {}", 
                request.getRequester().getId(), request.getTargetEmployee().getId());

        validateSwapRequest(request);

        // Kiá»ƒm tra xung Ä‘á»™t
        ShiftConflictDetectionService.ConflictCheckResult conflicts = checkSwapConflicts(request);
        if (conflicts.hasConflict()) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ táº¡o swap request: " + conflicts.getSummary());
        }

        // Kiá»ƒm tra xem cÃ³ request pending nÃ o cho assignments nÃ y khÃ´ng
        if (swapRequestRepository.existsPendingRequestForAssignment(request.getRequesterAssignment().getId()) ||
            swapRequestRepository.existsPendingRequestForAssignment(request.getTargetAssignment().getId())) {
            throw new BusinessLogicException("ÄÃ£ cÃ³ yÃªu cáº§u Ä‘á»•i ca Ä‘ang chá» xá»­ lÃ½ cho má»™t trong cÃ¡c ca nÃ y");
        }

        ShiftSwapRequest saved = swapRequestRepository.save(request);
        log.info("ÄÃ£ táº¡o swap request vá»›i ID: {}", saved.getId());

        // Gá»­i notification
        sendSwapNotifications(saved, NotificationType.REQUEST_CREATED);

        return saved;
    }

    @Override
    public ShiftSwapRequest updateSwapRequest(Long id, ShiftSwapRequest request) {
        log.info("Cáº­p nháº­t swap request ID: {}", id);

        ShiftSwapRequest existing = swapRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y swap request vá»›i ID: " + id));

        if (existing.getStatus() != ShiftSwapRequest.SwapStatus.PENDING) {
            throw new BusinessLogicException("Chá»‰ cÃ³ thá»ƒ cáº­p nháº­t request Ä‘ang pending");
        }

        // Chá»‰ cho phÃ©p requester cáº­p nháº­t
        if (!existing.getRequester().getId().equals(request.getRequester().getId())) {
            throw new BusinessLogicException("Chá»‰ ngÆ°á»i táº¡o request má»›i cÃ³ thá»ƒ cáº­p nháº­t");
        }

        existing.setRequestReason(request.getRequestReason());
        existing.setPriority(request.getPriority());
        existing.setIsEmergency(request.getIsEmergency());

        ShiftSwapRequest updated = swapRequestRepository.save(existing);
        log.info("ÄÃ£ cáº­p nháº­t swap request ID: {}", id);

        return updated;
    }

    @Override
    public void cancelSwapRequest(Long id, String reason) {
        log.info("Há»§y swap request ID: {} vá»›i lÃ½ do: {}", id, reason);

        ShiftSwapRequest request = swapRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y swap request vá»›i ID: " + id));

        request.cancel();
        swapRequestRepository.save(request);

        // Gá»­i notification
        sendSwapNotifications(request, NotificationType.REQUEST_CANCELLED);

        log.info("ÄÃ£ há»§y swap request ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftSwapRequest> findById(Long id) {
        return swapRequestRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findByRequester(Long requesterId) {
        return swapRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findByTargetEmployee(Long targetEmployeeId) {
        return swapRequestRepository.findByTargetEmployeeIdOrderByCreatedAtDesc(targetEmployeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findPendingRequestsForTarget(Long targetEmployeeId) {
        return swapRequestRepository.findPendingRequestsForTarget(targetEmployeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findPendingManagerApproval() {
        return swapRequestRepository.findPendingManagerApproval();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findEmergencyRequests() {
        return swapRequestRepository.findEmergencyRequests();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftSwapRequest> searchRequests(Long requesterId, Long targetEmployeeId,
                                                ShiftSwapRequest.SwapStatus status,
                                                ShiftSwapRequest.Priority priority,
                                                Boolean isEmergency, String search,
                                                Pageable pageable) {
        return swapRequestRepository.searchRequests(requesterId, targetEmployeeId, status, 
                                                   priority, isEmergency, search, pageable);
    }

    @Override
    public ShiftSwapRequest respondByTarget(Long requestId, ShiftSwapRequest.TargetResponse response,
                                           String reason, User targetEmployee) {
        log.info("Target employee {} pháº£n há»“i request ID: {} vá»›i response: {}", 
                targetEmployee.getId(), requestId, response);

        ShiftSwapRequest request = swapRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y swap request vá»›i ID: " + requestId));

        // Validate quyá»n pháº£n há»“i
        if (!request.getTargetEmployee().getId().equals(targetEmployee.getId())) {
            throw new BusinessLogicException("Chá»‰ target employee má»›i cÃ³ thá»ƒ pháº£n há»“i request nÃ y");
        }

        request.respondByTarget(response, reason);
        ShiftSwapRequest updated = swapRequestRepository.save(request);

        // Gá»­i notification
        NotificationType notificationType = response == ShiftSwapRequest.TargetResponse.ACCEPTED ?
            NotificationType.REQUEST_ACCEPTED : NotificationType.REQUEST_REJECTED;
        sendSwapNotifications(updated, notificationType);

        log.info("Target employee Ä‘Ã£ pháº£n há»“i request ID: {} vá»›i {}", requestId, response);
        return updated;
    }

    @Override
    public ShiftSwapRequest approveByManager(Long requestId, ShiftSwapRequest.ManagerResponse response,
                                            String reason, User manager) {
        log.info("Manager {} phÃª duyá»‡t request ID: {} vá»›i response: {}", 
                manager.getId(), requestId, response);

        ShiftSwapRequest request = swapRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y swap request vá»›i ID: " + requestId));

        request.approveByManager(manager, response, reason);
        ShiftSwapRequest updated = swapRequestRepository.save(request);

        // Náº¿u Ä‘Æ°á»£c phÃª duyá»‡t, thá»±c hiá»‡n swap
        if (response == ShiftSwapRequest.ManagerResponse.APPROVED) {
            executeSwap(requestId);
        }

        // Gá»­i notification
        NotificationType notificationType = response == ShiftSwapRequest.ManagerResponse.APPROVED ?
            NotificationType.REQUEST_APPROVED : NotificationType.REQUEST_REJECTED;
        sendSwapNotifications(updated, notificationType);

        log.info("Manager Ä‘Ã£ phÃª duyá»‡t request ID: {} vá»›i {}", requestId, response);
        return updated;
    }

    @Override
    public void validateSwapRequest(ShiftSwapRequest request) {
        if (request == null) {
            throw new BusinessLogicException("Swap request khÃ´ng Ä‘Æ°á»£c null");
        }

        if (!request.isValidRequest()) {
            throw new BusinessLogicException("ThÃ´ng tin swap request khÃ´ng há»£p lá»‡");
        }

        // Kiá»ƒm tra assignments cÃ³ cÃ¹ng shift template khÃ´ng
        if (!request.getRequesterAssignment().getShiftTemplate().getId()
                .equals(request.getTargetAssignment().getShiftTemplate().getId())) {
            throw new BusinessLogicException("Chá»‰ cÃ³ thá»ƒ Ä‘á»•i ca cÃ¹ng loáº¡i");
        }

        // Kiá»ƒm tra assignments chÆ°a báº¯t Ä‘áº§u
        if (request.getRequesterAssignment().getStatus() != 
            com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment.AssignmentStatus.SCHEDULED ||
            request.getTargetAssignment().getStatus() != 
            com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment.AssignmentStatus.SCHEDULED) {
            throw new BusinessLogicException("Chá»‰ cÃ³ thá»ƒ Ä‘á»•i ca chÆ°a báº¯t Ä‘áº§u");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftConflictDetectionService.ConflictCheckResult checkSwapConflicts(ShiftSwapRequest request) {
        return conflictDetectionService.checkSwapConflicts(
            request.getRequester().getId(),
            request.getTargetEmployee().getId(),
            request.getRequesterAssignment(),
            request.getTargetAssignment()
        );
    }

    @Override
    public void executeSwap(Long requestId) {
        log.info("Thá»±c hiá»‡n swap cho request ID: {}", requestId);

        ShiftSwapRequest request = swapRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y swap request vá»›i ID: " + requestId));

        if (request.getStatus() != ShiftSwapRequest.SwapStatus.APPROVED) {
            throw new BusinessLogicException("Chá»‰ cÃ³ thá»ƒ thá»±c hiá»‡n swap cho request Ä‘Ã£ Ä‘Æ°á»£c phÃª duyá»‡t");
        }

        // Swap assignments
        var requesterAssignment = request.getRequesterAssignment();
        var targetAssignment = request.getTargetAssignment();

        // Táº¡o temporary variables Ä‘á»ƒ swap
        var tempEmployee = requesterAssignment.getEmployee();
        requesterAssignment.setEmployee(targetAssignment.getEmployee());
        targetAssignment.setEmployee(tempEmployee);

        // Note: Actual swap implementation would involve updating assignments in database
        // This is simplified for demonstration

        log.info("ÄÃ£ thá»±c hiá»‡n swap cho request ID: {}", requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findExpiredRequests() {
        return swapRequestRepository.findExpiredRequests();
    }

    @Override
    public int markExpiredRequests() {
        int marked = swapRequestRepository.markExpiredRequests();
        log.info("ÄÃ£ Ä‘Ã¡nh dáº¥u {} requests háº¿t háº¡n", marked);
        return marked;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findRequestsNeedingNotification(int hoursBeforeExpiry) {
        LocalDateTime notificationTime = LocalDateTime.now().plusHours(hoursBeforeExpiry);
        return swapRequestRepository.findRequestsNeedingNotification(notificationTime);
    }

    @Override
    public void sendSwapNotifications(ShiftSwapRequest request, NotificationType type) {
        log.info("Gá»­i notification {} cho swap request ID: {}", type, request.getId());
        // TODO: Implement notification sending logic
    }

    @Override
    @Transactional(readOnly = true)
    public SwapStatistics getSwapStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Object[] stats = swapRequestRepository.getApprovalStatistics(startTime, endTime);
        
        if (stats != null && stats.length >= 4) {
            long approvedCount = ((Number) stats[0]).longValue();
            long rejectedCount = ((Number) stats[1]).longValue();
            long rejectedByTargetCount = ((Number) stats[2]).longValue();
            long totalCount = ((Number) stats[3]).longValue();
            
            long pendingCount = totalCount - approvedCount - rejectedCount - rejectedByTargetCount;
            
            return new SwapStatistics(totalCount, pendingCount, approvedCount, 
                                    rejectedCount + rejectedByTargetCount, 0);
        }
        
        return new SwapStatistics(0, 0, 0, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findTopRequesters(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return swapRequestRepository.findTopRequesters(startTime, endTime, PageRequest.of(0, limit));
    }

    @Override
    public void processExpiredRequests() {
        markExpiredRequests();
        
        // Gá»­i notifications cho expired requests
        List<ShiftSwapRequest> expiredRequests = findExpiredRequests();
        for (ShiftSwapRequest request : expiredRequests) {
            sendSwapNotifications(request, NotificationType.REQUEST_EXPIRED);
        }
        
        log.info("ÄÃ£ xá»­ lÃ½ {} expired requests", expiredRequests.size());
    }

    @Override
    public int cleanupOldRequests(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = swapRequestRepository.deleteOldRequests(cutoffDate);
        log.info("ÄÃ£ xÃ³a {} old requests (> {} ngÃ y)", deleted, daysOld);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SwapSuggestion> suggestSwapPartners(Long assignmentId) {
        // TODO: Implement swap suggestion logic
        return new ArrayList<>();
    }

    @Override
    public List<ShiftSwapRequest> bulkProcessRequests(List<Long> requestIds,
                                                     ShiftSwapRequest.ManagerResponse response,
                                                     String reason, User manager) {
        List<ShiftSwapRequest> processed = new ArrayList<>();
        
        for (Long requestId : requestIds) {
            try {
                ShiftSwapRequest processed_request = approveByManager(requestId, response, reason, manager);
                processed.add(processed_request);
            } catch (Exception e) {
                log.error("Lá»—i khi xá»­ lÃ½ request ID {}: {}", requestId, e.getMessage());
            }
        }
        
        log.info("ÄÃ£ bulk process {} requests", processed.size());
        return processed;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSwapRequests(LocalDateTime startTime, LocalDateTime endTime, String format) {
        // TODO: Implement export functionality
        throw new BusinessLogicException("Export functionality chÆ°a Ä‘Æ°á»£c implement");
    }
}
