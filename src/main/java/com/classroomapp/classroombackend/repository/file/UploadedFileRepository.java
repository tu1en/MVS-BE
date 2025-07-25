package com.classroomapp.classroombackend.repository.file;

import com.classroomapp.classroombackend.model.file.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho UploadedFile entity
 */
@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    /**
     * TÃ¬m file theo ID vÃ  chÆ°a bá»‹ xÃ³a
     */
    Optional<UploadedFile> findByIdAndDeletedFalse(Long id);

    /**
     * TÃ¬m file theo filename vÃ  chÆ°a bá»‹ xÃ³a
     */
    Optional<UploadedFile> findByFilenameAndDeletedFalse(String filename);

    /**
     * TÃ¬m files theo category vÃ  chÆ°a bá»‹ xÃ³a
     */
    List<UploadedFile> findByCategoryAndDeletedFalse(String category);

    /**
     * TÃ¬m files theo ngÆ°á»i upload vÃ  chÆ°a bá»‹ xÃ³a
     */
    List<UploadedFile> findByUploadedByAndDeletedFalse(String uploadedBy);

    /**
     * TÃ¬m files theo category vÃ  ngÆ°á»i upload
     */
    List<UploadedFile> findByCategoryAndUploadedByAndDeletedFalse(String category, String uploadedBy);

    /**
     * TÃ¬m files public vÃ  chÆ°a bá»‹ xÃ³a
     */
    List<UploadedFile> findByIsPublicTrueAndDeletedFalse();

    /**
     * TÃ¬m files theo MIME type vÃ  chÆ°a bá»‹ xÃ³a
     */
    List<UploadedFile> findByMimeTypeAndDeletedFalse(String mimeType);

    /**
     * TÃ¬m files theo virus scan status
     */
    List<UploadedFile> findByVirusScanStatus(UploadedFile.VirusScanStatus status);

    /**
     * TÃ¬m files bá»‹ quarantine
     */
    List<UploadedFile> findByQuarantinedTrue();

    /**
     * TÃ¬m files Ä‘Ã£ bá»‹ xÃ³a cÅ© hÆ¡n thá»i gian specified
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = true AND f.deletedAt < :cutoffDate")
    List<UploadedFile> findDeletedFilesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * TÃ¬m files theo checksum (Ä‘á»ƒ detect duplicates)
     */
    List<UploadedFile> findByChecksumAndDeletedFalse(String checksum);

    /**
     * TÃ¬m files theo parent file ID (versioning)
     */
    List<UploadedFile> findByParentFileIdAndDeletedFalse(Long parentFileId);

    /**
     * TÃ¬m files upload trong khoáº£ng thá»i gian
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.uploadedAt BETWEEN :startDate AND :endDate AND f.deleted = false")
    List<UploadedFile> findByUploadedAtBetweenAndDeletedFalse(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * TÃ¬m files vá»›i pagination vÃ  chÆ°a bá»‹ xÃ³a
     */
    Page<UploadedFile> findByDeletedFalse(Pageable pageable);

    /**
     * TÃ¬m files theo category vá»›i pagination
     */
    Page<UploadedFile> findByCategoryAndDeletedFalse(String category, Pageable pageable);

    /**
     * TÃ¬m files theo ngÆ°á»i upload vá»›i pagination
     */
    Page<UploadedFile> findByUploadedByAndDeletedFalse(String uploadedBy, Pageable pageable);

    /**
     * Search files theo filename hoáº·c description
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false AND " +
           "(LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UploadedFile> searchFiles(@Param("search") String search, Pageable pageable);

    /**
     * Search files theo multiple criteria
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false " +
           "AND (:category IS NULL OR f.category = :category) " +
           "AND (:uploadedBy IS NULL OR f.uploadedBy = :uploadedBy) " +
           "AND (:mimeType IS NULL OR f.mimeType = :mimeType) " +
           "AND (:search IS NULL OR LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UploadedFile> searchFilesWithCriteria(
        @Param("category") String category,
        @Param("uploadedBy") String uploadedBy,
        @Param("mimeType") String mimeType,
        @Param("search") String search,
        Pageable pageable
    );

    /**
     * Äáº¿m sá»‘ files chÆ°a bá»‹ xÃ³a
     */
    long countByDeletedFalse();

    /**
     * Äáº¿m sá»‘ files theo category
     */
    long countByCategoryAndDeletedFalse(String category);

    /**
     * Äáº¿m sá»‘ files theo ngÆ°á»i upload
     */
    long countByUploadedByAndDeletedFalse(String uploadedBy);

    /**
     * TÃ­nh tá»•ng kÃ­ch thÆ°á»›c files chÆ°a bá»‹ xÃ³a
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f WHERE f.deleted = false")
    Long sumFileSizeByDeletedFalse();

    /**
     * TÃ­nh tá»•ng kÃ­ch thÆ°á»›c files theo category
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f WHERE f.category = :category AND f.deleted = false")
    Long sumFileSizeByCategoryAndDeletedFalse(@Param("category") String category);

    /**
     * TÃ­nh tá»•ng kÃ­ch thÆ°á»›c files theo ngÆ°á»i upload
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f WHERE f.uploadedBy = :uploadedBy AND f.deleted = false")
    Long sumFileSizeByUploadedByAndDeletedFalse(@Param("uploadedBy") String uploadedBy);

    /**
     * Láº¥y top files Ä‘Æ°á»£c download nhiá»u nháº¥t
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false ORDER BY f.downloadCount DESC")
    List<UploadedFile> findTopDownloadedFiles(Pageable pageable);

    /**
     * Láº¥y files upload gáº§n Ä‘Ã¢y
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findRecentFiles(Pageable pageable);

    /**
     * Láº¥y files lá»›n nháº¥t
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false ORDER BY f.fileSize DESC")
    List<UploadedFile> findLargestFiles(Pageable pageable);

    /**
     * Láº¥y files cáº§n virus scan
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.virusScanStatus = 'PENDING' AND f.deleted = false")
    List<UploadedFile> findFilesNeedingVirusScan();

    /**
     * Láº¥y files cÃ³ access token háº¿t háº¡n
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.accessTokenExpiresAt < :now AND f.deleted = false")
    List<UploadedFile> findFilesWithExpiredAccessToken(@Param("now") LocalDateTime now);

    /**
     * Statistics queries
     */
    
    /**
     * Láº¥y thá»‘ng kÃª files theo category
     */
    @Query("SELECT f.category, COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f " +
           "WHERE f.deleted = false GROUP BY f.category")
    List<Object[]> getFileStatisticsByCategory();

    /**
     * Láº¥y thá»‘ng kÃª files theo MIME type
     */
    @Query("SELECT f.mimeType, COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f " +
           "WHERE f.deleted = false GROUP BY f.mimeType")
    List<Object[]> getFileStatisticsByMimeType();

    /**
     * Láº¥y thá»‘ng kÃª files theo ngÆ°á»i upload
     */
    @Query("SELECT f.uploadedBy, COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f " +
           "WHERE f.deleted = false GROUP BY f.uploadedBy")
    List<Object[]> getFileStatisticsByUploader();

    /**
     * Láº¥y thá»‘ng kÃª files theo thÃ¡ng
     */
    @Query("SELECT YEAR(f.uploadedAt), MONTH(f.uploadedAt), COUNT(f), COALESCE(SUM(f.fileSize), 0) " +
           "FROM UploadedFile f WHERE f.deleted = false " +
           "GROUP BY YEAR(f.uploadedAt), MONTH(f.uploadedAt) " +
           "ORDER BY YEAR(f.uploadedAt) DESC, MONTH(f.uploadedAt) DESC")
    List<Object[]> getFileStatisticsByMonth();

    /**
     * Láº¥y thá»‘ng kÃª virus scan
     */
    @Query("SELECT f.virusScanStatus, COUNT(f) FROM UploadedFile f GROUP BY f.virusScanStatus")
    List<Object[]> getVirusScanStatistics();

    /**
     * TÃ¬m duplicate files theo checksum
     */
    @Query("SELECT f.checksum, COUNT(f) FROM UploadedFile f " +
           "WHERE f.checksum IS NOT NULL AND f.deleted = false " +
           "GROUP BY f.checksum HAVING COUNT(f) > 1")
    List<Object[]> findDuplicateFiles();

    /**
     * Cleanup methods
     */
    
    /**
     * XÃ³a files Ä‘Ã£ bá»‹ soft delete cÅ© hÆ¡n specified days
     */
    @Query("DELETE FROM UploadedFile f WHERE f.deleted = true AND f.deletedAt < :cutoffDate")
    int deleteOldSoftDeletedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * XÃ³a files bá»‹ quarantine cÅ© hÆ¡n specified days
     */
    @Query("DELETE FROM UploadedFile f WHERE f.quarantined = true AND f.virusScannedAt < :cutoffDate")
    int deleteOldQuarantinedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);
}
