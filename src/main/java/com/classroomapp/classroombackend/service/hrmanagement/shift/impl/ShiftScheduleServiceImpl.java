package com.classroomapp.classroombackend.service.hrmanagement.shift.impl;

import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftScheduleRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của ShiftScheduleService
 * Xử lý business logic cho shift schedule management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftScheduleServiceImpl implements ShiftScheduleService {

    private final ShiftScheduleRepository shiftScheduleRepository;

    @Override
    public ShiftSchedule createSchedule(ShiftSchedule schedule) {
        log.info("Tạo shift schedule mới: {}", schedule.getScheduleName());

        validateSchedule(schedule);

        // Kiểm tra conflicts
        ScheduleConflictResult conflicts = validateScheduleConflicts(schedule);
        if (conflicts.hasConflict()) {
            log.warn("Phát hiện conflicts khi tạo schedule: {}", conflicts.getMessage());
        }

        ShiftSchedule saved = shiftScheduleRepository.save(schedule);
        log.info("Đã tạo shift schedule với ID: {}", saved.getId());

        // Gửi notification
        sendScheduleNotifications(saved, ScheduleNotificationType.SCHEDULE_CREATED);

        return saved;
    }

    @Override
    public ShiftSchedule updateSchedule(Long id, ShiftSchedule schedule) {
        log.info("Cập nhật shift schedule ID: {}", id);

        ShiftSchedule existing = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy schedule với ID: " + id));

        if (!existing.isEditable()) {
            throw new BusinessLogicException("Không thể chỉnh sửa schedule đã xuất bản");
        }

        validateSchedule(schedule);

        // Kiểm tra conflicts (exclude current schedule)
        ScheduleConflictResult conflicts = validateScheduleConflicts(schedule);
        if (conflicts.hasConflict()) {
            log.warn("Phát hiện conflicts khi cập nhật schedule: {}", conflicts.getMessage());
        }

        // Update fields
        existing.setScheduleName(schedule.getScheduleName());
        existing.setDescription(schedule.getDescription());
        existing.setStartDate(schedule.getStartDate());
        existing.setEndDate(schedule.getEndDate());
        existing.setScheduleType(schedule.getScheduleType());

        ShiftSchedule updated = shiftScheduleRepository.save(existing);
        log.info("Đã cập nhật shift schedule ID: {}", id);

        // Gửi notification
        sendScheduleNotifications(updated, ScheduleNotificationType.SCHEDULE_UPDATED);

        return updated;
    }

    @Override
    public void deleteSchedule(Long id) {
        log.info("Xóa shift schedule ID: {}", id);

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy schedule với ID: " + id));

        if (schedule.getStatus() == ShiftSchedule.ScheduleStatus.PUBLISHED) {
            throw new BusinessLogicException("Không thể xóa schedule đã xuất bản");
        }

        if (schedule.getTotalAssignments() > 0) {
            throw new BusinessLogicException("Không thể xóa schedule có assignments");
        }

        shiftScheduleRepository.delete(schedule);
        log.info("Đã xóa shift schedule ID: {}", id);
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
        log.info("Xuất bản schedule ID: {} bởi user {}", id, publisher.getId());

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy schedule với ID: " + id));

        if (!schedule.canPublish()) {
            throw new BusinessLogicException("Schedule không thể xuất bản trong trạng thái hiện tại");
        }

        schedule.publish(publisher);
        ShiftSchedule published = shiftScheduleRepository.save(schedule);

        // Gửi notification
        sendScheduleNotifications(published, ScheduleNotificationType.SCHEDULE_PUBLISHED);

        log.info("Đã xuất bản schedule ID: {}", id);
        return published;
    }

    @Override
    public ShiftSchedule archiveSchedule(Long id) {
        log.info("Lưu trữ schedule ID: {}", id);

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy schedule với ID: " + id));

        schedule.archive();
        ShiftSchedule archived = shiftScheduleRepository.save(schedule);

        // Gửi notification
        sendScheduleNotifications(archived, ScheduleNotificationType.SCHEDULE_ARCHIVED);

        log.info("Đã lưu trữ schedule ID: {}", id);
        return archived;
    }

    @Override
    public void cancelSchedule(Long id, String reason) {
        log.info("Hủy schedule ID: {} với lý do: {}", id, reason);

        ShiftSchedule schedule = shiftScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy schedule với ID: " + id));

        schedule.cancel();
        shiftScheduleRepository.save(schedule);

        // Gửi notification
        sendScheduleNotifications(schedule, ScheduleNotificationType.SCHEDULE_CANCELLED);

        log.info("Đã hủy schedule ID: {}", id);
    }

    @Override
    public void validateSchedule(ShiftSchedule schedule) {
        if (schedule == null) {
            throw new BusinessLogicException("Schedule không được null");
        }

        if (!schedule.isValidSchedule()) {
            throw new BusinessLogicException("Thông tin schedule không hợp lệ");
        }

        if (schedule.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException("Ngày bắt đầu không thể là quá khứ");
        }

        if (schedule.getCreatedBy() == null) {
            throw new BusinessLogicException("Người tạo schedule không được null");
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
        log.info("Đã auto-archive {} old schedules", archived);
        return archived;
    }

    @Override
    public int cleanupOldDrafts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = shiftScheduleRepository.deleteOldEmptyDrafts(cutoffDate);
        log.info("Đã cleanup {} old draft schedules", deleted);
        return deleted;
    }

    @Override
    public ShiftSchedule copySchedule(Long sourceScheduleId, LocalDate newStartDate, String newName) {
        log.info("Copy schedule từ ID: {} với start date mới: {}", sourceScheduleId, newStartDate);

        ShiftSchedule source = shiftScheduleRepository.findById(sourceScheduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy source schedule với ID: " + sourceScheduleId));

        // Tạo schedule mới
        ShiftSchedule newSchedule = new ShiftSchedule();
        newSchedule.setScheduleName(newName);
        newSchedule.setDescription("Copy từ: " + source.getScheduleName());
        newSchedule.setStartDate(newStartDate);
        newSchedule.setEndDate(newStartDate.plusDays(source.getDurationInDays() - 1));
        newSchedule.setScheduleType(source.getScheduleType());
        newSchedule.setCreatedBy(source.getCreatedBy());

        ShiftSchedule copied = shiftScheduleRepository.save(newSchedule);
        log.info("Đã copy schedule với ID mới: {}", copied.getId());

        return copied;
    }

    @Override
    public ShiftSchedule generateWeeklySchedule(LocalDate startDate, String name, User creator) {
        log.info("Generate weekly schedule từ ngày: {}", startDate);

        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setScheduleName(name);
        schedule.setDescription("Lịch làm việc hàng tuần tự động tạo");
        schedule.setStartDate(startDate);
        schedule.setEndDate(startDate.plusDays(6)); // 7 ngày
        schedule.setScheduleType(ShiftSchedule.ScheduleType.WEEKLY);
        schedule.setCreatedBy(creator);

        return shiftScheduleRepository.save(schedule);
    }

    @Override
    public ShiftSchedule generateMonthlySchedule(LocalDate startDate, String name, User creator) {
        log.info("Generate monthly schedule từ ngày: {}", startDate);

        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setScheduleName(name);
        schedule.setDescription("Lịch làm việc hàng tháng tự động tạo");
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
                                        archivedCount, totalAssignments);
        }
        
        return new ScheduleStatistics(0, 0, 0, 0, 0);
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
            throw new ResourceNotFoundException("Không tìm thấy schedule với ID: " + scheduleId);
        }
        log.debug("Đã cập nhật assignment count cho schedule ID: {}", scheduleId);
    }

    @Override
    public void bulkUpdateStatus(List<Long> scheduleIds, ShiftSchedule.ScheduleStatus status) {
        log.info("Bulk update status {} cho {} schedules", status, scheduleIds.size());
        
        for (Long scheduleId : scheduleIds) {
            try {
                shiftScheduleRepository.updateStatus(scheduleId, status);
            } catch (Exception e) {
                log.error("Lỗi khi update status cho schedule ID {}: {}", scheduleId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSchedule(Long scheduleId, String format) {
        // TODO: Implement export functionality
        throw new BusinessLogicException("Export functionality chưa được implement");
    }

    @Override
    public void sendScheduleNotifications(ShiftSchedule schedule, ScheduleNotificationType type) {
        log.info("Gửi notification {} cho schedule ID: {}", type, schedule.getId());
        // TODO: Implement notification sending logic
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleConflictResult validateScheduleConflicts(ShiftSchedule schedule) {
        List<ShiftSchedule> conflicts = findOverlappingSchedules(
            schedule.getStartDate(), schedule.getEndDate(), schedule.getId());

        if (conflicts.isEmpty()) {
            return new ScheduleConflictResult(false, conflicts, "Không có xung đột", 
                                            ScheduleConflictResult.ConflictSeverity.LOW);
        }

        String message = String.format("Phát hiện %d schedule xung đột thời gian", conflicts.size());
        return new ScheduleConflictResult(true, conflicts, message, 
                                        ScheduleConflictResult.ConflictSeverity.MEDIUM);
    }
}
