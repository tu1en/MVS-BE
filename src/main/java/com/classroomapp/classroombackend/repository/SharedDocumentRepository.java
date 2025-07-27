package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.SharedDocument;

/**
 * Repository cho SharedDocument entity
 */
@Repository
public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    
    /**
     * TÃ¬m táº¥t cáº£ documents cá»§a slot (chá»‰ available)
     */
    List<SharedDocument> findBySlotIdAndDocumentStatusOrderByUploadedAtDesc(
        Long slotId, SharedDocument.DocumentStatus documentStatus);
    
    /**
     * TÃ¬m documents theo type
     */
    List<SharedDocument> findBySlotIdAndDocumentTypeAndDocumentStatusOrderByUploadedAtDesc(
        Long slotId, SharedDocument.DocumentType documentType, SharedDocument.DocumentStatus documentStatus);
    
    /**
     * TÃ¬m presentation documents cá»§a slot
     */
    @Query("SELECT sd FROM SharedDocument sd WHERE sd.slotId = :slotId AND sd.isCurrentlyPresenting = true AND sd.documentStatus = 'AVAILABLE' ORDER BY sd.uploadedAt DESC")
    List<SharedDocument> findPresentationsBySlotId(@Param("slotId") Long slotId);
    
    /**
     * TÃ¬m documents Ä‘Æ°á»£c upload bá»Ÿi user
     */
    List<SharedDocument> findByUploadedBy_UsernameAndDocumentStatusOrderByUploadedAtDesc(
        String username, SharedDocument.DocumentStatus documentStatus);
    
    /**
     * TÃ¬m documents theo access level
     */
    List<SharedDocument> findBySlotIdAndAccessLevelAndDocumentStatusOrderByUploadedAtDesc(
        Long slotId, SharedDocument.AccessLevel accessLevel, SharedDocument.DocumentStatus documentStatus);
    
    /**
     * Count documents cá»§a slot
     */
    @Query("SELECT COUNT(sd) FROM SharedDocument sd WHERE sd.slotId = :slotId AND sd.documentStatus = 'AVAILABLE'")
    Long countActiveDocumentsBySlotId(@Param("slotId") Long slotId);
    
    /**
     * Find documents by date range
     */
    @Query("SELECT sd FROM SharedDocument sd WHERE sd.slotId = :slotId AND sd.uploadedAt BETWEEN :startDate AND :endDate AND sd.documentStatus = 'AVAILABLE' ORDER BY sd.uploadedAt DESC")
    List<SharedDocument> findBySlotIdAndDateRange(
        @Param("slotId") Long slotId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find most downloaded documents
     */
    @Query("SELECT sd FROM SharedDocument sd WHERE sd.slotId = :slotId AND sd.documentStatus = 'AVAILABLE' ORDER BY sd.downloadCount DESC")
    List<SharedDocument> findMostDownloadedBySlotId(@Param("slotId") Long slotId);
    
    /**
     * Find documents by file type (MIME)
     */
    @Query("SELECT sd FROM SharedDocument sd WHERE sd.slotId = :slotId AND sd.mimeType = :mimeType AND sd.documentStatus = 'AVAILABLE' ORDER BY sd.uploadedAt DESC")
    List<SharedDocument> findBySlotIdAndMimeType(@Param("slotId") Long slotId, @Param("mimeType") String mimeType);
    
    /**
     * Check if document exists and is available
     */
    @Query("SELECT CASE WHEN COUNT(sd) > 0 THEN true ELSE false END FROM SharedDocument sd WHERE sd.id = :documentId AND sd.documentStatus = 'AVAILABLE'")
    boolean existsByIdAndIsAvailable(@Param("documentId") Long documentId);
    
    /**
     * Find current presentation document for slot
     */
    @Query("SELECT sd FROM SharedDocument sd WHERE sd.slotId = :slotId AND sd.isCurrentlyPresenting = true AND sd.documentStatus = 'AVAILABLE' ORDER BY sd.updatedAt DESC")
    Optional<SharedDocument> findCurrentPresentationBySlotId(@Param("slotId") Long slotId);
}
