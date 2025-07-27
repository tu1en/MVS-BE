package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.LectureRecording;

/**
 * Repository interface for LectureRecording entity
 */
@Repository
public interface LectureRecordingRepository extends JpaRepository<LectureRecording, Long> {
    
    /**
     * Find lecture recording by lecture ID
     * @param lectureId the lecture ID
     * @return Optional containing the lecture recording if found
     */
    Optional<LectureRecording> findByLectureId(Long lectureId);
    
    /**
     * Find all lecture recordings by lecture ID
     * @param lectureId the lecture ID
     * @return List of lecture recordings for the lecture
     */
    List<LectureRecording> findByLectureIdOrderByRecordingDateDesc(Long lectureId);
    
    /**
     * Find lecture recording by file path
     * @param filePath the file path
     * @return Optional containing the lecture recording if found
     */
    Optional<LectureRecording> findByFilePath(String filePath);
    
    /**
     * Find lecture recording by file name
     * @param fileName the file name
     * @return Optional containing the lecture recording if found
     */
    Optional<LectureRecording> findByFileName(String fileName);
    
    /**
     * Find all lecture recordings by status
     * @param status the recording status
     * @return List of lecture recordings with the specified status
     */
    List<LectureRecording> findByStatusOrderByRecordingDateDesc(LectureRecording.RecordingStatus status);
    
    /**
     * Find lecture recordings within date range
     * @param startDate the start date
     * @param endDate the end date
     * @return List of lecture recordings within the date range
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.recordingDate >= :startDate AND lr.recordingDate <= :endDate ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findByRecordingDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find lecture recordings by classroom (through lecture relationship)
     * @param classroomId the classroom ID
     * @return List of lecture recordings for the classroom
     */
    @Query("SELECT lr FROM LectureRecording lr JOIN lr.lecture l WHERE l.classroom.id = :classroomId ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findByClassroomId(@Param("classroomId") Long classroomId);
    
    /**
     * Find lecture recordings by teacher (through lecture relationship)
     * @param teacherId the teacher ID
     * @return List of lecture recordings by the teacher
     */
    @Query("SELECT lr FROM LectureRecording lr JOIN lr.lecture l JOIN l.schedule s WHERE s.teacher.id = :teacherId ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * Find recordings that are currently being processed
     * @return List of recordings being processed
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.status = 'PROCESSING' ORDER BY lr.recordingDate ASC")
    List<LectureRecording> findProcessingRecordings();
    
    /**
     * Find completed recordings
     * @return List of completed recordings
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.status = 'COMPLETED' ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findCompletedRecordings();
    
    /**
     * Find failed recordings
     * @return List of failed recordings
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.status = 'FAILED' ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findFailedRecordings();
    
    /**
     * Get total storage used by recordings
     * @return Total file size in bytes
     */
    @Query("SELECT COALESCE(SUM(lr.fileSize), 0) FROM LectureRecording lr WHERE lr.status = 'COMPLETED'")
    Long getTotalStorageUsed();
    
    /**
     * Get total recording duration
     * @return Total duration in minutes
     */
    @Query("SELECT COALESCE(SUM(lr.durationMinutes), 0) FROM LectureRecording lr WHERE lr.status = 'COMPLETED'")
    Integer getTotalRecordingDuration();
    
    /**
     * Find recordings by title containing keyword
     * @param keyword the search keyword
     * @return List of recordings matching the keyword
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE LOWER(lr.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);
    
    /**
     * Find recordings larger than specified size
     * @param sizeInBytes the minimum file size in bytes
     * @return List of large recordings
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.fileSize > :sizeInBytes ORDER BY lr.fileSize DESC")
    List<LectureRecording> findLargeRecordings(@Param("sizeInBytes") Long sizeInBytes);
    
    /**
     * Find recordings older than specified date for cleanup
     * @param cutoffDate the cutoff date
     * @return List of old recordings
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.recordingDate < :cutoffDate ORDER BY lr.recordingDate ASC")
    List<LectureRecording> findOldRecordings(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Check if recording exists for lecture
     * @param lectureId the lecture ID
     * @return true if recording exists
     */
    boolean existsByLectureId(Long lectureId);
    
    /**
     * Count recordings by status
     * @param status the recording status
     * @return Count of recordings with the status
     */
    long countByStatus(LectureRecording.RecordingStatus status);
    
    /**
     * Find recent recordings (last 30 days)
     * @param sinceDate the date to search from
     * @return List of recent recordings
     */
    @Query("SELECT lr FROM LectureRecording lr WHERE lr.recordingDate >= :sinceDate ORDER BY lr.recordingDate DESC")
    List<LectureRecording> findRecentRecordings(@Param("sinceDate") LocalDateTime sinceDate);
}
