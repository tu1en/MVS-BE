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
     * Tìm file theo ID và chưa bị xóa
     */
    Optional<UploadedFile> findByIdAndDeletedFalse(Long id);

    /**
     * Tìm file theo filename và chưa bị xóa
     */
    Optional<UploadedFile> findByFilenameAndDeletedFalse(String filename);

    /**
     * Tìm files theo category và chưa bị xóa
     */
    List<UploadedFile> findByCategoryAndDeletedFalse(String category);

    /**
     * Tìm files theo người upload và chưa bị xóa
     */
    List<UploadedFile> findByUploadedByAndDeletedFalse(String uploadedBy);

    /**
     * Tìm files theo category và người upload
     */
    List<UploadedFile> findByCategoryAndUploadedByAndDeletedFalse(String category, String uploadedBy);

    /**
     * Tìm files public và chưa bị xóa
     */
    List<UploadedFile> findByIsPublicTrueAndDeletedFalse();

    /**
     * Tìm files theo MIME type và chưa bị xóa
     */
    List<UploadedFile> findByMimeTypeAndDeletedFalse(String mimeType);

    /**
     * Tìm files theo virus scan status
     */
    List<UploadedFile> findByVirusScanStatus(UploadedFile.VirusScanStatus status);

    /**
     * Tìm files bị quarantine
     */
    List<UploadedFile> findByQuarantinedTrue();

    /**
     * Tìm files đã bị xóa cũ hơn thời gian specified
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = true AND f.deletedAt < :cutoffDate")
    List<UploadedFile> findDeletedFilesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Tìm files theo checksum (để detect duplicates)
     */
    List<UploadedFile> findByChecksumAndDeletedFalse(String checksum);

    /**
     * Tìm files theo parent file ID (versioning)
     */
    List<UploadedFile> findByParentFileIdAndDeletedFalse(Long parentFileId);

    /**
     * Tìm files upload trong khoảng thời gian
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.uploadedAt BETWEEN :startDate AND :endDate AND f.deleted = false")
    List<UploadedFile> findByUploadedAtBetweenAndDeletedFalse(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tìm files với pagination và chưa bị xóa
     */
    Page<UploadedFile> findByDeletedFalse(Pageable pageable);

    /**
     * Tìm files theo category với pagination
     */
    Page<UploadedFile> findByCategoryAndDeletedFalse(String category, Pageable pageable);

    /**
     * Tìm files theo người upload với pagination
     */
    Page<UploadedFile> findByUploadedByAndDeletedFalse(String uploadedBy, Pageable pageable);

    /**
     * Search files theo filename hoặc description
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
     * Đếm số files chưa bị xóa
     */
    long countByDeletedFalse();

    /**
     * Đếm số files theo category
     */
    long countByCategoryAndDeletedFalse(String category);

    /**
     * Đếm số files theo người upload
     */
    long countByUploadedByAndDeletedFalse(String uploadedBy);

    /**
     * Tính tổng kích thước files chưa bị xóa
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f WHERE f.deleted = false")
    Long sumFileSizeByDeletedFalse();

    /**
     * Tính tổng kích thước files theo category
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f WHERE f.category = :category AND f.deleted = false")
    Long sumFileSizeByCategoryAndDeletedFalse(@Param("category") String category);

    /**
     * Tính tổng kích thước files theo người upload
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f WHERE f.uploadedBy = :uploadedBy AND f.deleted = false")
    Long sumFileSizeByUploadedByAndDeletedFalse(@Param("uploadedBy") String uploadedBy);

    /**
     * Lấy top files được download nhiều nhất
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false ORDER BY f.downloadCount DESC")
    List<UploadedFile> findTopDownloadedFiles(Pageable pageable);

    /**
     * Lấy files upload gần đây
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findRecentFiles(Pageable pageable);

    /**
     * Lấy files lớn nhất
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.deleted = false ORDER BY f.fileSize DESC")
    List<UploadedFile> findLargestFiles(Pageable pageable);

    /**
     * Lấy files cần virus scan
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.virusScanStatus = 'PENDING' AND f.deleted = false")
    List<UploadedFile> findFilesNeedingVirusScan();

    /**
     * Lấy files có access token hết hạn
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.accessTokenExpiresAt < :now AND f.deleted = false")
    List<UploadedFile> findFilesWithExpiredAccessToken(@Param("now") LocalDateTime now);

    /**
     * Statistics queries
     */
    
    /**
     * Lấy thống kê files theo category
     */
    @Query("SELECT f.category, COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f " +
           "WHERE f.deleted = false GROUP BY f.category")
    List<Object[]> getFileStatisticsByCategory();

    /**
     * Lấy thống kê files theo MIME type
     */
    @Query("SELECT f.mimeType, COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f " +
           "WHERE f.deleted = false GROUP BY f.mimeType")
    List<Object[]> getFileStatisticsByMimeType();

    /**
     * Lấy thống kê files theo người upload
     */
    @Query("SELECT f.uploadedBy, COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM UploadedFile f " +
           "WHERE f.deleted = false GROUP BY f.uploadedBy")
    List<Object[]> getFileStatisticsByUploader();

    /**
     * Lấy thống kê files theo tháng
     */
    @Query("SELECT YEAR(f.uploadedAt), MONTH(f.uploadedAt), COUNT(f), COALESCE(SUM(f.fileSize), 0) " +
           "FROM UploadedFile f WHERE f.deleted = false " +
           "GROUP BY YEAR(f.uploadedAt), MONTH(f.uploadedAt) " +
           "ORDER BY YEAR(f.uploadedAt) DESC, MONTH(f.uploadedAt) DESC")
    List<Object[]> getFileStatisticsByMonth();

    /**
     * Lấy thống kê virus scan
     */
    @Query("SELECT f.virusScanStatus, COUNT(f) FROM UploadedFile f GROUP BY f.virusScanStatus")
    List<Object[]> getVirusScanStatistics();

    /**
     * Tìm duplicate files theo checksum
     */
    @Query("SELECT f.checksum, COUNT(f) FROM UploadedFile f " +
           "WHERE f.checksum IS NOT NULL AND f.deleted = false " +
           "GROUP BY f.checksum HAVING COUNT(f) > 1")
    List<Object[]> findDuplicateFiles();

    /**
     * Cleanup methods
     */
    
    /**
     * Xóa files đã bị soft delete cũ hơn specified days
     */
    @Query("DELETE FROM UploadedFile f WHERE f.deleted = true AND f.deletedAt < :cutoffDate")
    int deleteOldSoftDeletedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Xóa files bị quarantine cũ hơn specified days
     */
    @Query("DELETE FROM UploadedFile f WHERE f.quarantined = true AND f.virusScannedAt < :cutoffDate")
    int deleteOldQuarantinedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);
}
