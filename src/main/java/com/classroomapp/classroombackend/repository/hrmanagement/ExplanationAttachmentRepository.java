package com.classroomapp.classroombackend.repository.hrmanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.hrmanagement.ExplanationAttachment;

/**
 * Repository interface for ExplanationAttachment entity
 */
@Repository
public interface ExplanationAttachmentRepository extends JpaRepository<ExplanationAttachment, Long> {
    
    /**
     * Find attachments by explanation ID
     * @param explanationId the explanation ID
     * @return list of attachments
     */
    List<ExplanationAttachment> findByExplanationIdOrderByUploadedAtDesc(Long explanationId);
    
    /**
     * Find attachments by explanation ID and file type
     * @param explanationId the explanation ID
     * @param mimeType the MIME type
     * @return list of attachments
     */
    List<ExplanationAttachment> findByExplanationIdAndMimeTypeOrderByUploadedAtDesc(Long explanationId, String mimeType);
    
    /**
     * Count attachments by explanation ID
     * @param explanationId the explanation ID
     * @return count of attachments
     */
    long countByExplanationId(Long explanationId);
    
    /**
     * Find large files (above specified size)
     * @param sizeThreshold the size threshold in bytes
     * @return list of large files
     */
    @Query("SELECT ea FROM ExplanationAttachment ea WHERE ea.fileSize > :sizeThreshold ORDER BY ea.fileSize DESC")
    List<ExplanationAttachment> findLargeFiles(@Param("sizeThreshold") Long sizeThreshold);
    
    /**
     * Find attachments uploaded before a specific date (for cleanup)
     * @param date the cutoff date
     * @return list of old attachments
     */
    List<ExplanationAttachment> findByUploadedAtBeforeOrderByUploadedAtAsc(LocalDateTime date);
    
    /**
     * Find attachments by file extension
     * @param extension the file extension
     * @return list of attachments
     */
    @Query("SELECT ea FROM ExplanationAttachment ea WHERE LOWER(ea.fileName) LIKE %:extension ORDER BY ea.uploadedAt DESC")
    List<ExplanationAttachment> findByFileExtension(@Param("extension") String extension);
    
    /**
     * Get total storage size used by all attachments
     * @return total size in bytes
     */
    @Query("SELECT COALESCE(SUM(ea.fileSize), 0) FROM ExplanationAttachment ea")
    Long getTotalStorageUsed();
    
    /**
     * Get storage usage by explanation
     * @param explanationId the explanation ID
     * @return total size for the explanation
     */
    @Query("SELECT COALESCE(SUM(ea.fileSize), 0) FROM ExplanationAttachment ea WHERE ea.explanation.id = :explanationId")
    Long getStorageUsedByExplanation(@Param("explanationId") Long explanationId);
    
    /**
     * Find orphaned attachments (explanation is null or soft-deleted)
     * @return list of orphaned attachments
     */
    @Query("SELECT ea FROM ExplanationAttachment ea WHERE ea.explanation IS NULL")
    List<ExplanationAttachment> findOrphanedAttachments();
    
    /**
     * Delete attachments by explanation ID
     * @param explanationId the explanation ID
     */
    void deleteByExplanationId(Long explanationId);
    
    /**
     * Find attachments by multiple explanation IDs
     * @param explanationIds list of explanation IDs
     * @return list of attachments
     */
    List<ExplanationAttachment> findByExplanationIdInOrderByUploadedAtDesc(List<Long> explanationIds);
}