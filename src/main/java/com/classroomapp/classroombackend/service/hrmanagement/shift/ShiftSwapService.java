package com.classroomapp.classroombackend.service.hrmanagement.shift;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Shift Swap Request management
 * Cung cáº¥p business logic cho quáº£n lÃ½ yÃªu cáº§u Ä‘á»•i ca
 */
public interface ShiftSwapService {

    /**
     * Táº¡o swap request má»›i
     */
    ShiftSwapRequest createSwapRequest(ShiftSwapRequest request);

    /**
     * Cáº­p nháº­t swap request
     */
    ShiftSwapRequest updateSwapRequest(Long id, ShiftSwapRequest request);

    /**
     * Há»§y swap request
     */
    void cancelSwapRequest(Long id, String reason);

    /**
     * TÃ¬m swap request theo ID
     */
    Optional<ShiftSwapRequest> findById(Long id);

    /**
     * TÃ¬m requests theo requester
     */
    List<ShiftSwapRequest> findByRequester(Long requesterId);

    /**
     * TÃ¬m requests theo target employee
     */
    List<ShiftSwapRequest> findByTargetEmployee(Long targetEmployeeId);

    /**
     * TÃ¬m requests Ä‘ang chá» pháº£n há»“i tá»« target
     */
    List<ShiftSwapRequest> findPendingRequestsForTarget(Long targetEmployeeId);

    /**
     * TÃ¬m requests Ä‘ang chá» phÃª duyá»‡t tá»« manager
     */
    List<ShiftSwapRequest> findPendingManagerApproval();

    /**
     * TÃ¬m requests kháº©n cáº¥p
     */
    List<ShiftSwapRequest> findEmergencyRequests();

    /**
     * Search requests vá»›i filters
     */
    Page<ShiftSwapRequest> searchRequests(Long requesterId, Long targetEmployeeId,
                                         ShiftSwapRequest.SwapStatus status,
                                         ShiftSwapRequest.Priority priority,
                                         Boolean isEmergency, String search,
                                         Pageable pageable);

    /**
     * Pháº£n há»“i tá»« target employee
     */
    ShiftSwapRequest respondByTarget(Long requestId, ShiftSwapRequest.TargetResponse response, 
                                    String reason, User targetEmployee);

    /**
     * PhÃª duyá»‡t tá»« manager
     */
    ShiftSwapRequest approveByManager(Long requestId, ShiftSwapRequest.ManagerResponse response,
                                     String reason, User manager);

    /**
     * Validate swap request trÆ°á»›c khi táº¡o
     */
    void validateSwapRequest(ShiftSwapRequest request);

    /**
     * Kiá»ƒm tra xung Ä‘á»™t cho swap request
     */
    ShiftConflictDetectionService.ConflictCheckResult checkSwapConflicts(ShiftSwapRequest request);

    /**
     * Thá»±c hiá»‡n swap assignments sau khi Ä‘Æ°á»£c phÃª duyá»‡t
     */
    void executeSwap(Long requestId);

    /**
     * TÃ¬m requests Ä‘Ã£ háº¿t háº¡n
     */
    List<ShiftSwapRequest> findExpiredRequests();

    /**
     * ÄÃ¡nh dáº¥u requests háº¿t háº¡n
     */
    int markExpiredRequests();

    /**
     * TÃ¬m requests cáº§n notification (sáº¯p háº¿t háº¡n)
     */
    List<ShiftSwapRequest> findRequestsNeedingNotification(int hoursBeforeExpiry);

    /**
     * Gá»­i notifications cho swap requests
     */
    void sendSwapNotifications(ShiftSwapRequest request, NotificationType type);

