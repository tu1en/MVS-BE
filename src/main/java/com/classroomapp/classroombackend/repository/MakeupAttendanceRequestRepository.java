package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest;
import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest.RequestStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Repository interface for MakeupAttendanceRequest entity
 */
@Repository
public interface MakeupAttendanceRequestRepository extends JpaRepository<MakeupAttendanceRequest, Long> {
    
    /**
     * Find all requests by teacher
     */
    List<MakeupAttendanceRequest> findByTeacherOrderByRequestedAtDesc(User teacher);
    
    /**
     * Find all requests by teacher with pagination
     */
    Page<MakeupAttendanceRequest> findByTeacherOrderByRequestedAtDesc(User teacher, Pageable pageable);
    
    /**
     * Find all requests by status
     */
    List<MakeupAttendanceRequest> findByStatusOrderByRequestedAtDesc(RequestStatus status);
    
    /**
     * Find all requests by status with pagination
     */
    Page<MakeupAttendanceRequest> findByStatusOrderByRequestedAtDesc(RequestStatus status, Pageable pageable);
    
    /**
     * Find all pending requests (for manager approval)
     */
    List<MakeupAttendanceRequest> findByStatusOrderByRequestedAtAsc(RequestStatus status);
    
    /**
     * Find requests by teacher and status
     */
    List<MakeupAttendanceRequest> findByTeacherAndStatusOrderByRequestedAtDesc(User teacher, RequestStatus status);
    
    /**
     * Find acknowledged requests for a specific teacher that haven't been completed yet
     */
    @Query("SELECT mar FROM MakeupAttendanceRequest mar WHERE mar.teacher = :teacher AND mar.status = 'ACKNOWLEDGED' ORDER BY mar.requestedAt DESC")
    List<MakeupAttendanceRequest> findAcknowledgedRequestsByTeacher(@Param("teacher") User teacher);
    
    /**
     * Check if there's already a pending or acknowledged request for the same lecture by the same teacher
     */
    @Query("SELECT mar FROM MakeupAttendanceRequest mar WHERE mar.teacher = :teacher AND mar.lecture.id = :lectureId AND mar.status IN ('PENDING', 'ACKNOWLEDGED')")
    Optional<MakeupAttendanceRequest> findExistingRequestForLecture(@Param("teacher") User teacher, @Param("lectureId") Long lectureId);
    
    /**
     * Find requests by date range
     */
    @Query("SELECT mar FROM MakeupAttendanceRequest mar WHERE mar.requestedAt BETWEEN :startDate AND :endDate ORDER BY mar.requestedAt DESC")
    List<MakeupAttendanceRequest> findByRequestedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count pending requests
     */
    long countByStatus(RequestStatus status);
    
    /**
     * Count requests by teacher and status
     */
    long countByTeacherAndStatus(User teacher, RequestStatus status);
    
    /**
     * Find requests that need to be auto-expired (older than X days and still pending)
     */
    @Query("SELECT mar FROM MakeupAttendanceRequest mar WHERE mar.status = 'PENDING' AND mar.requestedAt < :cutoffDate")
    List<MakeupAttendanceRequest> findExpiredPendingRequests(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get statistics for a teacher
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN mar.status = 'PENDING' THEN 1 END) as pending, " +
           "COUNT(CASE WHEN mar.status = 'ACKNOWLEDGED' THEN 1 END) as acknowledged, " +
           "COUNT(CASE WHEN mar.status = 'COMPLETED' THEN 1 END) as completed " +
           "FROM MakeupAttendanceRequest mar WHERE mar.teacher = :teacher")
    Object[] getTeacherStatistics(@Param("teacher") User teacher);

    /**
     * Get overall statistics
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN mar.status = 'PENDING' THEN 1 END) as pending, " +
           "COUNT(CASE WHEN mar.status = 'ACKNOWLEDGED' THEN 1 END) as acknowledged, " +
           "COUNT(CASE WHEN mar.status = 'COMPLETED' THEN 1 END) as completed " +
           "FROM MakeupAttendanceRequest mar")
    Object[] getOverallStatistics();
    
    /**
     * Find recent requests for dashboard (last 30 days)
     */
    @Query("SELECT mar FROM MakeupAttendanceRequest mar WHERE mar.requestedAt >= :thirtyDaysAgo ORDER BY mar.requestedAt DESC")
    List<MakeupAttendanceRequest> findRecentRequests(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}
