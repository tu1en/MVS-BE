package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {

    List<ShiftSwapRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<ShiftSwapRequest> findByTargetEmployeeIdOrderByCreatedAtDesc(Long targetEmployeeId);

    List<ShiftSwapRequest> findByStatusOrderByCreatedAtDesc(ShiftSwapRequest.SwapStatus status);

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "ssr.targetEmployee.id = :targetEmployeeId AND " +
           "ssr.status = 'PENDING' AND " +
           "ssr.expiryTime > CURRENT_TIMESTAMP " +
           "ORDER BY ssr.priority DESC, ssr.createdAt ASC")
    List<ShiftSwapRequest> findPendingRequestsForTarget(@Param("targetEmployeeId") Long targetEmployeeId);

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "ssr.status = 'ACCEPTED_BY_TARGET' AND " +
           "ssr.expiryTime > CURRENT_TIMESTAMP " +
           "ORDER BY ssr.priority DESC, ssr.isEmergency DESC, ssr.createdAt ASC")
    List<ShiftSwapRequest> findPendingManagerApproval();

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "ssr.status IN ('PENDING', 'ACCEPTED_BY_TARGET') AND " +
           "ssr.expiryTime <= CURRENT_TIMESTAMP")
    List<ShiftSwapRequest> findExpiredRequests();

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "ssr.isEmergency = true AND " +
           "ssr.status IN ('PENDING', 'ACCEPTED_BY_TARGET') " +
           "ORDER BY ssr.createdAt ASC")
    List<ShiftSwapRequest> findEmergencyRequests();

    List<ShiftSwapRequest> findByPriorityAndStatusInOrderByCreatedAtAsc(
            ShiftSwapRequest.Priority priority,
            List<ShiftSwapRequest.SwapStatus> statuses);

    @Query("SELECT ssr FROM ShiftSwapRequest ssr " +
           "JOIN ssr.requester r " +
           "JOIN ssr.targetEmployee te WHERE " +
           "(:requesterId IS NULL OR ssr.requester.id = :requesterId) AND " +
           "(:targetEmployeeId IS NULL OR ssr.targetEmployee.id = :targetEmployeeId) AND " +
           "(:status IS NULL OR ssr.status = :status) AND " +
           "(:priority IS NULL OR ssr.priority = :priority) AND " +
           "(:isEmergency IS NULL OR ssr.isEmergency = :isEmergency) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(te.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(ssr.requestReason) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY ssr.priority DESC, ssr.isEmergency DESC, ssr.createdAt DESC")
    Page<ShiftSwapRequest> searchRequests(@Param("requesterId") Long requesterId,
                                         @Param("targetEmployeeId") Long targetEmployeeId,
                                         @Param("status") ShiftSwapRequest.SwapStatus status,
                                         @Param("priority") ShiftSwapRequest.Priority priority,
                                         @Param("isEmergency") Boolean isEmergency,
                                         @Param("search") String search,
                                         Pageable pageable);

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "(ssr.requesterAssignment.id = :assignmentId OR ssr.targetAssignment.id = :assignmentId) " +
           "ORDER BY ssr.createdAt DESC")
    List<ShiftSwapRequest> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(ssr) > 0 FROM ShiftSwapRequest ssr WHERE " +
           "(ssr.requesterAssignment.id = :assignmentId OR ssr.targetAssignment.id = :assignmentId) AND " +
           "ssr.status IN ('PENDING', 'ACCEPTED_BY_TARGET')")
    boolean existsPendingRequestForAssignment(@Param("assignmentId") Long assignmentId);

    @Query("SELECT ssr.status, COUNT(ssr) FROM ShiftSwapRequest ssr " +
           "GROUP BY ssr.status")
    List<Object[]> countByStatus();

    @Query("SELECT ssr.priority, COUNT(ssr) FROM ShiftSwapRequest ssr " +
           "WHERE ssr.status IN ('PENDING', 'ACCEPTED_BY_TARGET') " +
           "GROUP BY ssr.priority")
    List<Object[]> countByPriority();

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "ssr.status IN ('PENDING', 'ACCEPTED_BY_TARGET') AND " +
           "ssr.expiryTime BETWEEN CURRENT_TIMESTAMP AND :notificationTime " +
           "ORDER BY ssr.expiryTime ASC")
    List<ShiftSwapRequest> findRequestsNeedingNotification(@Param("notificationTime") LocalDateTime notificationTime);

    @Query("SELECT ssr FROM ShiftSwapRequest ssr WHERE " +
           "ssr.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY ssr.createdAt DESC")
    List<ShiftSwapRequest> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    List<ShiftSwapRequest> findByApprovedByIdOrderByUpdatedAtDesc(Long managerId);

    @Query("SELECT " +
           "COUNT(CASE WHEN ssr.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN ssr.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "COUNT(CASE WHEN ssr.status = 'REJECTED_BY_TARGET' THEN 1 END) as rejectedByTargetCount, " +
           "COUNT(ssr) as totalCount " +
           "FROM ShiftSwapRequest ssr WHERE " +
           "ssr.createdAt BETWEEN :startTime AND :endTime")
    Object[] getApprovalStatistics(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT ssr.requester, COUNT(ssr) as requestCount FROM ShiftSwapRequest ssr WHERE " +
           "ssr.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY ssr.requester " +
           "ORDER BY requestCount DESC")
    List<Object[]> findTopRequesters(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);

    @Modifying
    @Query("UPDATE ShiftSwapRequest ssr SET ssr.status = 'EXPIRED', ssr.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ssr.status IN ('PENDING', 'ACCEPTED_BY_TARGET') AND ssr.expiryTime <= CURRENT_TIMESTAMP")
    int markExpiredRequests();

    @Modifying
    @Query("DELETE FROM ShiftSwapRequest ssr WHERE " +
           "ssr.status IN ('APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED') AND " +
           "ssr.updatedAt < :cutoffDate")
    int deleteOldRequests(@Param("cutoffDate") LocalDateTime cutoffDate);
}
