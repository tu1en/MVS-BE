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
 * Implementation của ShiftSwapService
 * Xử lý business logic cho shift swap request management
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
        log.info("Tạo swap request mới từ employee {} đến employee {}", 
                request.getRequester().getId(), request.getTargetEmployee().getId());

        validateSwapRequest(request);

        // Kiểm tra xung đột
        ShiftConflictDetectionService.ConflictCheckResult conflicts = checkSwapConflicts(request);
        if (conflicts.hasConflict()) {
            throw new BusinessLogicException("Không thể tạo swap request: " + conflicts.getSummary());
        }

        // Kiểm tra xem có request pending nào cho assignments này không
        if (swapRequestRepository.existsPendingRequestForAssignment(request.getRequesterAssignment().getId()) ||
            swapRequestRepository.existsPendingRequestForAssignment(request.getTargetAssignment().getId())) {
            throw new BusinessLogicException("Đã có yêu cầu đổi ca đang chờ xử lý cho một trong các ca này");
        }

        ShiftSwapRequest saved = swapRequestRepository.save(request);
        log.info("Đã tạo swap request với ID: {}", saved.getId());

        // Gửi notification
        sendSwapNotifications(saved, NotificationType.REQUEST_CREATED);

        return saved;
    }

    @Override
    public ShiftSwapRequest updateSwapRequest(Long id, ShiftSwapRequest request) {
        log.info("Cập nhật swap request ID: {}", id);

        ShiftSwapRequest existing = swapRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy swap request với ID: " + id));

        if (existing.getStatus() != ShiftSwapRequest.SwapStatus.PENDING) {
            throw new BusinessLogicException("Chỉ có thể cập nhật request đang pending");
        }

        // Chỉ cho phép requester cập nhật
        if (!existing.getRequester().getId().equals(request.getRequester().getId())) {
            throw new BusinessLogicException("Chỉ người tạo request mới có thể cập nhật");
        }

        existing.setRequestReason(request.getRequestReason());
        existing.setPriority(request.getPriority());
        existing.setIsEmergency(request.getIsEmergency());

        ShiftSwapRequest updated = swapRequestRepository.save(existing);
        log.info("Đã cập nhật swap request ID: {}", id);

        return updated;
    }

    @Override
    public void cancelSwapRequest(Long id, String reason) {
        log.info("Hủy swap request ID: {} với lý do: {}", id, reason);

        ShiftSwapRequest request = swapRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy swap request với ID: " + id));

        request.cancel();
        swapRequestRepository.save(request);

        // Gửi notification
        sendSwapNotifications(request, NotificationType.REQUEST_CANCELLED);

        log.info("Đã hủy swap request ID: {}", id);
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
        log.info("Target employee {} phản hồi request ID: {} với response: {}", 
                targetEmployee.getId(), requestId, response);

        ShiftSwapRequest request = swapRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy swap request với ID: " + requestId));

        // Validate quyền phản hồi
        if (!request.getTargetEmployee().getId().equals(targetEmployee.getId())) {
            throw new BusinessLogicException("Chỉ target employee mới có thể phản hồi request này");
        }

        request.respondByTarget(response, reason);
        ShiftSwapRequest updated = swapRequestRepository.save(request);

        // Gửi notification
        NotificationType notificationType = response == ShiftSwapRequest.TargetResponse.ACCEPTED ?
            NotificationType.REQUEST_ACCEPTED : NotificationType.REQUEST_REJECTED;
        sendSwapNotifications(updated, notificationType);

        log.info("Target employee đã phản hồi request ID: {} với {}", requestId, response);
        return updated;
    }

    @Override
    public ShiftSwapRequest approveByManager(Long requestId, ShiftSwapRequest.ManagerResponse response,
                                            String reason, User manager) {
        log.info("Manager {} phê duyệt request ID: {} với response: {}", 
                manager.getId(), requestId, response);

        ShiftSwapRequest request = swapRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy swap request với ID: " + requestId));

        request.approveByManager(manager, response, reason);
        ShiftSwapRequest updated = swapRequestRepository.save(request);

        // Nếu được phê duyệt, thực hiện swap
        if (response == ShiftSwapRequest.ManagerResponse.APPROVED) {
            executeSwap(requestId);
        }

        // Gửi notification
        NotificationType notificationType = response == ShiftSwapRequest.ManagerResponse.APPROVED ?
            NotificationType.REQUEST_APPROVED : NotificationType.REQUEST_REJECTED;
        sendSwapNotifications(updated, notificationType);

        log.info("Manager đã phê duyệt request ID: {} với {}", requestId, response);
        return updated;
    }

    @Override
    public void validateSwapRequest(ShiftSwapRequest request) {
        if (request == null) {
            throw new BusinessLogicException("Swap request không được null");
        }

        if (!request.isValidRequest()) {
            throw new BusinessLogicException("Thông tin swap request không hợp lệ");
        }

        // Kiểm tra assignments có cùng shift template không
        if (!request.getRequesterAssignment().getShiftTemplate().getId()
                .equals(request.getTargetAssignment().getShiftTemplate().getId())) {
            throw new BusinessLogicException("Chỉ có thể đổi ca cùng loại");
        }

        // Kiểm tra assignments chưa bắt đầu
        if (request.getRequesterAssignment().getStatus() != 
            com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment.AssignmentStatus.SCHEDULED ||
            request.getTargetAssignment().getStatus() != 
            com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment.AssignmentStatus.SCHEDULED) {
            throw new BusinessLogicException("Chỉ có thể đổi ca chưa bắt đầu");
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
        log.info("Thực hiện swap cho request ID: {}", requestId);

        ShiftSwapRequest request = swapRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy swap request với ID: " + requestId));

        if (request.getStatus() != ShiftSwapRequest.SwapStatus.APPROVED) {
            throw new BusinessLogicException("Chỉ có thể thực hiện swap cho request đã được phê duyệt");
        }

        // Swap assignments
        var requesterAssignment = request.getRequesterAssignment();
        var targetAssignment = request.getTargetAssignment();

        // Tạo temporary variables để swap
        var tempEmployee = requesterAssignment.getEmployee();
        requesterAssignment.setEmployee(targetAssignment.getEmployee());
        targetAssignment.setEmployee(tempEmployee);

        // Note: Actual swap implementation would involve updating assignments in database
        // This is simplified for demonstration

        log.info("Đã thực hiện swap cho request ID: {}", requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSwapRequest> findExpiredRequests() {
        return swapRequestRepository.findExpiredRequests();
    }

    @Override
    public int markExpiredRequests() {
        int marked = swapRequestRepository.markExpiredRequests();
        log.info("Đã đánh dấu {} requests hết hạn", marked);
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
        log.info("Gửi notification {} cho swap request ID: {}", type, request.getId());
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
        
        // Gửi notifications cho expired requests
        List<ShiftSwapRequest> expiredRequests = findExpiredRequests();
        for (ShiftSwapRequest request : expiredRequests) {
            sendSwapNotifications(request, NotificationType.REQUEST_EXPIRED);
        }
        
        log.info("Đã xử lý {} expired requests", expiredRequests.size());
    }

    @Override
    public int cleanupOldRequests(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = swapRequestRepository.deleteOldRequests(cutoffDate);
        log.info("Đã xóa {} old requests (> {} ngày)", deleted, daysOld);
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
                log.error("Lỗi khi xử lý request ID {}: {}", requestId, e.getMessage());
            }
        }
        
        log.info("Đã bulk process {} requests", processed.size());
        return processed;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSwapRequests(LocalDateTime startTime, LocalDateTime endTime, String format) {
        // TODO: Implement export functionality
        throw new BusinessLogicException("Export functionality chưa được implement");
    }
}
