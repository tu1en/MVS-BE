package com.classroomapp.classroombackend.repository.parentmanagement;

import com.classroomapp.classroombackend.model.ParentLeaveNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ParentLeaveNotice entity
 * Based on PARENT_ROLE_SPEC.md - Core feature for leave notifications
 */
@Repository
public interface ParentLeaveNoticeRepository extends JpaRepository<ParentLeaveNotice, Long> {

    /**
     * Find notices by parent ID
     */
    List<ParentLeaveNotice> findByParentIdOrderByCreatedAtDesc(Long parentId);

    /**
     * Find notices by student ID
     */
    List<ParentLeaveNotice> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Find notices by parent and student
     */
    List<ParentLeaveNotice> findByParentIdAndStudentIdOrderByCreatedAtDesc(Long parentId, Long studentId);

    /**
     * Find notices by date
     */
    List<ParentLeaveNotice> findByDate(LocalDate date);

    /**
     * Find notices by date range
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.date BETWEEN :startDate AND :endDate ORDER BY pln.date DESC, pln.createdAt DESC")
    List<ParentLeaveNotice> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find notices by status
     */
    List<ParentLeaveNotice> findByStatusOrderByCreatedAtDesc(ParentLeaveNotice.NoticeStatus status);

    /**
     * Find notices by type
     */
    List<ParentLeaveNotice> findByTypeOrderByCreatedAtDesc(ParentLeaveNotice.NoticeType type);

    /**
     * Find pending notices (not acknowledged)
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.status IN ('SENT', 'DELIVERED') ORDER BY pln.createdAt DESC")
    List<ParentLeaveNotice> findPendingNotices();

    /**
     * Find pending notices for specific student
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.studentId = :studentId AND pln.status IN ('SENT', 'DELIVERED') ORDER BY pln.createdAt DESC")
    List<ParentLeaveNotice> findPendingNoticesByStudentId(@Param("studentId") Long studentId);

    /**
     * Find pending notices for specific date
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.date = :date AND pln.status IN ('SENT', 'DELIVERED') ORDER BY pln.createdAt DESC")
    List<ParentLeaveNotice> findPendingNoticesByDate(@Param("date") LocalDate date);

    /**
     * Find notices for teacher review (by class/student IDs)
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.studentId IN :studentIds ORDER BY pln.date DESC, pln.createdAt DESC")
    List<ParentLeaveNotice> findByStudentIds(@Param("studentIds") List<Long> studentIds);

    /**
     * Find notices for specific parent and date range
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.parentId = :parentId AND pln.date BETWEEN :startDate AND :endDate ORDER BY pln.date DESC")
    List<ParentLeaveNotice> findByParentIdAndDateRange(@Param("parentId") Long parentId, 
                                                       @Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Find notices for specific student and date range
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.studentId = :studentId AND pln.date BETWEEN :startDate AND :endDate ORDER BY pln.date DESC")
    List<ParentLeaveNotice> findByStudentIdAndDateRange(@Param("studentId") Long studentId, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);

    /**
     * Check for overlapping notices (prevent duplicates)
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.studentId = :studentId AND pln.date = :date AND pln.type = :type AND " +
           "((pln.type = 'FULL_DAY') OR " +
           " (pln.type = 'LATE' AND pln.arriveAt = :arriveAt) OR " +
           " (pln.type = 'EARLY' AND pln.leaveAt = :leaveAt))")
    List<ParentLeaveNotice> findOverlappingNotices(@Param("studentId") Long studentId, 
                                                   @Param("date") LocalDate date, 
                                                   @Param("type") ParentLeaveNotice.NoticeType type,
                                                   @Param("arriveAt") java.time.LocalTime arriveAt,
                                                   @Param("leaveAt") java.time.LocalTime leaveAt);

    /**
     * Count pending notices for parent
     */
    @Query("SELECT COUNT(pln) FROM ParentLeaveNotice pln WHERE pln.parentId = :parentId AND pln.status IN ('SENT', 'DELIVERED')")
    Long countPendingNoticesByParentId(@Param("parentId") Long parentId);

    /**
     * Count total notices for parent
     */
    Long countByParentId(Long parentId);

    /**
     * Find recent notices (last N days)
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.createdAt >= :since ORDER BY pln.createdAt DESC")
    List<ParentLeaveNotice> findRecentNotices(@Param("since") LocalDateTime since);

    /**
     * Find notices by reason code
     */
    List<ParentLeaveNotice> findByReasonCodeOrderByCreatedAtDesc(ParentLeaveNotice.ReasonCode reasonCode);

    /**
     * Find notices acknowledged by specific user
     */
    List<ParentLeaveNotice> findByAckByUserIdOrderByAckAtDesc(Long ackByUserId);

    /**
     * Find notices for attendance integration
     * Get notices that affect attendance for specific student and date
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.studentId = :studentId AND pln.date = :date AND pln.status = 'ACKNOWLEDGED'")
    List<ParentLeaveNotice> findAcknowledgedNoticesForAttendance(@Param("studentId") Long studentId, @Param("date") LocalDate date);

    /**
     * Find future notices
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.date > :currentDate ORDER BY pln.date ASC")
    List<ParentLeaveNotice> findFutureNotices(@Param("currentDate") LocalDate currentDate);

    /**
     * Find today's notices
     */
    @Query("SELECT pln FROM ParentLeaveNotice pln WHERE pln.date = :today ORDER BY pln.createdAt DESC")
    List<ParentLeaveNotice> findTodayNotices(@Param("today") LocalDate today);

    /**
     * Statistics: Count by status for dashboard
     */
    @Query("SELECT pln.status, COUNT(pln) FROM ParentLeaveNotice pln WHERE pln.parentId = :parentId GROUP BY pln.status")
    List<Object[]> countByStatusForParent(@Param("parentId") Long parentId);

    /**
     * Statistics: Count by type for dashboard
     */
    @Query("SELECT pln.type, COUNT(pln) FROM ParentLeaveNotice pln WHERE pln.parentId = :parentId GROUP BY pln.type")
    List<Object[]> countByTypeForParent(@Param("parentId") Long parentId);
}