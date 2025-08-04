package com.classroomapp.classroombackend.service.hrmanagement.shift.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.ScheduleNotificationType;
import com.classroomapp.classroombackend.dto.hrmanagement.ScheduleConflictResult;
import com.classroomapp.classroombackend.dto.hrmanagement.ScheduleStatistics;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftScheduleRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation cá»§a ShiftScheduleService
 * Xá»­ lÃ½ business logic cho shift schedule management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftScheduleServiceImpl implements ShiftScheduleService {

    private final ShiftScheduleRepository shiftScheduleRepository;

    @Override
    public ShiftSchedule createSchedule(ShiftSchedule schedule) {
        log.info("Táº¡o shift schedule má»›i: {}", schedule.getScheduleName());

        validateSchedule(schedule);

        // Kiá»ƒm tra conflicts
        ScheduleConflictResult conflicts = validateScheduleConflicts(schedule);
        if (conflicts.isHasConflict()) {
            log.warn("PhÃ¡t hiá»‡n conflicts khi táº¡o schedule: {}", conflicts.getMessage());
        }

        ShiftSchedule saved = shiftScheduleRepository.save(schedule);
        log.info("ÄÃ£ táº¡o shift schedule vá»›i ID: {}", saved.getId());

        // Gá»­i notification
        sendScheduleNotifications(saved, ScheduleNotificationType.SCHEDULE_CREATED);

        return saved;
    }

    @Override
    public ShiftSchedule updateSchedule(Long id, ShiftSchedule schedule) {
        log.info("Cáº­p nháº­t shift schedule ID: {}", id);

        ShiftSchedule existing = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + id));

        if (!existing.isEditable()) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ chá»‰nh sá»­a schedule Ä‘Ã£ xuáº¥t báº£n");
        }

        validateSchedule(schedule);

        // Kiá»ƒm tra conflicts (exclude current schedule)
        ScheduleConflictResult conflicts = validateScheduleConflicts(schedule);
        if (conflicts.isHasConflict()) {
            log.warn("PhÃ¡t hiá»‡n conflicts khi cáº­p nháº­t schedule: {}", conflicts.getMessage());
        }

        // Update fields
        existing.setScheduleName(schedule.getScheduleName());
        existing.setDescription(schedule.getDescription());
        existing.setStartDate(schedule.getStartDate());
        existing.setEndDate(schedule.getEndDate());
        existing.setScheduleType(schedule.getScheduleType());

        ShiftSchedule updated = shiftScheduleRepository.save(existing);
        log.info("ÄÃ£ cáº­p nháº­t shift schedule ID: {}", id);

        // Gá»­i notification
        sendScheduleNotifications(updated, ScheduleNotificationType.SCHEDULE_UPDATED);

        return updated;
    }

    @Override
    public void deleteSchedule(Long id) {
        log.info("XÃ³a shift schedule ID: {}", id);

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + id));

        if (schedule.getStatus() == ShiftSchedule.ScheduleStatus.PUBLISHED) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ xÃ³a schedule Ä‘Ã£ xuáº¥t báº£n");
        }

        if (schedule.getTotalAssignments() > 0) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ xÃ³a schedule cÃ³ assignments");
        }

        shiftScheduleRepository.delete(schedule);
        log.info("ÄÃ£ xÃ³a shift schedule ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftSchedule> findById(Long id) {
        return shiftScheduleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findByStatus(ShiftSchedule.ScheduleStatus status) {
        return shiftScheduleRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findByType(ShiftSchedule.ScheduleType scheduleType) {
        return shiftScheduleRepository.findByScheduleTypeOrderByStartDateDesc(scheduleType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findActiveSchedules() {
        return shiftScheduleRepository.findActiveSchedules(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftSchedule> findActiveScheduleForDate(LocalDate date) {
        return shiftScheduleRepository.findActiveScheduleForDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftSchedule> searchSchedules(ShiftSchedule.ScheduleStatus status,
                                              ShiftSchedule.ScheduleType scheduleType,
                                              Long createdById, LocalDate startDate, LocalDate endDate,
                                              String search, Pageable pageable) {
        return shiftScheduleRepository.searchSchedules(status, scheduleType, createdById, 
                                                      startDate, endDate, search, pageable);
    }

    @Override
    public ShiftSchedule publishSchedule(Long id, User publisher) {
        log.info("Xuáº¥t báº£n schedule ID: {} bá»Ÿi user {}", id, publisher.getId());

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + id));

        if (!schedule.canPublish()) {
            throw new BusinessLogicException("Schedule khÃ´ng thá»ƒ xuáº¥t báº£n trong tráº¡ng thÃ¡i hiá»‡n táº¡i");
        }

        schedule.publish(publisher);
        ShiftSchedule published = shiftScheduleRepository.save(schedule);

        // Gá»­i notification
        sendScheduleNotifications(published, ScheduleNotificationType.SCHEDULE_PUBLISHED);

        log.info("ÄÃ£ xuáº¥t báº£n schedule ID: {}", id);
        return published;
    }

    @Override
    public ShiftSchedule archiveSchedule(Long id) {
        log.info("LÆ°u trá»¯ schedule ID: {}", id);

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + id));

        schedule.archive();
        ShiftSchedule archived = shiftScheduleRepository.save(schedule);

        // Gá»­i notification
        sendScheduleNotifications(archived, ScheduleNotificationType.SCHEDULE_ARCHIVED);

        log.info("ÄÃ£ lÆ°u trá»¯ schedule ID: {}", id);
        return archived;
    }

    @Override
    public void cancelSchedule(Long id, String reason) {
        log.info("Há»§y schedule ID: {} vá»›i lÃ½ do: {}", id, reason);

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + id));

        schedule.cancel();
        shiftScheduleRepository.save(schedule);

        // Gá»­i notification
        sendScheduleNotifications(schedule, ScheduleNotificationType.SCHEDULE_CANCELLED);

        log.info("ÄÃ£ há»§y schedule ID: {}", id);
    }

    @Override
    public void validateSchedule(ShiftSchedule schedule) {
        if (schedule == null) {
            throw new BusinessLogicException("Schedule khÃ´ng Ä‘Æ°á»£c null");
        }

        if (!schedule.isValidSchedule()) {
            throw new BusinessLogicException("ThÃ´ng tin schedule khÃ´ng há»£p lá»‡");
        }

        if (schedule.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException("NgÃ y báº¯t Ä‘áº§u khÃ´ng thá»ƒ lÃ  quÃ¡ khá»©");
        }

        if (schedule.getCreatedBy() == null) {
            throw new BusinessLogicException("NgÆ°á»i táº¡o schedule khÃ´ng Ä‘Æ°á»£c null");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findOverlappingSchedules(LocalDate startDate, LocalDate endDate, Long excludeId) {
        return shiftScheduleRepository.findOverlappingSchedules(startDate, endDate, excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findSchedulesNeedingArchive(int daysAfterEnd) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysAfterEnd);
        return shiftScheduleRepository.findSchedulesNeedingArchive(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findUpcomingSchedules(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate notificationDate = today.plusDays(daysAhead);
        return shiftScheduleRepository.findUpcomingSchedules(today, notificationDate);
    }

    @Override
    public int autoArchiveOldSchedules(int daysAfterEnd) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysAfterEnd);
        int archived = shiftScheduleRepository.archiveOldSchedules(cutoffDate);
        log.info("ÄÃ£ auto-archive {} old schedules", archived);
        return archived;
    }

    @Override
    public int cleanupOldDrafts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = shiftScheduleRepository.deleteOldEmptyDrafts(cutoffDate);
        log.info("ÄÃ£ cleanup {} old draft schedules", deleted);
        return deleted;
    }

    @Override
    public ShiftSchedule copySchedule(Long sourceScheduleId, LocalDate newStartDate, String newName) {
        log.info("Copy schedule tá»« ID: {} vá»›i start date má»›i: {}", sourceScheduleId, newStartDate);

        ShiftSchedule source = shiftScheduleRepository.findById(sourceScheduleId)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y source schedule vá»›i ID: " + sourceScheduleId));

        // Táº¡o schedule má»›i
        ShiftSchedule newSchedule = new ShiftSchedule();
        newSchedule.setScheduleName(newName);
        newSchedule.setDescription("Copy tá»«: " + source.getScheduleName());
        newSchedule.setStartDate(newStartDate);
        newSchedule.setEndDate(newStartDate.plusDays(source.getDurationInDays() - 1));
        newSchedule.setScheduleType(source.getScheduleType());
        newSchedule.setCreatedBy(source.getCreatedBy());

        ShiftSchedule copied = shiftScheduleRepository.save(newSchedule);
        log.info("ÄÃ£ copy schedule vá»›i ID má»›i: {}", copied.getId());

        return copied;
    }

    @Override
    public ShiftSchedule generateWeeklySchedule(LocalDate startDate, String name, User creator) {
        log.info("Generate weekly schedule tá»« ngÃ y: {}", startDate);

        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setScheduleName(name);
        schedule.setDescription("Lá»‹ch lÃ m viá»‡c hÃ ng tuáº§n tá»± Ä‘á»™ng táº¡o");
        schedule.setStartDate(startDate);
        schedule.setEndDate(startDate.plusDays(6)); // 7 ngÃ y
        schedule.setScheduleType(ShiftSchedule.ScheduleType.WEEKLY);
        schedule.setCreatedBy(creator);

        return shiftScheduleRepository.save(schedule);
    }

    @Override
    public ShiftSchedule generateMonthlySchedule(LocalDate startDate, String name, User creator) {
        log.info("Generate monthly schedule tá»« ngÃ y: {}", startDate);

        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setScheduleName(name);
        schedule.setDescription("Lá»‹ch lÃ m viá»‡c hÃ ng thÃ¡ng tá»± Ä‘á»™ng táº¡o");
        schedule.setStartDate(monthStart);
        schedule.setEndDate(monthEnd);
        schedule.setScheduleType(ShiftSchedule.ScheduleType.MONTHLY);
        schedule.setCreatedBy(creator);

        return shiftScheduleRepository.save(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleStatistics getScheduleStatistics(LocalDate startDate, LocalDate endDate) {
        Object[] stats = shiftScheduleRepository.getScheduleStatistics(startDate, endDate);
        
        if (stats != null && stats.length >= 3) {
            long totalSchedules = ((Number) stats[0]).longValue();
            long totalAssignments = ((Number) stats[1]).longValue();
            double avgAssignments = ((Number) stats[2]).doubleValue();
            
            // Get status counts
            List<Object[]> statusCounts = shiftScheduleRepository.countByStatus();
            long draftCount = 0, publishedCount = 0, archivedCount = 0;
            
            for (Object[] row : statusCounts) {
                ShiftSchedule.ScheduleStatus status = (ShiftSchedule.ScheduleStatus) row[0];
                long count = ((Number) row[1]).longValue();
                
                switch (status) {
                    case DRAFT: draftCount = count; break;
                    case PUBLISHED: publishedCount = count; break;
                    case ARCHIVED: archivedCount = count; break;
                }
            }
            
            return new ScheduleStatistics(totalSchedules, draftCount, publishedCount, 
                                        archivedCount, 0L, totalAssignments, 
                                        totalSchedules > 0 ? (double)totalAssignments / totalSchedules : 0.0);
        }
        
        return new ScheduleStatistics(0L, 0L, 0L, 0L, 0L, 0L, 0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSchedule> findSchedulesWithMostAssignments(int limit) {
        return shiftScheduleRepository.findSchedulesWithMostAssignments(PageRequest.of(0, limit));
    }

    @Override
    public void updateAssignmentCount(Long scheduleId) {
        int updated = shiftScheduleRepository.updateAssignmentCount(scheduleId);
        if (updated == 0) {
            throw new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + scheduleId);
        }
        log.debug("ÄÃ£ cáº­p nháº­t assignment count cho schedule ID: {}", scheduleId);
    }

    @Override
    public void bulkUpdateStatus(List<Long> scheduleIds, ShiftSchedule.ScheduleStatus status) {
        log.info("Bulk update status {} cho {} schedules", status, scheduleIds.size());
        
        for (Long scheduleId : scheduleIds) {
            try {
                shiftScheduleRepository.updateStatus(scheduleId, status);
            } catch (Exception e) {
                log.error("Lá»—i khi update status cho schedule ID {}: {}", scheduleId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSchedule(Long scheduleId, String format) {
        // TODO: Implement export functionality
        throw new BusinessLogicException("Export functionality chÆ°a Ä‘Æ°á»£c implement");
    }

    @Override
    public void sendScheduleNotifications(ShiftSchedule schedule, ScheduleNotificationType type) {
        log.info("Gá»­i notification {} cho schedule ID: {}", type, schedule.getId());
        // TODO: Implement notification sending logic
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleConflictResult validateScheduleConflicts(ShiftSchedule schedule) {
        List<ShiftSchedule> conflicts = findOverlappingSchedules(
            schedule.getStartDate(), schedule.getEndDate(), schedule.getId());

        if (conflicts.isEmpty()) {
            return new ScheduleConflictResult(false, conflicts, "KhÃ´ng cÃ³ xung Ä‘á»™t", 
                                            ScheduleConflictResult.ConflictSeverity.LOW);
        }

        String message = String.format("PhÃ¡t hiá»‡n %d schedule xung Ä‘á»™t thá»i gian", conflicts.size());
        return new ScheduleConflictResult(true, conflicts, message, 
                                        ScheduleConflictResult.ConflictSeverity.MEDIUM);
    }

    @Override
    public List<ShiftSchedule> findByCreatedByUserId(Long userId) {
        log.info("Finding schedules created by user ID: {}", userId);
        return shiftScheduleRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }
}
