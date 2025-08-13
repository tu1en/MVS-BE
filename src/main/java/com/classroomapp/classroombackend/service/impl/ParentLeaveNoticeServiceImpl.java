package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.ParentLeaveNotice;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentLeaveNoticeRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.service.ParentLeaveNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ParentLeaveNoticeService
 * Based on PARENT_ROLE_SPEC.md - Core feature for leave notifications
 */
@Service
@Slf4j
@Transactional
public class ParentLeaveNoticeServiceImpl implements ParentLeaveNoticeService {

    private final ParentLeaveNoticeRepository leaveNoticeRepository;
    private final StudentParentRepository studentParentRepository;

    @Autowired
    public ParentLeaveNoticeServiceImpl(ParentLeaveNoticeRepository leaveNoticeRepository,
                                       StudentParentRepository studentParentRepository) {
        this.leaveNoticeRepository = leaveNoticeRepository;
        this.studentParentRepository = studentParentRepository;
    }

    @Override
    public ParentLeaveNotice createLeaveNotice(ParentLeaveNotice notice) {
        log.info("Creating leave notice for student {} by parent {}", notice.getStudentId(), notice.getParentId());
        
        // Validate parent-student relationship
        if (!validateParentAccess(notice.getParentId(), notice.getStudentId())) {
            throw new IllegalArgumentException("Parent does not have access to this student");
        }
        
        // Validate notice constraints
        validateNoticeConstraints(notice);
        
        // Check for overlapping notices
        if (hasOverlappingNotice(notice.getStudentId(), notice.getDate(), notice.getType(),
                                notice.getArriveAt(), notice.getLeaveAt())) {
            throw new IllegalArgumentException("Overlapping notice already exists for this time period");
        }
        
        // Set timestamps
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        notice.setStatus(ParentLeaveNotice.NoticeStatus.SENT);
        
        ParentLeaveNotice savedNotice = leaveNoticeRepository.save(notice);
        log.info("Created leave notice with ID: {}", savedNotice.getId());
        
        return savedNotice;
    }

    @Override
    public ParentLeaveNotice createFullDayNotice(Long parentId, Long studentId, LocalDate date, 
                                                ParentLeaveNotice.ReasonCode reasonCode, String note) {
        log.info("Creating FULL_DAY notice for student {} on {}", studentId, date);
        
        ParentLeaveNotice notice = new ParentLeaveNotice(parentId, studentId, 
                                                        ParentLeaveNotice.NoticeType.FULL_DAY, 
                                                        date, reasonCode, note);
        return createLeaveNotice(notice);
    }

    @Override
    public ParentLeaveNotice createLateNotice(Long parentId, Long studentId, LocalDate date, 
                                             LocalTime arriveAt, ParentLeaveNotice.ReasonCode reasonCode, String note) {
        log.info("Creating LATE notice for student {} on {} at {}", studentId, date, arriveAt);
        
        ParentLeaveNotice notice = new ParentLeaveNotice(parentId, studentId, date, arriveAt, reasonCode, note);
        return createLeaveNotice(notice);
    }