    /**
     * Láº¥y thá»‘ng kÃª swap requests
     */
    SwapStatistics getSwapStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * TÃ¬m top requesters
     */
    List<Object[]> findTopRequesters(LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * Auto-expire old requests
     */
    void processExpiredRequests();

    /**
     * Cleanup old completed requests
     */
    int cleanupOldRequests(int daysOld);

    /**
     * Suggest alternative swap partners
     */
    List<SwapSuggestion> suggestSwapPartners(Long assignmentId);

    /**
     * Bulk approve/reject requests
     */
    List<ShiftSwapRequest> bulkProcessRequests(List<Long> requestIds, 
                                              ShiftSwapRequest.ManagerResponse response,
                                              String reason, User manager);

    /**
     * Export swap requests to CSV/Excel
     */
    byte[] exportSwapRequests(LocalDateTime startTime, LocalDateTime endTime, String format);

    /**
     * Enum cho notification types
     */
    enum NotificationType {
        REQUEST_CREATED("YÃªu cáº§u Ä‘á»•i ca má»›i"),
        REQUEST_ACCEPTED("YÃªu cáº§u Ä‘Æ°á»£c cháº¥p nháº­n"),
        REQUEST_REJECTED("YÃªu cáº§u bá»‹ tá»« chá»‘i"),
        REQUEST_APPROVED("YÃªu cáº§u Ä‘Æ°á»£c phÃª duyá»‡t"),
        REQUEST_CANCELLED("YÃªu cáº§u bá»‹ há»§y"),
        REQUEST_EXPIRING("YÃªu cáº§u sáº¯p háº¿t háº¡n"),
        REQUEST_EXPIRED("YÃªu cáº§u Ä‘Ã£ háº¿t háº¡n");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    /**
     * DTO cho swap statistics
     */
    class SwapStatistics {
        private long totalRequests;
        private long pendingRequests;
        private long approvedRequests;
        private long rejectedRequests;
        private long expiredRequests;
        private double approvalRate;
        private double averageResponseTime; // in hours

        // Constructors, getters, setters
        public SwapStatistics() {}

        public SwapStatistics(long totalRequests, long pendingRequests, long approvedRequests,
                             long rejectedRequests, long expiredRequests) {
            this.totalRequests = totalRequests;
            this.pendingRequests = pendingRequests;
            this.approvedRequests = approvedRequests;
            this.rejectedRequests = rejectedRequests;
            this.expiredRequests = expiredRequests;
            
            long processedRequests = approvedRequests + rejectedRequests;
            this.approvalRate = processedRequests > 0 ? 
                (double) approvedRequests / processedRequests * 100 : 0;
        }

        // Getters and setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

        public long getPendingRequests() { return pendingRequests; }
        public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }

        public long getApprovedRequests() { return approvedRequests; }
        public void setApprovedRequests(long approvedRequests) { this.approvedRequests = approvedRequests; }

        public long getRejectedRequests() { return rejectedRequests; }
        public void setRejectedRequests(long rejectedRequests) { this.rejectedRequests = rejectedRequests; }

        public long getExpiredRequests() { return expiredRequests; }
        public void setExpiredRequests(long expiredRequests) { this.expiredRequests = expiredRequests; }

        public double getApprovalRate() { return approvalRate; }
        public void setApprovalRate(double approvalRate) { this.approvalRate = approvalRate; }

        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
    }

    /**
     * DTO cho swap suggestions
     */
    class SwapSuggestion {
        private Long assignmentId;
        private String employeeName;
        private String shiftTemplateName;
        private java.time.LocalDate assignmentDate;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private double compatibilityScore;
        private String reason;

        // Constructors, getters, setters
        public SwapSuggestion() {}

        public SwapSuggestion(Long assignmentId, String employeeName, String shiftTemplateName,
                             java.time.LocalDate assignmentDate, java.time.LocalTime startTime,
                             java.time.LocalTime endTime, double compatibilityScore, String reason) {
            this.assignmentId = assignmentId;
            this.employeeName = employeeName;
            this.shiftTemplateName = shiftTemplateName;
            this.assignmentDate = assignmentDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.compatibilityScore = compatibilityScore;
            this.reason = reason;
        }

        // Getters and setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getShiftTemplateName() { return shiftTemplateName; }
        public void setShiftTemplateName(String shiftTemplateName) { this.shiftTemplateName = shiftTemplateName; }

        public java.time.LocalDate getAssignmentDate() { return assignmentDate; }
        public void setAssignmentDate(java.time.LocalDate assignmentDate) { this.assignmentDate = assignmentDate; }

        public java.time.LocalTime getStartTime() { return startTime; }
        public void setStartTime(java.time.LocalTime startTime) { this.startTime = startTime; }

        public java.time.LocalTime getEndTime() { return endTime; }
        public void setEndTime(java.time.LocalTime endTime) { this.endTime = endTime; }

        public double getCompatibilityScore() { return compatibilityScore; }
        public void setCompatibilityScore(double compatibilityScore) { this.compatibilityScore = compatibilityScore; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
