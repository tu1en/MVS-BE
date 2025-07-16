package com.classroomapp.classroombackend.repository.administration;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.administration.AuditLog;

/**
 * Repository for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find logs by user
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find logs by username
     */
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUsername(@Param("username") String username, Pageable pageable);
    
    /**
     * Find logs by action
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.timestamp DESC")
    Page<AuditLog> findByAction(@Param("action") AuditLog.AuditAction action, Pageable pageable);
    
    /**
     * Find logs by category
     */
    @Query("SELECT a FROM AuditLog a WHERE a.category = :category ORDER BY a.timestamp DESC")
    Page<AuditLog> findByCategory(@Param("category") AuditLog.AuditCategory category, Pageable pageable);
    
    /**
     * Find logs by severity
     */
    @Query("SELECT a FROM AuditLog a WHERE a.severity = :severity ORDER BY a.timestamp DESC")
    Page<AuditLog> findBySeverity(@Param("severity") AuditLog.AuditSeverity severity, Pageable pageable);
    
    /**
     * Find logs in date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findInDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);
    
    /**
     * Find security-related logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.category IN ('SECURITY', 'AUTHENTICATION', 'AUTHORIZATION') " +
           "OR a.action IN ('LOGIN_FAILED', 'SECURITY_VIOLATION') " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findSecurityLogs(Pageable pageable);
    
    /**
     * Find failed operations
     */
    @Query("SELECT a FROM AuditLog a WHERE a.success = false ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailedOperations(Pageable pageable);
    
    /**
     * Find logs by entity
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType " +
           "AND (:entityId IS NULL OR a.entityId = :entityId) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByEntity(@Param("entityType") String entityType,
                               @Param("entityId") String entityId,
                               Pageable pageable);
    
    /**
     * Find logs by IP address
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress ORDER BY a.timestamp DESC")
    Page<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);
    
    /**
     * Find logs by session
     */
    @Query("SELECT a FROM AuditLog a WHERE a.sessionId = :sessionId ORDER BY a.timestamp DESC")
    List<AuditLog> findBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Find recent logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    /**
     * Search logs
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchLogs(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Count logs by action
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY a.action " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countLogsByAction(@Param("since") LocalDateTime since);
    
    /**
     * Count logs by category
     */
    @Query("SELECT a.category, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY a.category " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countLogsByCategory(@Param("since") LocalDateTime since);
    
    /**
     * Count logs by severity
     */
    @Query("SELECT a.severity, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY a.severity " +
           "ORDER BY a.severity ASC")
    List<Object[]> countLogsBySeverity(@Param("since") LocalDateTime since);
    
    /**
     * Get daily log counts
     */
    @Query("SELECT DATE(a.timestamp) as logDate, COUNT(a) as logCount " +
           "FROM AuditLog a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY DATE(a.timestamp) " +
           "ORDER BY DATE(a.timestamp) DESC")
    List<Object[]> getDailyLogCounts(@Param("since") LocalDateTime since);
    
    /**
     * Get hourly log counts for today
     */
    @Query("SELECT HOUR(a.timestamp) as logHour, COUNT(a) as logCount " +
           "FROM AuditLog a " +
           "WHERE a.timestamp >= CURRENT_DATE AND a.timestamp < DATEADD(day, 1, CURRENT_DATE) " +
           "GROUP BY HOUR(a.timestamp) " +
           "ORDER BY HOUR(a.timestamp) ASC")
    List<Object[]> getHourlyLogCounts();
    
    /**
     * Find most active users
     */
    @Query("SELECT a.username, COUNT(a) as activityCount " +
           "FROM AuditLog a " +
           "WHERE a.timestamp >= :since AND a.username IS NOT NULL " +
           "GROUP BY a.username " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);
    
    /**
     * Find most accessed entities
     */
    @Query("SELECT a.entityType, COUNT(a) as accessCount " +
           "FROM AuditLog a " +
           "WHERE a.timestamp >= :since AND a.entityType IS NOT NULL " +
           "GROUP BY a.entityType " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findMostAccessedEntities(@Param("since") LocalDateTime since);
    
    /**
     * Get error statistics
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN a.success = false THEN 1 END) as errorCount, " +
           "COUNT(CASE WHEN a.severity = 'ERROR' THEN 1 END) as errorSeverityCount, " +
           "COUNT(CASE WHEN a.severity = 'FATAL' THEN 1 END) as fatalCount, " +
           "COUNT(a) as totalCount " +
           "FROM AuditLog a " +
           "WHERE a.timestamp >= :since")
    Object[] getErrorStatistics(@Param("since") LocalDateTime since);
    
    /**
     * Find suspicious activities
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(a.action = 'LOGIN_FAILED' AND a.timestamp >= :since) OR " +
           "(a.action = 'SECURITY_VIOLATION') OR " +
           "(a.severity = 'FATAL') " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findSuspiciousActivities(@Param("since") LocalDateTime since);
    
    /**
     * Delete old logs
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count logs older than date
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    Long countOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get audit statistics
     */
    @Query("SELECT " +
           "COUNT(a) as totalLogs, " +
           "COUNT(CASE WHEN a.success = true THEN 1 END) as successfulLogs, " +
           "COUNT(CASE WHEN a.success = false THEN 1 END) as failedLogs, " +
           "COUNT(DISTINCT a.username) as uniqueUsers, " +
           "COUNT(DISTINCT a.ipAddress) as uniqueIPs " +
           "FROM AuditLog a " +
           "WHERE a.timestamp >= :since")
    Object[] getAuditStatistics(@Param("since") LocalDateTime since);
}
