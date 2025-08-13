package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.ParentLeaveNotice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for ParentLeaveNotice management
 * Based on PARENT_ROLE_SPEC.md - Core feature for leave notifications
 */
public interface ParentLeaveNoticeService {

    /**
     * Create a new leave notice
     */
    ParentLeaveNotice createLeaveNotice(ParentLeaveNotice notice);

    /**
     * Create FULL_DAY leave notice
     */
    ParentLeaveNotice createFullDayNotice(Long parentId, Long studentId, LocalDate date, 
                                         ParentLeaveNotice.ReasonCode reasonCode, String note);

    /**
     * Create LATE leave notice
     */
    ParentLeaveNotice createLateNotice(Long parentId, Long studentId, LocalDate date, 
                                      java.time.LocalTime arriveAt, ParentLeaveNotice.ReasonCode reasonCode, String note);

    /**
     * Create EARLY leave notice
     */
    ParentLeaveNotice createEarlyNotice(Long parentId, Long studentId, LocalDate date, 
                                       java.time.LocalTime leaveAt, ParentLeaveNotice.ReasonCode reasonCode, String note);

    /**
     * Get notice by ID
     */
    Optional<ParentLeaveNotice> getNoticeById(Long noticeId);

    /**
     * Get notices for parent
     */
    List<ParentLeaveNotice> getNoticesByParentId(Long parentId);

    /**
     * Get notices for student
     */
    List<ParentLeaveNotice> getNoticesByStudentId(Long studentId);

    /**
     * Get notices for parent and specific student
     */
    List<ParentLeaveNotice> getNoticesByParentAndStudent(Long parentId, Long studentId);

    /**
     * Get notices by date range
     */
    List<ParentLeaveNotice> getNoticesByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get notices for specific date
     */
    List<ParentLeaveNotice> getNoticesByDate(LocalDate date);

    /**
     * Get pending notices (not acknowledged)
     */
    List<ParentLeaveNotice> getPendingNotices();

    /**
     * Get pending notices for student
     */
    List<ParentLeaveNotice> getPendingNoticesByStudentId(Long studentId);

    /**
     * Get pending notices for date
     */
    List<ParentLeaveNotice> getPendingNoticesByDate(LocalDate date);

    /**
     * Acknowledge a notice (by teacher/staff)
     */
    ParentLeaveNotice acknowledgeNotice(Long noticeId, Long acknowledgedByUserId);

    /**
     * Check for overlapping notices
     */
    boolean hasOverlappingNotice(Long studentId, LocalDate date, ParentLeaveNotice.NoticeType type,
                                java.time.LocalTime arriveAt, java.time.LocalTime leaveAt);

    /**
     * Validate notice constraints
     */
    void validateNoticeConstraints(ParentLeaveNotice notice);

    /**
     * Get notices for teacher review (by student IDs)
     */
    List<ParentLeaveNotice> getNoticesForTeacherReview(List<Long> studentIds);

    /**
     * Get notices for attendance integration
     */
    List<ParentLeaveNotice> getAcknowledgedNoticesForAttendance(Long studentId, LocalDate date);

    /**
     * Count pending notices for parent
     */
    Long countPendingNoticesByParentId(Long parentId);

    /**
     * Get today's notices
     */
    List<ParentLeaveNotice> getTodayNotices();

    /**
     * Get future notices
     */
    List<ParentLeaveNotice> getFutureNotices();

    /**
     * Get recent notices (last N days)
     */
    List<ParentLeaveNotice> getRecentNotices(int days);

    /**
     * Get notice statistics for parent
     */
    NoticeStatistics getNoticeStatisticsForParent(Long parentId);

    /**
     * Delete notice (if allowed)
     */
    void deleteNotice(Long noticeId, Long parentId);

    /**
     * Update notice status
     */
    ParentLeaveNotice updateNoticeStatus(Long noticeId, ParentLeaveNotice.NoticeStatus status);

    /**
     * Search notices
     */
    List<ParentLeaveNotice> searchNotices(String searchTerm);

    /**
     * Get notices for parent in date range
     */
    List<ParentLeaveNotice> getNoticesForParentInDateRange(Long parentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get notices for student in date range
     */
    List<ParentLeaveNotice> getNoticesForStudentInDateRange(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Validate parent access to create notice for student
     */
    boolean validateParentAccess(Long parentId, Long studentId);

    /**
     * Inner class for notice statistics
     */
    class NoticeStatistics {
        private long totalNotices;
        private long pendingNotices;
        private long acknowledgedNotices;
        private long fullDayNotices;
        private long lateNotices;
        private long earlyNotices;

        // Constructors, getters, setters
        public NoticeStatistics() {}

        public NoticeStatistics(long totalNotices, long pendingNotices, long acknowledgedNotices,
                               long fullDayNotices, long lateNotices, long earlyNotices) {
            this.totalNotices = totalNotices;
            this.pendingNotices = pendingNotices;
            this.acknowledgedNotices = acknowledgedNotices;
            this.fullDayNotices = fullDayNotices;
            this.lateNotices = lateNotices;
            this.earlyNotices = earlyNotices;
        }

        // Getters and setters
        public long getTotalNotices() { return totalNotices; }
        public void setTotalNotices(long totalNotices) { this.totalNotices = totalNotices; }

        public long getPendingNotices() { return pendingNotices; }
        public void setPendingNotices(long pendingNotices) { this.pendingNotices = pendingNotices; }

        public long getAcknowledgedNotices() { return acknowledgedNotices; }
        public void setAcknowledgedNotices(long acknowledgedNotices) { this.acknowledgedNotices = acknowledgedNotices; }

        public long getFullDayNotices() { return fullDayNotices; }
        public void setFullDayNotices(long fullDayNotices) { this.fullDayNotices = fullDayNotices; }

        public long getLateNotices() { return lateNotices; }
        public void setLateNotices(long lateNotices) { this.lateNotices = lateNotices; }

        public long getEarlyNotices() { return earlyNotices; }
        public void setEarlyNotices(long earlyNotices) { this.earlyNotices = earlyNotices; }
    }
}