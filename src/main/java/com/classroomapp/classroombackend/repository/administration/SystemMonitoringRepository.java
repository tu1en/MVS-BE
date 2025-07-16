package com.classroomapp.classroombackend.repository.administration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.administration.SystemMonitoring;

/**
 * Repository for SystemMonitoring entity
 */
@Repository
public interface SystemMonitoringRepository extends JpaRepository<SystemMonitoring, Long> {
    
    /**
     * Find metrics by category
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.category = :category ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByCategoryOrderByTimestampDesc(@Param("category") SystemMonitoring.MonitoringCategory category);
    
    /**
     * Find metrics by status
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.status = :status ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByStatus(@Param("status") SystemMonitoring.MetricStatus status);
    
    /**
     * Find latest metrics (top 10)
     */
    @Query("SELECT m FROM SystemMonitoring m ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findTop10ByOrderByTimestampDesc();
    
    /**
     * Find metrics by metric name
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricName = :metricName ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByMetricNameOrderByTimestampDesc(@Param("metricName") String metricName);
    
    /**
     * Find metrics by host
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.hostName = :hostName ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByHostNameOrderByTimestampDesc(@Param("hostName") String hostName);
    
    /**
     * Find metrics by instance
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.instanceId = :instanceId ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByInstanceIdOrderByTimestampDesc(@Param("instanceId") String instanceId);
    
    /**
     * Find metrics in time range
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByTimestampBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find metrics since timestamp
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByTimestampAfter(@Param("since") LocalDateTime since);
    
    /**
     * Find critical metrics
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.status = 'CRITICAL' ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findCriticalMetrics();
    
    /**
     * Find warning metrics
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.status = 'WARNING' ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findWarningMetrics();
    
    /**
     * Find metrics above threshold
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricValue > :threshold ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByMetricValueGreaterThan(@Param("threshold") Double threshold);
    
    /**
     * Find metrics below threshold
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricValue < :threshold ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByMetricValueLessThan(@Param("threshold") Double threshold);
    
    /**
     * Find metrics in value range
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricValue BETWEEN :minValue AND :maxValue ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByMetricValueBetween(@Param("minValue") Double minValue, @Param("maxValue") Double maxValue);
    
    /**
     * Find latest metric by name
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricName = :metricName ORDER BY m.timestamp DESC LIMIT 1")
    Optional<SystemMonitoring> findLatestByMetricName(@Param("metricName") String metricName);
    
    /**
     * Find latest metric by category
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.category = :category ORDER BY m.timestamp DESC LIMIT 1")
    Optional<SystemMonitoring> findLatestByCategory(@Param("category") SystemMonitoring.MonitoringCategory category);
    
    /**
     * Find metrics with tags
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.tags IS NOT NULL AND m.tags LIKE %:tag% ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * Find metrics by category and status
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.category = :category AND m.status = :status ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByCategoryAndStatus(@Param("category") SystemMonitoring.MonitoringCategory category,
                                                   @Param("status") SystemMonitoring.MetricStatus status);
    
    /**
     * Find metrics by metric name and time range
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricName = :metricName AND m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp ASC")
    List<SystemMonitoring> findByMetricNameAndTimestampBetween(@Param("metricName") String metricName, 
                                                              @Param("startTime") LocalDateTime startTime, 
                                                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * Count metrics by status
     */
    @Query("SELECT COUNT(m) FROM SystemMonitoring m WHERE m.status = :status")
    long countByStatus(@Param("status") SystemMonitoring.MetricStatus status);
    
    /**
     * Count metrics by category
     */
    @Query("SELECT m.category, COUNT(m) FROM SystemMonitoring m GROUP BY m.category")
    List<Object[]> countMetricsByCategory();
    
    /**
     * Count metrics by status
     */
    @Query("SELECT m.status, COUNT(m) FROM SystemMonitoring m GROUP BY m.status")
    List<Object[]> countMetricsByStatus();
    
    /**
     * Count metrics by host
     */
    @Query("SELECT m.hostName, COUNT(m) FROM SystemMonitoring m WHERE m.hostName IS NOT NULL GROUP BY m.hostName")
    List<Object[]> countMetricsByHost();
    
    /**
     * Get average metric value by name
     */
    @Query("SELECT AVG(m.metricValue) FROM SystemMonitoring m WHERE m.metricName = :metricName AND m.timestamp >= :since")
    Double getAverageMetricValue(@Param("metricName") String metricName, @Param("since") LocalDateTime since);
    
    /**
     * Get max metric value by name
     */
    @Query("SELECT MAX(m.metricValue) FROM SystemMonitoring m WHERE m.metricName = :metricName AND m.timestamp >= :since")
    Double getMaxMetricValue(@Param("metricName") String metricName, @Param("since") LocalDateTime since);
    
    /**
     * Get min metric value by name
     */
    @Query("SELECT MIN(m.metricValue) FROM SystemMonitoring m WHERE m.metricName = :metricName AND m.timestamp >= :since")
    Double getMinMetricValue(@Param("metricName") String metricName, @Param("since") LocalDateTime since);
    
    /**
     * Find metrics exceeding warning threshold
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.thresholdWarning IS NOT NULL AND m.metricValue > m.thresholdWarning ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findMetricsExceedingWarningThreshold();
    
    /**
     * Find metrics exceeding critical threshold
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.thresholdCritical IS NOT NULL AND m.metricValue > m.thresholdCritical ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findMetricsExceedingCriticalThreshold();
    
    /**
     * Find unique metric names
     */
    @Query("SELECT DISTINCT m.metricName FROM SystemMonitoring m ORDER BY m.metricName")
    List<String> findDistinctMetricNames();
    
    /**
     * Find unique categories
     */
    @Query("SELECT DISTINCT m.category FROM SystemMonitoring m ORDER BY m.category")
    List<SystemMonitoring.MonitoringCategory> findDistinctCategories();
    
    /**
     * Find unique hosts
     */
    @Query("SELECT DISTINCT m.hostName FROM SystemMonitoring m WHERE m.hostName IS NOT NULL ORDER BY m.hostName")
    List<String> findDistinctHosts();
    
    /**
     * Delete old metrics
     */
    @Query("DELETE FROM SystemMonitoring m WHERE m.timestamp < :cutoffDate")
    void deleteOldMetrics(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count total metrics
     */
    @Query("SELECT COUNT(m) FROM SystemMonitoring m")
    long count();
    
    /**
     * Find metrics with description
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.description IS NOT NULL AND m.description LIKE %:keyword% ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * Find recent metrics (last hour)
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.timestamp >= :oneHourAgo ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findRecentMetrics(@Param("oneHourAgo") LocalDateTime oneHourAgo);
    
    /**
     * Get monitoring statistics
     */
    @Query("SELECT " +
           "COUNT(m) as totalMetrics, " +
           "COUNT(CASE WHEN m.status = 'NORMAL' THEN 1 END) as normalCount, " +
           "COUNT(CASE WHEN m.status = 'WARNING' THEN 1 END) as warningCount, " +
           "COUNT(CASE WHEN m.status = 'CRITICAL' THEN 1 END) as criticalCount, " +
           "COUNT(CASE WHEN m.status = 'UNKNOWN' THEN 1 END) as unknownCount " +
           "FROM SystemMonitoring m")
    Object[] getMonitoringStatistics();
    
    /**
     * Find metrics by unit
     */
    @Query("SELECT m FROM SystemMonitoring m WHERE m.metricUnit = :unit ORDER BY m.timestamp DESC")
    List<SystemMonitoring> findByMetricUnit(@Param("unit") String unit);
}
