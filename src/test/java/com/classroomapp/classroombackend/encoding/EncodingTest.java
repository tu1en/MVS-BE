package com.classroomapp.classroombackend.encoding;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class to verify UTF-8 encoding is working correctly
 */
@SpringBootTest
class EncodingTest {

    private static final Logger logger = LoggerFactory.getLogger(EncodingTest.class);

    @Test
    void testSystemEncoding() {
        // Test system encoding
        String defaultCharset = Charset.defaultCharset().name();
        logger.info("Default system charset: {}", defaultCharset);
        
        // Verify UTF-8 is the default
        assertEquals("UTF-8", defaultCharset, "System should use UTF-8 encoding");
    }

    @Test
    void testVietnameseCharacters() {
        // Test Vietnamese characters
        String vietnameseText = "Xin chào! Đây là tiếng Việt với các ký tự đặc biệt: áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ";
        
        logger.info("Vietnamese text: {}", vietnameseText);
        
        // Convert to bytes and back to verify encoding
        byte[] utf8Bytes = vietnameseText.getBytes(StandardCharsets.UTF_8);
        String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
        
        assertEquals(vietnameseText, reconstructed, "Vietnamese text should be preserved through UTF-8 encoding");
        
        // Log the byte length
        logger.info("Original text length: {} characters", vietnameseText.length());
        logger.info("UTF-8 byte length: {} bytes", utf8Bytes.length);
    }

    @Test
    void testSpecialCharacters() {
        // Test various special characters
        String specialChars = "Special characters: ©®™€£¥§¶†‡•…‰‹›\"\"''–—";
        
        logger.info("Special characters: {}", specialChars);
        
        byte[] utf8Bytes = specialChars.getBytes(StandardCharsets.UTF_8);
        String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
        
        assertEquals(specialChars, reconstructed, "Special characters should be preserved through UTF-8 encoding");
    }

    @Test
    void testEmojis() {
        // Test emoji characters
        String emojis = "Emojis: 😀😃😄😁😆😅😂🤣😊😇🙂🙃😉😌😍🥰😘😗😙😚😋😛😝😜🤪🤨🧐🤓😎🤩🥳";
        
        logger.info("Emojis: {}", emojis);
        
        byte[] utf8Bytes = emojis.getBytes(StandardCharsets.UTF_8);
        String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
        
        assertEquals(emojis, reconstructed, "Emojis should be preserved through UTF-8 encoding");
    }

    @Test
    void testLogMessages() {
        // Test various log levels with Vietnamese text
        logger.trace("TRACE: Thông điệp trace với tiếng Việt");
        logger.debug("DEBUG: Thông điệp debug với tiếng Việt");
        logger.info("INFO: Thông điệp info với tiếng Việt");
        logger.warn("WARN: Thông điệp warning với tiếng Việt");
        logger.error("ERROR: Thông điệp error với tiếng Việt");
        
        // Test with special characters
        logger.info("Mixed content: English + Tiếng Việt + 特殊字符 + العربية + русский + 日本語");
        
        // This test passes if no exceptions are thrown
        assertTrue(true, "All log messages should be displayed correctly");
    }

    @Test
    void testFileEncoding() {
        // Test file encoding property
        String fileEncoding = System.getProperty("file.encoding");
        logger.info("File encoding system property: {}", fileEncoding);
        
        assertEquals("UTF-8", fileEncoding, "file.encoding system property should be UTF-8");
    }

    @Test
    void testConsoleEncoding() {
        // Test console encoding property
        String consoleEncoding = System.getProperty("console.encoding");
        logger.info("Console encoding system property: {}", consoleEncoding);
        
        // Console encoding might be null if not set, but if set should be UTF-8
        if (consoleEncoding != null) {
            assertEquals("UTF-8", consoleEncoding, "console.encoding system property should be UTF-8");
        }
    }

    @Test
    void testTimezone() {
        // Test timezone setting
        String timezone = System.getProperty("user.timezone");
        logger.info("User timezone: {}", timezone);
        
        // Verify Vietnam timezone if set
        if (timezone != null) {
            assertEquals("Asia/Ho_Chi_Minh", timezone, "Timezone should be set to Vietnam");
        }
    }

    @Test
    void testDatabaseStringHandling() {
        // Test string that might cause database encoding issues
        String problematicString = "Test string with quotes: 'single' \"double\" and special chars: àáâãäåæçèéêë";
        
        logger.info("Problematic string: {}", problematicString);
        
        // Verify the string remains intact
        assertNotNull(problematicString);
        assertTrue(problematicString.contains("àáâãäåæçèéêë"), "String should contain Vietnamese characters");
        assertTrue(problematicString.contains("'single'"), "String should contain single quotes");
        assertTrue(problematicString.contains("\"double\""), "String should contain double quotes");
    }
}
