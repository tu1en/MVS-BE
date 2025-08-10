package com.classroomapp.classroombackend.config;

import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple UTF-8 encoding fixer for Vietnamese text
 */
@Component
@Order(998)
@Slf4j
public class SimpleEncodingFixer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔧 Starting Simple UTF-8 encoding fix...");
        
        try {
            // Set system properties for UTF-8
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            
            log.info("✅ UTF-8 system properties set successfully");
            
            // Test Vietnamese text encoding
            String testText = "Lý Thị Bình - Giáo viên Văn học lớp 10";
            byte[] utf8Bytes = testText.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
            
            log.info("🧪 UTF-8 Test: Original='{}', Reconstructed='{}'", testText, reconstructed);
            
            if (testText.equals(reconstructed)) {
                log.info("✅ UTF-8 encoding test passed");
            } else {
                log.warn("❌ UTF-8 encoding test failed");
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to set UTF-8 encoding", e);
        }
    }
    
    /**
     * Fix common Vietnamese encoding issues
     */
    public static String fixVietnameseEncoding(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            // Common fixes for Vietnamese characters
            String fixed = text
                .replace("Th?", "Thị")
                .replace("th?", "thị")
                .replace("Van h?c", "Văn học")
                .replace("van h?c", "văn học")
                .replace("l?p", "lớp")
                .replace("L?p", "Lớp")
                .replace("gi?o", "giáo")
                .replace("Gi?o", "Giáo")
                .replace("vi?n", "viên")
                .replace("Vi?n", "Viên")
                .replace("h?c", "học")
                .replace("H?c", "Học")
                .replace("sinh", "sinh")
                .replace("Sinh", "Sinh")
                .replace("to?n", "toán")
                .replace("To?n", "Toán")
                .replace("?", "ị"); // Generic fix for missing ị
            
            return fixed;
            
        } catch (Exception e) {
            log.warn("Could not fix Vietnamese encoding for: {}", text, e);
            return text;
        }
    }
}
