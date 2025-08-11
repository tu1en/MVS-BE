package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.ClassEntity.ClassStatus;
import com.classroomapp.classroombackend.repository.ClassRepository;

import lombok.RequiredArgsConstructor;

/**
 * Tự động đồng bộ trạng thái lớp theo ngày:
 * - Trước ngày bắt đầu: PLANNING
 * - Trong khoảng [startDate, endDate]: ACTIVE
 * - Sau ngày kết thúc: COMPLETED
 * Bỏ qua lớp đã CANCELLED.
 */
@Service
@RequiredArgsConstructor
public class ClassStatusSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ClassStatusSchedulerService.class);

    private final ClassRepository classRepository;
    private final ClassService classService;

    /**
     * Chạy mỗi ngày lúc 00:10. Cron: sec min hour day month weekday
     */
    @Scheduled(cron = "0 10 0 * * ?")
    @Transactional
    public void updateClassStatusesDaily() {
        updateClassStatuses();
    }

    /**
     * Cho phép gọi thủ công (controller hoặc test) để đồng bộ ngay.
     */
    @Transactional
    public void updateClassStatuses() {
        LocalDate today = LocalDate.now();

        List<ClassEntity> toCheck = new ArrayList<>();
        toCheck.addAll(classRepository.findByStatusOrderByCreatedAtDesc(ClassStatus.PLANNING));
        toCheck.addAll(classRepository.findByStatusOrderByCreatedAtDesc(ClassStatus.ACTIVE));

        int updated = 0;
        for (ClassEntity entity : toCheck) {
            if (entity.getStatus() == ClassStatus.CANCELLED) {
                continue; // Bỏ qua đã hủy
            }

            LocalDate start = entity.getStartDate();
            LocalDate end = entity.getEndDate();
            ClassStatus current = entity.getStatus();
            ClassStatus desired = current;

            if (start != null && end != null) {
                if (today.isBefore(start)) {
                    desired = ClassStatus.PLANNING;
                } else if ((today.isEqual(start) || today.isAfter(start)) && (today.isBefore(end) || today.isEqual(end))) {
                    desired = ClassStatus.ACTIVE;
                } else if (today.isAfter(end)) {
                    desired = ClassStatus.COMPLETED;
                }
            } else if (start != null) {
                // Không có endDate: coi như ACTIVE từ ngày bắt đầu trở đi
                desired = today.isBefore(start) ? ClassStatus.PLANNING : ClassStatus.ACTIVE;
            } else if (end != null) {
                // Không có startDate: nếu đã quá endDate thì COMPLETED, còn lại giữ nguyên
                if (today.isAfter(end)) desired = ClassStatus.COMPLETED;
            }

            if (desired != current) {
                try {
                    classService.updateClassStatus(entity.getId(), desired.name());
                    updated++;
                } catch (Exception e) {
                    logger.error("Failed to auto-update class status for id {}: {}", entity.getId(), e.getMessage());
                }
            }
        }

        logger.info("Class status daily sync completed. Updated {} records.", updated);
    }
}


