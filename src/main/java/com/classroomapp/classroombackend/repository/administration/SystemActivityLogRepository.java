package com.classroomapp.classroombackend.repository.administration;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.SystemActivityLog;

/**
 * Repository for SystemActivityLog entity
 */
@Repository
public interface SystemActivityLogRepository extends JpaRepository<SystemActivityLog, Long> {
    
    /**
     * Find logs by user ID with pagination
     */
    Page<SystemActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find logs by username with pagination
     */
    Page<SystemActivityLog> findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(String username, Pageable pageable);
    
    /**
     * Find logs by action with pagination
     */
    Page<SystemActivityLog> findByActionContainingIgnoreCaseOrderByCreatedAtDesc(String action, Pageable pageable);
    
    /**
     * Find logs by date range
     */
    Page<SystemActivityLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find logs by log level
     */
    Page<SystemActivityLog> findByLogLevelOrderByCreatedAtDesc(
            SystemActivityLog.LogLevel logLevel, Pageable pageable);
    
    /**
     * Find logs by success status
     */
    Page<SystemActivityLog> findBySuccessOrderByCreatedAtDesc(Boolean success, Pageable pageable);
    
    /**
     * Count logs by date range
     */
    @Query("SELECT COUNT(l) FROM SystemActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get recent failed activities
     */
    @Query("SELECT l FROM SystemActivityLog l WHERE l.success = false ORDER BY l.createdAt DESC")
    List<SystemActivityLog> findRecentFailedActivities(Pageable pageable);
    
    /**
     * Get activity statistics by action
     */
    @Query("SELECT l.action, COUNT(l) FROM SystemActivityLog l GROUP BY l.action ORDER BY COUNT(l) DESC")
    List<Object[]> getActivityStatistics();
}