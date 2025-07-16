package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.GeneratedReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for GeneratedReport entity
 */
@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
    
    /**
     * Find reports by user
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.generatedBy.id = :userId " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find reports by template
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.template.id = :templateId " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findByTemplateId(@Param("templateId") Long templateId, Pageable pageable);
    
    /**
     * Find reports by status
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.status = :status " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findByStatus(@Param("status") GeneratedReport.ReportStatus status, 
                                      Pageable pageable);
    
    /**
     * Find reports by format
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.format = :format " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findByFormat(@Param("format") GeneratedReport.ReportFormat format, 
                                      Pageable pageable);
    
    /**
     * Find completed reports
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.status = 'COMPLETED' " +
           "AND g.expiresAt > CURRENT_TIMESTAMP " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findCompleted(Pageable pageable);
    
    /**
     * Find expired reports
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.expiresAt <= CURRENT_TIMESTAMP " +
           "ORDER BY g.expiresAt ASC")
    List<GeneratedReport> findExpired();
    
    /**
     * Find reports expiring soon
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.status = 'COMPLETED' " +
           "AND g.expiresAt BETWEEN CURRENT_TIMESTAMP AND :futureTime " +
           "ORDER BY g.expiresAt ASC")
    List<GeneratedReport> findExpiringSoon(@Param("futureTime") LocalDateTime futureTime);
    
    /**
     * Find reports by user and template
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.generatedBy.id = :userId " +
           "AND g.template.id = :templateId " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findByUserIdAndTemplateId(@Param("userId") Long userId,
                                                   @Param("templateId") Long templateId,
                                                   Pageable pageable);
    
    /**
     * Find reports created in date range
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findCreatedInDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);
    
    /**
     * Find scheduled reports
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.isScheduled = true " +
           "ORDER BY g.nextRunAt ASC")
    List<GeneratedReport> findScheduledReports();
    
    /**
     * Find scheduled reports due for execution
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.isScheduled = true " +
           "AND g.nextRunAt <= CURRENT_TIMESTAMP " +
           "ORDER BY g.nextRunAt ASC")
    List<GeneratedReport> findScheduledReportsDue();
    
    /**
     * Find reports by generation time range
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.generationTimeMs BETWEEN :minTime AND :maxTime " +
           "ORDER BY g.generationTimeMs DESC")
    List<GeneratedReport> findByGenerationTimeRange(@Param("minTime") Long minTime,
                                                   @Param("maxTime") Long maxTime);
    
    /**
     * Find large reports (by file size)
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.fileSize > :minSize " +
           "ORDER BY g.fileSize DESC")
    List<GeneratedReport> findLargeReports(@Param("minSize") Long minSize);
    
    /**
     * Find frequently downloaded reports
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.downloadCount > :minDownloads " +
           "ORDER BY g.downloadCount DESC")
    List<GeneratedReport> findFrequentlyDownloaded(@Param("minDownloads") Integer minDownloads);
    
    /**
     * Find reports with errors
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.status = 'FAILED' " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findWithErrors(Pageable pageable);
    
    /**
     * Get report statistics
     */
    @Query("SELECT " +
           "COUNT(g) as totalReports, " +
           "COUNT(CASE WHEN g.status = 'COMPLETED' THEN 1 END) as completedReports, " +
           "COUNT(CASE WHEN g.status = 'FAILED' THEN 1 END) as failedReports, " +
           "COUNT(CASE WHEN g.status = 'GENERATING' THEN 1 END) as generatingReports, " +
           "AVG(g.generationTimeMs) as avgGenerationTime, " +
           "SUM(g.fileSize) as totalFileSize " +
           "FROM GeneratedReport g")
    Object[] getReportStatistics();
    
    /**
     * Get report statistics by template
     */
    @Query("SELECT " +
           "g.template.templateName, " +
           "COUNT(g) as reportCount, " +
           "AVG(g.generationTimeMs) as avgGenerationTime, " +
           "SUM(g.downloadCount) as totalDownloads " +
           "FROM GeneratedReport g " +
           "GROUP BY g.template " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getReportStatisticsByTemplate();
    
    /**
     * Get report statistics by user
     */
    @Query("SELECT " +
           "g.generatedBy.fullName, " +
           "COUNT(g) as reportCount, " +
           "SUM(g.downloadCount) as totalDownloads " +
           "FROM GeneratedReport g " +
           "GROUP BY g.generatedBy " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getReportStatisticsByUser();
    
    /**
     * Get daily report generation trends
     */
    @Query("SELECT " +
           "DATE(g.createdAt) as reportDate, " +
           "COUNT(g) as reportCount, " +
           "COUNT(CASE WHEN g.status = 'COMPLETED' THEN 1 END) as successCount, " +
           "COUNT(CASE WHEN g.status = 'FAILED' THEN 1 END) as failureCount " +
           "FROM GeneratedReport g " +
           "WHERE g.createdAt >= :startDate " +
           "GROUP BY DATE(g.createdAt) " +
           "ORDER BY DATE(g.createdAt) DESC")
    List<Object[]> getDailyReportTrends(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find most popular report formats
     */
    @Query("SELECT g.format, COUNT(g) FROM GeneratedReport g " +
           "GROUP BY g.format " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getPopularFormats();
    
    /**
     * Find reports by template category
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.template.reportCategory = :category " +
           "ORDER BY g.createdAt DESC")
    Page<GeneratedReport> findByTemplateCategory(@Param("category") com.classroomapp.classroombackend.model.hrmanagement.ReportTemplate.ReportCategory category,
                                               Pageable pageable);
    
    /**
     * Find reports never downloaded
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.downloadCount = 0 " +
           "AND g.status = 'COMPLETED' " +
           "ORDER BY g.createdAt ASC")
    List<GeneratedReport> findNeverDownloaded();
    
    /**
     * Find reports with specific parameters
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.parameters LIKE CONCAT('%', :parameterKey, '%') " +
           "ORDER BY g.createdAt DESC")
    List<GeneratedReport> findWithParameter(@Param("parameterKey") String parameterKey);
    
    /**
     * Count reports by status for date range
     */
    @Query("SELECT g.status, COUNT(g) FROM GeneratedReport g " +
           "WHERE g.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY g.status")
    List<Object[]> countByStatusInDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find slowest generating reports
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.generationTimeMs IS NOT NULL " +
           "ORDER BY g.generationTimeMs DESC")
    List<GeneratedReport> findSlowestReports(Pageable pageable);
    
    /**
     * Find fastest generating reports
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.generationTimeMs IS NOT NULL " +
           "AND g.generationTimeMs > 0 " +
           "ORDER BY g.generationTimeMs ASC")
    List<GeneratedReport> findFastestReports(Pageable pageable);
    
    /**
     * Find reports by record count range
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.recordCount BETWEEN :minRecords AND :maxRecords " +
           "ORDER BY g.recordCount DESC")
    List<GeneratedReport> findByRecordCountRange(@Param("minRecords") Integer minRecords,
                                               @Param("maxRecords") Integer maxRecords);
    
    /**
     * Find reports with no data
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.recordCount = 0 " +
           "OR g.recordCount IS NULL " +
           "ORDER BY g.createdAt DESC")
    List<GeneratedReport> findWithNoData();
    
    /**
     * Delete old reports
     */
    @Query("DELETE FROM GeneratedReport g WHERE g.expiresAt < :cutoffDate")
    void deleteExpiredBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find reports for cleanup (old and not downloaded)
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.createdAt < :cutoffDate " +
           "AND g.downloadCount = 0 " +
           "ORDER BY g.createdAt ASC")
    List<GeneratedReport> findForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get storage usage statistics
     */
    @Query("SELECT " +
           "COUNT(g) as totalReports, " +
           "SUM(g.fileSize) as totalSize, " +
           "AVG(g.fileSize) as avgSize, " +
           "MAX(g.fileSize) as maxSize " +
           "FROM GeneratedReport g " +
           "WHERE g.fileSize IS NOT NULL")
    Object[] getStorageStatistics();
    
    /**
     * Find reports by file path pattern (for migration)
     */
    @Query("SELECT g FROM GeneratedReport g WHERE g.filePath LIKE :pathPattern " +
           "ORDER BY g.createdAt DESC")
    List<GeneratedReport> findByFilePathPattern(@Param("pathPattern") String pathPattern);
}
