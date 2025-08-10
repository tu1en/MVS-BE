package com.classroomapp.classroombackend.config;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Component để sửa lỗi encoding UTF-8 cho dữ liệu tiếng Việt trong database
 * Chạy sau khi ứng dụng khởi động để kiểm tra và sửa dữ liệu bị lỗi encoding
 */
@Component
@Order(999) // Chạy trước UTF8EncodingFixer
@Slf4j
public class DatabaseEncodingFixer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔧 Starting Database UTF-8 encoding fix...");
        
        try {
            // 1. Set database connection charset
            setDatabaseCharset();
            
            // 2. Fix existing data encoding issues
            fixUserDataEncoding();
            
            log.info("✅ Database UTF-8 encoding fix completed successfully.");
            
        } catch (Exception e) {
            log.error("❌ Failed to fix database encoding", e);
        }
    }

    /**
     * Thiết lập charset cho database connection
     */
    private void setDatabaseCharset() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("🔧 Setting database charset to UTF-8...");
            
            // Set connection charset to UTF-8
            try (PreparedStatement stmt = conn.prepareStatement("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci")) {
                stmt.execute();
                log.info("✅ Database connection charset set to UTF-8");
            }
            
            // Set character set results
            try (PreparedStatement stmt = conn.prepareStatement("SET character_set_results = utf8mb4")) {
                stmt.execute();
            }
            
            // Set character set client
            try (PreparedStatement stmt = conn.prepareStatement("SET character_set_client = utf8mb4")) {
                stmt.execute();
            }
            
            // Set character set connection
            try (PreparedStatement stmt = conn.prepareStatement("SET character_set_connection = utf8mb4")) {
                stmt.execute();
            }
            
        } catch (SQLException e) {
            log.warn("Could not set database charset: {}", e.getMessage());
        }
    }

    /**
     * Sửa lỗi encoding cho dữ liệu user (tên, email, etc.)
     */
    private void fixUserDataEncoding() {
        log.info("🔍 Fixing user data encoding issues...");
        
        try (Connection conn = dataSource.getConnection()) {
            // Fix users table
            fixTableEncoding(conn, "users", new String[]{"full_name", "email"}, "user_id");
            
            // Fix contracts table - Vietnamese character encoding
            fixTableEncoding(conn, "contracts", new String[]{"full_name", "email", "position", "address", "qualification", "subject", "contract_terms", "comments"}, "id");
            
            // Fix other tables if needed
            // fixTableEncoding(conn, "employees", new String[]{"employee_name", "position"}, "employee_id");
            
        } catch (SQLException e) {
            log.error("Failed to fix user data encoding", e);
        }
    }

    /**
     * Sửa lỗi encoding cho một bảng cụ thể
     */
    private void fixTableEncoding(Connection conn, String tableName, String[] textColumns, String idColumn) {
        log.info("🔧 Fixing encoding for table: {}", tableName);
        
        try {
            // Get all records that might have encoding issues
            String selectSql = "SELECT " + idColumn + ", " + String.join(", ", textColumns) + " FROM " + tableName;
            
            List<EncodingFixRecord> recordsToFix = new ArrayList<>();
            
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery()) {
                
                while (rs.next()) {
                    EncodingFixRecord record = new EncodingFixRecord();
                    record.id = rs.getString(idColumn);
                    record.tableName = tableName;
                    record.idColumn = idColumn;
                    
                    boolean needsFix = false;
                    for (String column : textColumns) {
                        String value = rs.getString(column);
                        if (value != null && hasEncodingIssues(value)) {
                            String fixedValue = fixEncodingIssues(value);
                            record.columnFixes.put(column, fixedValue);
                            needsFix = true;
                            
                            log.info("🔧 Found encoding issue in {}.{}: '{}' -> '{}'", 
                                tableName, column, value, fixedValue);
                        }
                    }
                    
                    if (needsFix) {
                        recordsToFix.add(record);
                    }
                }
            }
            
            // Apply fixes
            for (EncodingFixRecord record : recordsToFix) {
                applyEncodingFix(conn, record);
            }
            
            log.info("✅ Fixed {} records in table {}", recordsToFix.size(), tableName);
            
        } catch (SQLException e) {
            log.error("Failed to fix encoding for table: {}", tableName, e);
        }
    }

    /**
     * Kiểm tra xem text có vấn đề encoding không
     */
    private boolean hasEncodingIssues(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Check for common encoding issues
        return text.contains("?") || 
               text.contains("�") ||
               text.matches(".*[À-ỹ].*") == false && text.matches(".*[àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐ].*");
    }

    /**
     * Sửa lỗi encoding cho một chuỗi text
     */
    private String fixEncodingIssues(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            // Common Vietnamese encoding fixes
            String fixed = text;
            
            // Fix common character replacements
            fixed = fixed.replace("Ã¡", "á")
                        .replace("Ã ", "à")
                        .replace("áº£", "ả")
                        .replace("Ã£", "ã")
                        .replace("áº¡", "ạ")
                        .replace("Äƒ", "ă")
                        .replace("áº¯", "ắ")
                        .replace("áº±", "ằ")
                        .replace("áº³", "ẳ")
                        .replace("áºµ", "ẵ")
                        .replace("áº·", "ặ")
                        .replace("Ã¢", "â")
                        .replace("áº¥", "ấ")
                        .replace("áº§", "ầ")
                        .replace("áº©", "ẩ")
                        .replace("áº«", "ẫ")
                        .replace("áº­", "ậ")
                        .replace("Ã¨", "è")
                        .replace("Ã©", "é")
                        .replace("áº»", "ẻ")
                        .replace("áº½", "ẽ")
                        .replace("áº¹", "ẹ")
                        .replace("Ãª", "ê")
                        .replace("áº¿", "ế")
                        .replace("á»", "ề")
                        .replace("á»ƒ", "ể")
                        .replace("á»…", "ễ")
                        .replace("á»‡", "ệ")
                        .replace("Ã¬", "ì")
                        .replace("Ã­", "í")
                        .replace("á»‰", "ỉ")
                        .replace("Ä©", "ĩ")
                        .replace("á»‹", "ị")
                        .replace("Ã²", "ò")
                        .replace("Ã³", "ó")
                        .replace("á»", "ỏ")
                        .replace("Ãµ", "õ")
                        .replace("á»", "ọ")
                        .replace("Ã´", "ô")
                        .replace("á»'", "ố")
                        .replace("á»\"", "ồ")
                        .replace("á»•", "ổ")
                        .replace("á»—", "ỗ")
                        .replace("á»™", "ộ")
                        .replace("Æ¡", "ơ")
                        .replace("á»›", "ớ")
                        .replace("á»", "ờ")
                        .replace("á»Ÿ", "ở")
                        .replace("á»¡", "ỡ")
                        .replace("á»£", "ợ")
                        .replace("Ã¹", "ù")
                        .replace("Ãº", "ú")
                        .replace("á»§", "ủ")
                        .replace("Å©", "ũ")
                        .replace("á»¥", "ụ")
                        .replace("Æ°", "ư")
                        .replace("á»©", "ứ")
                        .replace("á»«", "ừ")
                        .replace("á»­", "ử")
                        .replace("á»¯", "ữ")
                        .replace("á»±", "ự")
                        .replace("á»³", "ỳ")
                        .replace("Ã½", "ý")
                        .replace("á»·", "ỷ")
                        .replace("á»¹", "ỹ")
                        .replace("á»µ", "ỵ")
                        .replace("Ä'", "đ")
                        .replace("Ä", "Đ");
            
            // Try ISO-8859-1 to UTF-8 conversion if still has issues
            if (hasEncodingIssues(fixed)) {
                try {
                    byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
                    fixed = new String(bytes, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    // Keep original if conversion fails
                    fixed = text;
                }
            }
            
            return fixed;
            
        } catch (Exception e) {
            log.warn("Could not fix encoding for text: {}", text, e);
            return text;
        }
    }

    /**
     * Áp dụng fix encoding cho một record
     */
    private void applyEncodingFix(Connection conn, EncodingFixRecord record) {
        try {
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(record.tableName).append(" SET ");
            List<String> setClauses = new ArrayList<>();
            
            for (String column : record.columnFixes.keySet()) {
                setClauses.add(column + " = ?");
            }
            
            updateSql.append(String.join(", ", setClauses));
            updateSql.append(" WHERE ").append(record.idColumn).append(" = ?");
            
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql.toString())) {
                int paramIndex = 1;
                
                for (String fixedValue : record.columnFixes.values()) {
                    updateStmt.setString(paramIndex++, fixedValue);
                }
                
                updateStmt.setString(paramIndex, record.id);
                updateStmt.executeUpdate();
                
                log.debug("✅ Applied encoding fix for record ID: {}", record.id);
            }
            
        } catch (SQLException e) {
            log.error("Failed to apply encoding fix for record ID: {}", record.id, e);
        }
    }

    /**
     * Helper class để lưu thông tin fix encoding
     */
    private static class EncodingFixRecord {
        String id;
        String tableName;
        String idColumn;
        java.util.Map<String, String> columnFixes = new java.util.HashMap<>();
    }
}
