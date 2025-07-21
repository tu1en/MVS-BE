package com.classroomapp.classroombackend.repository.classroommanagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Attachment;

/**
 * Repository interface for Attachment entity
 * Provides CRUD operations and custom queries for attachment management
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long>, JpaSpecificationExecutor<Attachment> {

    /**
     * Find all attachments for a specific slot
     */
    List<Attachment> findBySlotId(Long slotId);

    /**
     * Find all attachments for a specific slot with pagination
     */
    Page<Attachment> findBySlotId(Long slotId, Pageable pageable);

    /**
     * Find attachments by slot ID ordered by upload date
     */
    List<Attachment> findBySlotIdOrderByUploadedAtDesc(Long slotId);

    /**
     * Find attachments by uploader
     */
    List<Attachment> findByUploadedById(Long uploaderId);

    /**
     * Find attachments by uploader with pagination
     */
    Page<Attachment> findByUploadedById(Long uploaderId, Pageable pageable);

    /**
     * Find attachments by MIME type
     */
    List<Attachment> findByMimeType(String mimeType);

    /**
     * Find attachments by MIME type with pagination
     */
    Page<Attachment> findByMimeType(String mimeType, Pageable pageable);

    /**
     * Find attachments by multiple MIME types
     */
    @Query("SELECT a FROM Attachment a WHERE a.mimeType IN :mimeTypes ORDER BY a.uploadedAt DESC")
    List<Attachment> findByMimeTypes(@Param("mimeTypes") List<String> mimeTypes);

    /**
     * Find attachments by file size range
     */
    @Query("SELECT a FROM Attachment a WHERE a.fileSize BETWEEN :minSize AND :maxSize ORDER BY a.uploadedAt DESC")
    List<Attachment> findByFileSizeRange(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

    /**
     * Find attachments by upload date range
     */
    List<Attachment> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find attachments by upload date range with pagination
     */
    Page<Attachment> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find attachment by stored file name
     */
    Optional<Attachment> findByStoredFileName(String storedFileName);

    /**
     * Find attachments by original file name (case-insensitive)
     */
    List<Attachment> findByOriginalFileNameContainingIgnoreCase(String fileName);

    /**
     * Find attachments by session ID (through slot)
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.session.id = :sessionId ORDER BY a.uploadedAt DESC")
    List<Attachment> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Find attachments by session ID with pagination
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.session.id = :sessionId ORDER BY a.uploadedAt DESC")
    Page<Attachment> findBySessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * Find attachments by classroom ID (through slot and session)
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.session.classroom.id = :classroomId ORDER BY a.uploadedAt DESC")
    List<Attachment> findByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * Find attachments by classroom ID with pagination
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.session.classroom.id = :classroomId ORDER BY a.uploadedAt DESC")
    Page<Attachment> findByClassroomId(@Param("classroomId") Long classroomId, Pageable pageable);

    /**
     * Find attachments by teacher ID (through slot, session, and classroom)
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.session.classroom.teacher.id = :teacherId ORDER BY a.uploadedAt DESC")
    List<Attachment> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find attachments by teacher ID with pagination
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.session.classroom.teacher.id = :teacherId ORDER BY a.uploadedAt DESC")
    Page<Attachment> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    /**
     * Find attachments accessible to student (through classroom enrollments)
     */
    @Query("SELECT a FROM Attachment a JOIN a.slot.session.classroom.enrollments e WHERE e.user.id = :studentId ORDER BY a.uploadedAt DESC")
    List<Attachment> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find attachments accessible to student with pagination
     */
    @Query("SELECT a FROM Attachment a JOIN a.slot.session.classroom.enrollments e WHERE e.user.id = :studentId ORDER BY a.uploadedAt DESC")
    Page<Attachment> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * Count attachments by slot ID
     */
    long countBySlotId(Long slotId);

    /**
     * Count attachments by uploader ID
     */
    long countByUploadedById(Long uploaderId);

    /**
     * Count attachments by MIME type
     */
    long countByMimeType(String mimeType);

    /**
     * Get total file size for a slot
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.slot.id = :slotId")
    Long getTotalFileSizeBySlotId(@Param("slotId") Long slotId);

    /**
     * Get total file size for a session
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.slot.session.id = :sessionId")
    Long getTotalFileSizeBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Get total file size for a classroom
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.slot.session.classroom.id = :classroomId")
    Long getTotalFileSizeByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * Find PDF attachments
     */
    @Query("SELECT a FROM Attachment a WHERE a.mimeType = 'application/pdf' ORDER BY a.uploadedAt DESC")
    List<Attachment> findPdfAttachments();

    /**
     * Find Word document attachments
     */
    @Query("SELECT a FROM Attachment a WHERE a.mimeType = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ORDER BY a.uploadedAt DESC")
    List<Attachment> findWordAttachments();

    /**
     * Find PowerPoint attachments
     */
    @Query("SELECT a FROM Attachment a WHERE a.mimeType = 'application/vnd.openxmlformats-officedocument.presentationml.presentation' ORDER BY a.uploadedAt DESC")
    List<Attachment> findPowerPointAttachments();

    /**
     * Find large attachments (> specified size)
     */
    @Query("SELECT a FROM Attachment a WHERE a.fileSize > :sizeThreshold ORDER BY a.fileSize DESC")
    List<Attachment> findLargeAttachments(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Find recent attachments (uploaded within specified hours)
     */
    @Query("SELECT a FROM Attachment a WHERE a.uploadedAt >= :since ORDER BY a.uploadedAt DESC")
    List<Attachment> findRecentAttachments(@Param("since") LocalDateTime since);

    /**
     * Search attachments by file name (original or stored)
     */
    @Query("SELECT a FROM Attachment a WHERE " +
           "LOWER(a.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.storedFileName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY a.uploadedAt DESC")
    List<Attachment> searchByFileName(@Param("keyword") String keyword);

    /**
     * Find attachments by multiple slot IDs
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot.id IN :slotIds ORDER BY a.uploadedAt DESC")
    List<Attachment> findBySlotIds(@Param("slotIds") List<Long> slotIds);

    /**
     * Get attachment statistics for a classroom
     */
    @Query("SELECT " +
           "COUNT(a) as totalAttachments, " +
           "COUNT(CASE WHEN a.mimeType = 'application/pdf' THEN 1 END) as pdfCount, " +
           "COUNT(CASE WHEN a.mimeType = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' THEN 1 END) as wordCount, " +
           "COUNT(CASE WHEN a.mimeType = 'application/vnd.openxmlformats-officedocument.presentationml.presentation' THEN 1 END) as powerpointCount, " +
           "COALESCE(SUM(a.fileSize), 0) as totalSize " +
           "FROM Attachment a WHERE a.slot.session.classroom.id = :classroomId")
    Object[] getAttachmentStatistics(@Param("classroomId") Long classroomId);

    /**
     * Find attachments uploaded by user in a specific classroom
     */
    @Query("SELECT a FROM Attachment a WHERE a.uploadedBy.id = :userId AND a.slot.session.classroom.id = :classroomId ORDER BY a.uploadedAt DESC")
    List<Attachment> findByUploaderAndClassroom(@Param("userId") Long userId, @Param("classroomId") Long classroomId);

    /**
     * Check if user has uploaded any attachments to a slot
     */
    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.uploadedBy.id = :userId AND a.slot.id = :slotId")
    boolean hasUserUploadedToSlot(@Param("userId") Long userId, @Param("slotId") Long slotId);

    /**
     * Find duplicate file names in a slot
     */
    @Query("SELECT a.originalFileName, COUNT(a) FROM Attachment a WHERE a.slot.id = :slotId GROUP BY a.originalFileName HAVING COUNT(a) > 1")
    List<Object[]> findDuplicateFileNamesInSlot(@Param("slotId") Long slotId);

    /**
     * Delete attachments by slot ID (for cascade delete)
     */
    void deleteBySlotId(Long slotId);

    /**
     * Find orphaned attachments (slots that don't exist)
     */
    @Query("SELECT a FROM Attachment a WHERE a.slot IS NULL")
    List<Attachment> findOrphanedAttachments();

    /**
     * Find attachments older than specified date
     */
    @Query("SELECT a FROM Attachment a WHERE a.uploadedAt < :cutoffDate ORDER BY a.uploadedAt ASC")
    List<Attachment> findOldAttachments(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get file type distribution for a classroom
     */
    @Query("SELECT a.mimeType, COUNT(a) FROM Attachment a WHERE a.slot.session.classroom.id = :classroomId GROUP BY a.mimeType ORDER BY COUNT(a) DESC")
    List<Object[]> getFileTypeDistribution(@Param("classroomId") Long classroomId);
}
