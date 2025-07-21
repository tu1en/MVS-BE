package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.dto.RecordingSessionDto;
import com.classroomapp.classroombackend.model.LectureRecording;

/**
 * Service interface for LectureRecording management
 */
public interface LectureRecordingService {
    
    /**
     * Start recording for a lecture
     * @param lectureId the lecture ID
     * @param title the recording title
     * @return the created recording DTO
     */
    RecordingSessionDto startRecording(Long lectureId, String title);
    
    /**
     * Stop recording
     * @param recordingId the recording ID
     * @return the updated recording DTO
     */
    RecordingSessionDto stopRecording(Long recordingId);
    
    /**
     * Get recording by ID
     * @param recordingId the recording ID
     * @return the recording DTO
     */
    RecordingSessionDto getRecordingById(Long recordingId);
    
    /**
     * Get recording by lecture ID
     * @param lectureId the lecture ID
     * @return the recording DTO or null if none
     */
    RecordingSessionDto getRecordingByLectureId(Long lectureId);
    
    /**
     * Get all recordings for a lecture
     * @param lectureId the lecture ID
     * @return list of recording DTOs
     */
    List<RecordingSessionDto> getRecordingsByLectureId(Long lectureId);
    
    /**
     * Get recordings by classroom
     * @param classroomId the classroom ID
     * @return list of recording DTOs
     */
    List<RecordingSessionDto> getRecordingsByClassroomId(Long classroomId);
    
    /**
     * Get recordings by teacher
     * @param teacherId the teacher ID
     * @return list of recording DTOs
     */
    List<RecordingSessionDto> getRecordingsByTeacherId(Long teacherId);
    
    /**
     * Get all completed recordings
     * @return list of completed recording DTOs
     */
    List<RecordingSessionDto> getCompletedRecordings();
    
    /**
     * Get recordings currently being processed
     * @return list of processing recording DTOs
     */
    List<RecordingSessionDto> getProcessingRecordings();
    
    /**
     * Get failed recordings
     * @return list of failed recording DTOs
     */
    List<RecordingSessionDto> getFailedRecordings();
    
    /**
     * Update recording status
     * @param recordingId the recording ID
     * @param status the new status
     * @return the updated recording DTO
     */
    RecordingSessionDto updateRecordingStatus(Long recordingId, LectureRecording.RecordingStatus status);
    
    /**
     * Update recording file information
     * @param recordingId the recording ID
     * @param filePath the file path
     * @param fileName the file name
     * @param fileSize the file size in bytes
     * @param durationMinutes the duration in minutes
     * @return the updated recording DTO
     */
    RecordingSessionDto updateRecordingFile(Long recordingId, String filePath, String fileName, 
                                          Long fileSize, Integer durationMinutes);
    
    /**
     * Set recording as public/private
     * @param recordingId the recording ID
     * @param isPublic true if public, false if private
     * @return the updated recording DTO
     */
    RecordingSessionDto setRecordingVisibility(Long recordingId, boolean isPublic);
    
    /**
     * Increment view count for a recording
     * @param recordingId the recording ID
     */
    void incrementViewCount(Long recordingId);
    
    /**
     * Search recordings by title
     * @param keyword the search keyword
     * @return list of matching recording DTOs
     */
    List<RecordingSessionDto> searchRecordingsByTitle(String keyword);
    
    /**
     * Get recordings within date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of recording DTOs
     */
    List<RecordingSessionDto> getRecordingsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get recent recordings (last 30 days)
     * @return list of recent recording DTOs
     */
    List<RecordingSessionDto> getRecentRecordings();
    
    /**
     * Delete recording
     * @param recordingId the recording ID
     */
    void deleteRecording(Long recordingId);
    
    /**
     * Get total storage used by recordings
     * @return total storage in bytes
     */
    Long getTotalStorageUsed();
    
    /**
     * Get total recording duration
     * @return total duration in minutes
     */
    Integer getTotalRecordingDuration();
    
    /**
     * Get large recordings (above specified size)
     * @param sizeInBytes the minimum size in bytes
     * @return list of large recording DTOs
     */
    List<RecordingSessionDto> getLargeRecordings(Long sizeInBytes);
    
    /**
     * Get old recordings for cleanup
     * @param cutoffDate the cutoff date
     * @return list of old recording DTOs
     */
    List<RecordingSessionDto> getOldRecordings(LocalDateTime cutoffDate);
    
    /**
     * Check if recording exists for lecture
     * @param lectureId the lecture ID
     * @return true if recording exists
     */
    boolean hasRecording(Long lectureId);
    
    /**
     * Count recordings by status
     * @param status the recording status
     * @return count of recordings
     */
    long countRecordingsByStatus(LectureRecording.RecordingStatus status);
    
    /**
     * Process failed recordings (retry processing)
     * @return number of recordings reprocessed
     */
    int processFailedRecordings();
    
    /**
     * Cleanup old recordings
     * @param cutoffDate the cutoff date
     * @return number of recordings cleaned up
     */
    int cleanupOldRecordings(LocalDateTime cutoffDate);
    
    /**
     * Generate recording file name
     * @param lectureId the lecture ID
     * @param title the recording title
     * @return generated file name
     */
    String generateRecordingFileName(Long lectureId, String title);
    
    /**
     * Validate recording configuration
     * @param lectureId the lecture ID
     * @param title the recording title
     * @return true if valid
     */
    boolean validateRecordingConfig(Long lectureId, String title);
}
