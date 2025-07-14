package com.classroomapp.classroombackend.config.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.util.LoggingUtils;

/**
 * Service để quản lý data verification process
 */
@Service
public class DataVerificationService {
    
    private static final Logger log = LoggerFactory.getLogger(DataVerificationService.class);
    
    @Autowired
    private ComprehensiveDataVerifier dataVerifier;
    
    /**
     * Chạy verification sau khi application startup hoàn tất
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1000) // Chạy sau tất cả các seeder
    public void runPostStartupVerification() {
        log.info("🔍 Running post-startup data verification...");
        
        try {
            DataVerificationReport report = dataVerifier.runComprehensiveVerification();
            
            // Log summary
            log.info(report.getSummary());
            
            // Log detailed report nếu có issues
            if (report.hasIssues()) {
                log.warn("📋 Detailed verification report:\n{}", report.getDetailedReport());
            }
            
            // Cảnh báo nếu có critical issues
            if (report.hasCriticalIssues()) {
                log.error("❌ CRITICAL DATA INTEGRITY ISSUES DETECTED!");
                log.error("Application may not function correctly. Please review and fix the issues above.");
                
                // Có thể throw exception để fail startup nếu cần
                // throw new DataIntegrityException("Critical data integrity issues found");
            } else {
                log.info("✅ Data verification completed successfully - no critical issues found");
            }
            
        } catch (Exception e) {
            log.error("❌ Data verification failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Chạy verification manually (có thể gọi từ API hoặc admin interface)
     */
    public DataVerificationReport runManualVerification() {
        log.info(LoggingUtils.SEARCH + " Running manual data verification...");
        return dataVerifier.runComprehensiveVerification();
    }
    
    /**
     * Chạy verification và trả về JSON report
     */
    public String runVerificationAndGetJsonReport() {
        DataVerificationReport report = runManualVerification();
        return report.toJson();
    }
    
    /**
     * Kiểm tra xem có critical issues không
     */
    public boolean hasCriticalIssues() {
        DataVerificationReport report = dataVerifier.runComprehensiveVerification();
        return report.hasCriticalIssues();
    }
    
    /**
     * Lấy quick health check
     */
    public String getHealthStatus() {
        try {
            DataVerificationReport report = dataVerifier.runComprehensiveVerification();
            
            if (report.hasCriticalIssues()) {
                return "CRITICAL - " + report.getCriticalIssues() + " critical issues found";
            } else if (report.hasWarningIssues()) {
                return "WARNING - " + report.getWarningIssues() + " warning issues found";
            } else {
                return "HEALTHY - No issues found";
            }
        } catch (Exception e) {
            return "ERROR - Verification failed: " + e.getMessage();
        }
    }
}
