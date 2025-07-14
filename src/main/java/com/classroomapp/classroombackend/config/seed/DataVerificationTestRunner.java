package com.classroomapp.classroombackend.config.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Test runner để kiểm tra ComprehensiveDataVerifier
 * Chỉ chạy khi có profile "test-verification"
 */
@Component
@Order(1000) // Chạy sau tất cả seeders
public class DataVerificationTestRunner implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataVerificationTestRunner.class);
    
    @Autowired
    private ComprehensiveDataVerifier dataVerifier;
    
    @Override
    public void run(String... args) throws Exception {
        // Chỉ chạy nếu có argument "test-verification"
        boolean shouldRunTest = false;
        for (String arg : args) {
            if ("test-verification".equals(arg)) {
                shouldRunTest = true;
                break;
            }
        }
        
        if (!shouldRunTest) {
            return; // Skip nếu không có flag
        }
        
        log.info("🧪 ============== TESTING DATA VERIFICATION ==============");
        
        try {
            // Test comprehensive verification
            DataVerificationReport report = dataVerifier.runComprehensiveVerification();
            
            // Log kết quả
            log.info("📊 Test Results:");
            log.info("   Total Issues: {}", report.getTotalIssues());
            log.info("   Critical Issues: {}", report.getCriticalIssues());
            log.info("   Warning Issues: {}", report.getWarningIssues());
            log.info("   Info Issues: {}", report.getInfoIssues());
            
            // Log detailed report nếu có issues
            if (report.hasIssues()) {
                log.info("📋 Detailed Report:");
                log.info(report.getDetailedReport());
            }
            
            // Test JSON output
            log.info("📄 JSON Report: {}", report.toJson());
            
            // Test summary
            log.info("📝 Summary:\n{}", report.getSummary());
            
            log.info("✅ ============== DATA VERIFICATION TEST COMPLETED ==============");
            
        } catch (Exception e) {
            log.error("❌ Data verification test failed: {}", e.getMessage(), e);
        }
    }
}
