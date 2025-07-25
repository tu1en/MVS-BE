package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ExplanationEvidence entity
 */
@Repository
public interface ExplanationEvidenceRepository extends JpaRepository<ExplanationEvidence, Long> {
    
    /**
     * Find evidence files by explanation ID
     * @param explanationId the explanation ID
     * @return list of evidence files
     */
    List<ExplanationEvidence> findByExplanationIdOrderByCreatedAtAsc(Long explanationId);
    
    /**
     * Find evidence files by evidence type
     * @param evidenceType the evidence type
     * @return list of evidence files
     */
    List<ExplanationEvidence> findByEvidenceTypeOrderByCreatedAtDesc(ExplanationEvidence.EvidenceType evidenceType);
    
    /**
     * Find verified evidence files
     * @param isVerified verification status
     * @return list of evidence files
     */
    List<ExplanationEvidence> findByIsVerifiedOrderByCreatedAtDesc(Boolean isVerified);
    
    /**
     * Find evidence files by file type
     * @param fileType the file type
     * @return list of evidence files
     */
    List<ExplanationEvidence> findByFileTypeOrderByCreatedAtDesc(String fileType);
    
    /**
     * Find evidence files by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of evidence files in date range
     */
    List<ExplanationEvidence> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find evidence files verified by specific user
     * @param verifiedBy the user ID who verified
     * @return list of verified evidence files
     */
    List<ExplanationEvidence> findByVerifiedByOrderByVerifiedAtDesc(Long verifiedBy);
    
    /**
     * Find large files (above certain size)
     * @param minSize minimum file size in bytes
     * @return list of large files
     */
    @Query("SELECT ee FROM ExplanationEvidence ee " +
           "WHERE ee.fileSize > :minSize " +
           "ORDER BY ee.fileSize DESC")
    List<ExplanationEvidence> findLargeFiles(@Param("minSize") Long minSize);
    
    /**
     * Count evidence files by explanation
     * @param explanationId the explanation ID
     * @return count of evidence files
     */
    long countByExplanationId(Long explanationId);
    
    /**
     * Count evidence files by type
     * @param evidenceType the evidence type
     * @return count of evidence files
     */
    long countByEvidenceType(ExplanationEvidence.EvidenceType evidenceType);
    
    /**
     * Count verified evidence files
     * @param isVerified verification status
     * @return count of evidence files
     */
    long countByIsVerified(Boolean isVerified);
    
    /**
     * Get total file size by explanation
     * @param explanationId the explanation ID
     * @return total file size in bytes
     */
    @Query("SELECT COALESCE(SUM(ee.fileSize), 0) FROM ExplanationEvidence ee " +
           "WHERE ee.explanation.id = :explanationId")
    Long getTotalFileSizeByExplanation(@Param("explanationId") Long explanationId);
    
    /**
     * Get file statistics by type
     * @return file statistics
     */
    @Query("SELECT " +
           "ee.evidenceType as evidenceType, " +
           "COUNT(ee) as count, " +
           "COALESCE(SUM(ee.fileSize), 0) as totalSize, " +
           "COALESCE(AVG(ee.fileSize), 0) as avgSize " +
           "FROM ExplanationEvidence ee " +
           "GROUP BY ee.evidenceType " +
           "ORDER BY COUNT(ee) DESC")
    List<Object[]> getFileStatisticsByType();
    
    /**
     * Find evidence files by upload IP
     * @param uploadIp the upload IP address
     * @return list of evidence files
     */
    List<ExplanationEvidence> findByUploadIpOrderByCreatedAtDesc(String uploadIp);
    
    /**
     * Find evidence files that need verification
     * @return list of unverified evidence files
     */
    @Query("SELECT ee FROM ExplanationEvidence ee " +
           "WHERE ee.isVerified = false OR ee.isVerified IS NULL " +
           "ORDER BY ee.createdAt ASC")
    List<ExplanationEvidence> findUnverifiedFiles();
    
    /**
     * Find orphaned evidence files (explanation deleted)
     * @return list of orphaned files
     */
    @Query("SELECT ee FROM ExplanationEvidence ee " +
           "WHERE ee.explanation IS NULL")
    List<ExplanationEvidence> findOrphanedFiles();
    
    /**
     * Get monthly file upload statistics
     * @param year the year
     * @param month the month
     * @return monthly upload statistics
     */
    @Query("SELECT " +
           "ee.evidenceType as evidenceType, " +
           "COUNT(ee) as count, " +
           "COALESCE(SUM(ee.fileSize), 0) as totalSize " +
           "FROM ExplanationEvidence ee " +
           "WHERE YEAR(ee.createdAt) = :year " +
           "AND MONTH(ee.createdAt) = :month " +
           "GROUP BY ee.evidenceType " +
           "ORDER BY COUNT(ee) DESC")
    List<Object[]> getMonthlyUploadStatistics(@Param("year") int year, @Param("month") int month);
}
