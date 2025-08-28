package com.classroomapp.classroombackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.service.PerformanceTestService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller để test hiệu năng và debug N+1 query problem
 * Chỉ sử dụng trong môi trường development
 */
@RestController
@RequestMapping("/api/test/performance")
@Slf4j
public class PerformanceTestController {

    @Autowired
    private PerformanceTestService performanceTestService;

    /**
     * Test hiệu năng cho một classroom cụ thể
     */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<String> testClassroomPerformance(@PathVariable Long classroomId) {
        log.info("Performance test requested for classroom ID: {}", classroomId);
        
        try {
            performanceTestService.runAllPerformanceTests(classroomId);
            return ResponseEntity.ok("Performance tests completed successfully for classroom ID: " + classroomId);
        } catch (Exception e) {
            log.error("Performance test failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Performance test failed: " + e.getMessage());
        }
    }

    /**
     * Test hiệu năng với classroom ID mặc định (để test nhanh)
     */
    @GetMapping("/quick-test")
    public ResponseEntity<String> quickPerformanceTest() {
        log.info("Quick performance test requested");
        
        try {
            // Sử dụng classroom ID mặc định (có thể thay đổi)
            Long defaultClassroomId = 1L;
            performanceTestService.runAllPerformanceTests(defaultClassroomId);
            return ResponseEntity.ok("Quick performance test completed successfully");
        } catch (Exception e) {
            log.error("Quick performance test failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Quick performance test failed: " + e.getMessage());
        }
    }
}