    @Override
    public ParentLeaveNotice createEarlyNotice(Long parentId, Long studentId, LocalDate date, 
                                              LocalTime leaveAt, ParentLeaveNotice.ReasonCode reasonCode, String note) {
        log.info("Creating EARLY notice for student {} on {} at {}", studentId, date, leaveAt);
        
        ParentLeaveNotice notice = new ParentLeaveNotice(parentId, studentId, date, leaveAt, reasonCode, note);
        return createLeaveNotice(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParentLeaveNotice> getNoticeById(Long noticeId) {
        return leaveNoticeRepository.findById(noticeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesByParentId(Long parentId) {
        return leaveNoticeRepository.findByParentIdOrderByCreatedAtDesc(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesByStudentId(Long studentId) {
        return leaveNoticeRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesByParentAndStudent(Long parentId, Long studentId) {
        return leaveNoticeRepository.findByParentIdAndStudentIdOrderByCreatedAtDesc(parentId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesByDateRange(LocalDate startDate, LocalDate endDate) {
        return leaveNoticeRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesByDate(LocalDate date) {
        return leaveNoticeRepository.findByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getPendingNotices() {
        return leaveNoticeRepository.findPendingNotices();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getPendingNoticesByStudentId(Long studentId) {
        return leaveNoticeRepository.findPendingNoticesByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getPendingNoticesByDate(LocalDate date) {
        return leaveNoticeRepository.findPendingNoticesByDate(date);
    }

    @Override
    public ParentLeaveNotice acknowledgeNotice(Long noticeId, Long acknowledgedByUserId) {
        log.info("Acknowledging notice {} by user {}", noticeId, acknowledgedByUserId);
        
        ParentLeaveNotice notice = leaveNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new IllegalArgumentException("Notice not found: " + noticeId));
        
        if (notice.isAcknowledged()) {
            throw new IllegalArgumentException("Notice already acknowledged");
        }
        
        notice.acknowledge(acknowledgedByUserId);
        
        ParentLeaveNotice savedNotice = leaveNoticeRepository.save(notice);
        log.info("Acknowledged notice: {}", savedNotice.getId());
        
        return savedNotice;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverlappingNotice(Long studentId, LocalDate date, ParentLeaveNotice.NoticeType type,
                                       LocalTime arriveAt, LocalTime leaveAt) {
        List<ParentLeaveNotice> overlapping = leaveNoticeRepository.findOverlappingNotices(
            studentId, date, type, arriveAt, leaveAt);
        return !overlapping.isEmpty();
    }

    @Override
    public void validateNoticeConstraints(ParentLeaveNotice notice) {
        // Validate date is not in the past
        if (notice.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create notice for past date");
        }
        
        // Validate time constraints based on type
        switch (notice.getType()) {
            case LATE:
                if (notice.getArriveAt() == null) {
                    throw new IllegalArgumentException("LATE notice must have arrive time");
                }
                if (notice.getLeaveAt() != null) {
                    throw new IllegalArgumentException("LATE notice cannot have leave time");
                }
                break;
            case EARLY:
                if (notice.getLeaveAt() == null) {
                    throw new IllegalArgumentException("EARLY notice must have leave time");
                }
                if (notice.getArriveAt() != null) {
                    throw new IllegalArgumentException("EARLY notice cannot have arrive time");
                }
                break;
            case FULL_DAY:
                if (notice.getArriveAt() != null || notice.getLeaveAt() != null) {
                    throw new IllegalArgumentException("FULL_DAY notice cannot have arrive or leave time");
                }
                break;
        }
        
        // Validate reason code
        if (notice.getReasonCode() == null) {
            throw new IllegalArgumentException("Reason code is required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesForTeacherReview(List<Long> studentIds) {
        return leaveNoticeRepository.findByStudentIds(studentIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getAcknowledgedNoticesForAttendance(Long studentId, LocalDate date) {
        return leaveNoticeRepository.findAcknowledgedNoticesForAttendance(studentId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingNoticesByParentId(Long parentId) {
        return leaveNoticeRepository.countPendingNoticesByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getTodayNotices() {
        return leaveNoticeRepository.findTodayNotices(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getFutureNotices() {
        return leaveNoticeRepository.findFutureNotices(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getRecentNotices(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return leaveNoticeRepository.findRecentNotices(since);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeStatistics getNoticeStatisticsForParent(Long parentId) {
        List<Object[]> statusCounts = leaveNoticeRepository.countByStatusForParent(parentId);
        List<Object[]> typeCounts = leaveNoticeRepository.countByTypeForParent(parentId);
        
        long totalNotices = leaveNoticeRepository.countByParentId(parentId);
        long pendingNotices = countPendingNoticesByParentId(parentId);
        long acknowledgedNotices = totalNotices - pendingNotices;
        
        // Extract type counts
        long fullDayNotices = 0, lateNotices = 0, earlyNotices = 0;
        for (Object[] typeCount : typeCounts) {
            ParentLeaveNotice.NoticeType type = (ParentLeaveNotice.NoticeType) typeCount[0];
            Long count = (Long) typeCount[1];
            
            switch (type) {
                case FULL_DAY: fullDayNotices = count; break;
                case LATE: lateNotices = count; break;
                case EARLY: earlyNotices = count; break;
            }
        }
        
        return new NoticeStatistics(totalNotices, pendingNotices, acknowledgedNotices,
                                   fullDayNotices, lateNotices, earlyNotices);
    }

    @Override
    public void deleteNotice(Long noticeId, Long parentId) {
        log.info("Deleting notice {} by parent {}", noticeId, parentId);
        
        ParentLeaveNotice notice = leaveNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new IllegalArgumentException("Notice not found: " + noticeId));
        
        // Validate parent owns this notice
        if (!notice.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Parent does not own this notice");
        }
        
        // Check if notice can be deleted (only if not acknowledged and future date)
        if (notice.isAcknowledged()) {
            throw new IllegalArgumentException("Cannot delete acknowledged notice");
        }
        
        if (notice.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot delete notice for past date");
        }
        
        leaveNoticeRepository.delete(notice);
        log.info("Deleted notice: {}", noticeId);
    }

    @Override
    public ParentLeaveNotice updateNoticeStatus(Long noticeId, ParentLeaveNotice.NoticeStatus status) {
        log.info("Updating notice {} status to {}", noticeId, status);
        
        ParentLeaveNotice notice = leaveNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new IllegalArgumentException("Notice not found: " + noticeId));
        
        notice.setStatus(status);
        notice.setUpdatedAt(LocalDateTime.now());
        
        ParentLeaveNotice savedNotice = leaveNoticeRepository.save(notice);
        log.info("Updated notice status: {}", savedNotice.getId());
        
        return savedNotice;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> searchNotices(String searchTerm) {
        // This would need a more sophisticated search implementation
        // For now, return empty list
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesForParentInDateRange(Long parentId, LocalDate startDate, LocalDate endDate) {
        return leaveNoticeRepository.findByParentIdAndDateRange(parentId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLeaveNotice> getNoticesForStudentInDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        return leaveNoticeRepository.findByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateParentAccess(Long parentId, Long studentId) {
        return studentParentRepository.existsActiveRelationship(parentId, studentId);
    }
}