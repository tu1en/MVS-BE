package com.classroomapp.classroombackend.repository.classroommanagement;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Slot;

/**
 * Repository interface for Slot entity
 * Provides CRUD operations and custom queries for slot management
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long>, JpaSpecificationExecutor<Slot> {

    /**
     * Find all slots for a specific session
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId")
    List<Slot> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Find all slots for a specific session with pagination
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId")
    Page<Slot> findBySessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * Find slots by session ID ordered by start time
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId ORDER BY s.startTime ASC")
    List<Slot> findBySessionIdOrderByStartTimeAsc(@Param("sessionId") Long sessionId);

    /**
     * Find slots by session ID and status
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId AND s.status = :status")
    List<Slot> findBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") Slot.SlotStatus status);

    /**
     * Find slots by status
     */
    List<Slot> findByStatus(Slot.SlotStatus status);

    /**
     * Find slots by status with pagination
     */
    Page<Slot> findByStatus(Slot.SlotStatus status, Pageable pageable);

    /**
     * Find slots by time range
     */
    List<Slot> findByStartTimeBetween(LocalTime startTime, LocalTime endTime);

    /**
     * Find overlapping slots in a session
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId AND " +
           "((s.startTime >= :startTime AND s.startTime < :endTime) OR " +
           "(s.endTime > :startTime AND s.endTime <= :endTime) OR " +
           "(s.startTime <= :startTime AND s.endTime >= :endTime))")
    List<Slot> findOverlappingSlots(@Param("sessionId") Long sessionId,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime);

    /**
     * Find overlapping slots in a session excluding a specific slot
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId AND s.id != :excludeSlotId AND " +
           "((s.startTime >= :startTime AND s.startTime < :endTime) OR " +
           "(s.endTime > :startTime AND s.endTime <= :endTime) OR " +
           "(s.startTime <= :startTime AND s.endTime >= :endTime))")
    List<Slot> findOverlappingSlotsExcluding(@Param("sessionId") Long sessionId,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime,
                                            @Param("excludeSlotId") Long excludeSlotId);

    /**
     * Check if time slot overlaps with existing slots
     */
    @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.session.id = :sessionId AND " +
           "((s.startTime >= :startTime AND s.startTime < :endTime) OR " +
           "(s.endTime > :startTime AND s.endTime <= :endTime) OR " +
           "(s.startTime <= :startTime AND s.endTime >= :endTime))")
    boolean existsOverlappingSlot(@Param("sessionId") Long sessionId,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime);

    /**
     * Check if time slot overlaps excluding a specific slot
     */
    @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.session.id = :sessionId AND s.id != :excludeSlotId AND " +
           "((s.startTime >= :startTime AND s.startTime < :endTime) OR " +
           "(s.endTime > :startTime AND s.endTime <= :endTime) OR " +
           "(s.startTime <= :startTime AND s.endTime >= :endTime))")
    boolean existsOverlappingSlotExcluding(@Param("sessionId") Long sessionId,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime,
                                          @Param("excludeSlotId") Long excludeSlotId);

    /**
     * Find slot by ID with attachments
     */
    @Query("SELECT s FROM Slot s LEFT JOIN FETCH s.attachments WHERE s.id = :slotId")
    Optional<Slot> findByIdWithAttachments(@Param("slotId") Long slotId);

    /**
     * Find slots by session with attachments
     */
    @Query("SELECT DISTINCT s FROM Slot s LEFT JOIN FETCH s.attachments WHERE s.session.id = :sessionId ORDER BY s.startTime ASC")
    List<Slot> findBySessionIdWithAttachments(@Param("sessionId") Long sessionId);

    /**
     * Find slots by classroom ID (through session)
     */
    @Query("SELECT s FROM Slot s WHERE s.session.classroom.id = :classroomId ORDER BY s.session.sessionDate ASC, s.startTime ASC")
    List<Slot> findByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * Find slots by classroom ID with pagination
     */
    @Query("SELECT s FROM Slot s WHERE s.session.classroom.id = :classroomId ORDER BY s.session.sessionDate ASC, s.startTime ASC")
    Page<Slot> findByClassroomId(@Param("classroomId") Long classroomId, Pageable pageable);

    /**
     * Find slots by teacher ID (through session and classroom)
     */
    @Query("SELECT s FROM Slot s WHERE s.session.classroom.teacher.id = :teacherId ORDER BY s.session.sessionDate DESC, s.startTime ASC")
    List<Slot> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find slots by teacher ID with pagination
     */
    @Query("SELECT s FROM Slot s WHERE s.session.classroom.teacher.id = :teacherId ORDER BY s.session.sessionDate DESC, s.startTime ASC")
    Page<Slot> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    /**
     * Find slots for student (through classroom enrollments)
     */
    @Query("SELECT s FROM Slot s JOIN s.session.classroom.enrollments e WHERE e.user.id = :studentId ORDER BY s.session.sessionDate DESC, s.startTime ASC")
    List<Slot> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find slots for student with pagination
     */
    @Query("SELECT s FROM Slot s JOIN s.session.classroom.enrollments e WHERE e.user.id = :studentId ORDER BY s.session.sessionDate DESC, s.startTime ASC")
    Page<Slot> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * Count slots by session ID
     */
    @Query("SELECT COUNT(s) FROM Slot s WHERE s.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Count slots by session ID and status
     */
    @Query("SELECT COUNT(s) FROM Slot s WHERE s.session.id = :sessionId AND s.status = :status")
    long countBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") Slot.SlotStatus status);

    /**
     * Find active slots (status = ACTIVE)
     */
    @Query("SELECT s FROM Slot s WHERE s.status = 'ACTIVE' ORDER BY s.session.sessionDate ASC, s.startTime ASC")
    List<Slot> findActiveSlots();

    /**
     * Find slots that can accept attachments (status = ACTIVE)
     */
    @Query("SELECT s FROM Slot s WHERE s.status = 'ACTIVE' AND s.session.classroom.id = :classroomId ORDER BY s.session.sessionDate ASC, s.startTime ASC")
    List<Slot> findSlotsAcceptingAttachments(@Param("classroomId") Long classroomId);

    /**
     * Find slots with attachments count
     */
    @Query("SELECT s, COUNT(a) as attachmentCount FROM Slot s LEFT JOIN s.attachments a WHERE s.session.id = :sessionId GROUP BY s ORDER BY s.startTime ASC")
    List<Object[]> findSlotsWithAttachmentCount(@Param("sessionId") Long sessionId);

    /**
     * Search slots by description
     */
    @Query("SELECT s FROM Slot s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.session.sessionDate DESC, s.startTime ASC")
    List<Slot> searchByDescription(@Param("keyword") String keyword);

    /**
     * Find slots by multiple session IDs
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id IN :sessionIds ORDER BY s.session.sessionDate ASC, s.startTime ASC")
    List<Slot> findBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    /**
     * Get slot statistics for a session
     */
    @Query("SELECT " +
           "COUNT(s) as totalSlots, " +
           "COUNT(CASE WHEN s.status = 'DONE' THEN 1 END) as completedSlots, " +
           "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) as activeSlots, " +
           "COUNT(CASE WHEN s.status = 'PLANNED' THEN 1 END) as plannedSlots " +
           "FROM Slot s WHERE s.session.id = :sessionId")
    Object[] getSlotStatistics(@Param("sessionId") Long sessionId);

    /**
     * Delete slots by session ID (for cascade delete)
     */
    @Query("DELETE FROM Slot s WHERE s.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Find slots that can be deleted (PLANNED status and no attachments)
     */
    @Query("SELECT s FROM Slot s WHERE s.status = 'PLANNED' AND s.attachments IS EMPTY")
    List<Slot> findDeletableSlots();

    /**
     * Find slots by duration range (in minutes)
     */
    @Query("SELECT s FROM Slot s WHERE " +
           "DATEDIFF(MINUTE, s.startTime, s.endTime) BETWEEN :minDuration AND :maxDuration " +
           "ORDER BY s.session.sessionDate ASC, s.startTime ASC")
    List<Slot> findByDurationRange(@Param("minDuration") int minDuration, @Param("maxDuration") int maxDuration);

    /**
     * Find slots that need status update based on current time
     */
    @Query("SELECT s FROM Slot s WHERE s.session.sessionDate = CURRENT_DATE AND " +
           "((s.status = 'PLANNED' AND s.startTime <= CURRENT_TIME) OR " +
           "(s.status = 'ACTIVE' AND s.endTime <= CURRENT_TIME))")
    List<Slot> findSlotsNeedingStatusUpdate();

    /**
     * Find earliest slot in a session
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId ORDER BY s.startTime ASC LIMIT 1")
    Optional<Slot> findEarliestSlotInSession(@Param("sessionId") Long sessionId);

    /**
     * Find latest slot in a session
     */
    @Query("SELECT s FROM Slot s WHERE s.session.id = :sessionId ORDER BY s.endTime DESC LIMIT 1")
    Optional<Slot> findLatestSlotInSession(@Param("sessionId") Long sessionId);

    /**
     * Find slots by status ordered by start time ascending
     */
    List<Slot> findByStatusOrderByStartTimeAsc(Slot.SlotStatus status);

    /**
     * Count slots by status
     */
    long countByStatus(Slot.SlotStatus status);
}
