package com.classroomapp.classroombackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled service để tự động đồng bộ dữ liệu giữa Manager và Teacher systems
 * Giải quyết vấn đề data inconsistency giữa ClassEntity và Classroom
 */
@Service
public class DataSyncSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncSchedulerService.class);

    @Autowired
    private ClassService classService;

    /**
     * Chạy sync tự động mỗi 30 phút để đảm bảo data consistency
     * Cron: sec min hour day month weekday
     */
    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional
    public void autoSyncClassesToClassrooms() {
        logger.info("🔄 Starting scheduled data synchronization...");
        
        try {
            classService.syncAllClassesToClassrooms();
            logger.info("✅ Scheduled data synchronization completed successfully");
        } catch (Exception e) {
            logger.error("❌ Scheduled data synchronization failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Chạy sync manual khi cần thiết
     */
    @Transactional
    public void manualSync() {
        logger.info("🔄 Starting manual data synchronization...");
        
        try {
            classService.syncAllClassesToClassrooms();
            logger.info("✅ Manual data synchronization completed successfully");
        } catch (Exception e) {
            logger.error("❌ Manual data synchronization failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sync một class cụ thể
     */
    @Transactional
    public void syncSpecificClass(Long classId) {
        logger.info("🔄 Starting sync for class ID: {}", classId);
        
        try {
            classService.syncClassToClassroom(classId);
            logger.info("✅ Sync completed for class ID: {}", classId);
        } catch (Exception e) {
            logger.error("❌ Sync failed for class ID {}: {}", classId, e.getMessage(), e);
            throw e;
        }
    }
}
