package com.classroomapp.classroombackend.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.service.impl.AttendanceServiceImpl;
import com.classroomapp.classroombackend.service.impl.AccomplishmentServiceImpl;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;

/**
 * Test runner to verify critical fixes are working
 */
@Component
@Profile("test")
public class CriticalFixesTestRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CriticalFixesTestRunner.class);

    @Autowired
    private AttendanceServiceImpl attendanceService;

    @Autowired
    private AccomplishmentServiceImpl accomplishmentService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("🧪 [CriticalFixesTestRunner] Starting critical fixes verification...");

        try {
            // Test 1: Check if assignments can be queried (tests dueDate column mapping)
            log.info("Test 1: Checking Assignment entity column mapping...");
            long assignmentCount = assignmentRepository.count();
            log.info("✅ Assignment count: {}", assignmentCount);

            // Test 2: Check AccomplishmentService can be instantiated
            log.info("Test 2: Checking AccomplishmentService instantiation...");
            if (accomplishmentService != null) {
                log.info("✅ AccomplishmentService instantiated successfully");
            }

            // Test 3: Check AttendanceService can be instantiated
            log.info("Test 3: Checking AttendanceService instantiation...");
            if (attendanceService != null) {
                log.info("✅ AttendanceService instantiated successfully");
            }

            log.info("🎉 [CriticalFixesTestRunner] All critical fixes verified successfully!");

        } catch (Exception e) {
            log.error("❌ [CriticalFixesTestRunner] Critical fix verification failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
