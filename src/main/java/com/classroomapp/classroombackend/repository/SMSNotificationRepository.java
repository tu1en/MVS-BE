package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.SMSNotification;
import com.classroomapp.classroombackend.model.SMSStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SMSNotificationRepository extends JpaRepository<SMSNotification, Long> {
    
    /**
     * Find SMS notifications by status
     */
    List<SMSNotification> findByStatus(SMSStatus status);
    
    /**
     * Find SMS notifications for a specific student
     */
    List<SMSNotification> findByStudentIdOrderBySendTimeDesc(Long studentId);
    
    /**
     * Find SMS notifications for a specific attendance session
     */
    List<SMSNotification> findByAttendanceSessionId(Long attendanceSessionId);
    
    /**
     * Find pending or retry SMS notifications that are ready to be sent
     */
    @Query("SELECT s FROM SMSNotification s WHERE (s.status = 'PENDING' OR s.status = 'RETRY') " +
           "AND s.sendTime <= :currentTime ORDER BY s.sendTime ASC")
    List<SMSNotification> findReadyToSend(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find failed SMS notifications that can be retried
     */
    @Query("SELECT s FROM SMSNotification s WHERE s.status = 'FAILED' " +
           "AND s.retryCount < :maxRetries " +
           "AND (s.lastRetryTime IS NULL OR s.lastRetryTime <= :retryAfterTime)")
    List<SMSNotification> findFailedForRetry(@Param("maxRetries") int maxRetries, 
                                           @Param("retryAfterTime") LocalDateTime retryAfterTime);
    
    /**
     * Find SMS notifications by date range
     */
    @Query("SELECT s FROM SMSNotification s WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY s.createdAt DESC")
    List<SMSNotification> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count SMS notifications by status
     */
    Long countByStatus(SMSStatus status);
    
    /**
     * Count SMS notifications sent today
     */
    @Query("SELECT COUNT(s) FROM SMSNotification s WHERE s.status = 'SENT' " +
           "AND DATE(s.updatedAt) = DATE(CURRENT_DATE)")
    Long countSentToday();
    
    /**
     * Get SMS statistics for dashboard
     */
    @Query("SELECT s.status, COUNT(s) FROM SMSNotification s " +
           "WHERE s.createdAt >= :fromDate GROUP BY s.status")
    List<Object[]> getStatusStatistics(@Param("fromDate") LocalDateTime fromDate);
    
    /**
     * Check if SMS notification already exists for student and attendance session
     */
    boolean existsByStudentIdAndAttendanceSessionId(Long studentId, Long attendanceSessionId);
    
    /**
     * Delete old SMS notifications (for cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime beforeDate);
}