package com.classroomapp.classroombackend.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for LoggingUtils
 */
class LoggingUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUtilsTest.class);

    @Test
    void testPlatformDetection() {
        // Test platform detection
        boolean isWindows = LoggingUtils.isWindows();
        String osName = System.getProperty("os.name", "").toLowerCase();
        
        assertEquals(osName.contains("windows"), isWindows);
        
        logger.info("Platform detection test:");
        logger.info("OS Name: {}", osName);
        logger.info("Is Windows: {}", isWindows);
    }
    
    @Test
    void testEmojiConstants() {
        // Test that emoji constants are not null
        assertNotNull(LoggingUtils.SEARCH);
        assertNotNull(LoggingUtils.CONFIG);
        assertNotNull(LoggingUtils.SUCCESS);
        assertNotNull(LoggingUtils.ERROR);
        assertNotNull(LoggingUtils.WARNING);
        assertNotNull(LoggingUtils.INFO);
        
        logger.info("Emoji constants test:");
        logger.info("Search: {}", LoggingUtils.SEARCH);
        logger.info("Config: {}", LoggingUtils.CONFIG);
        logger.info("Success: {}", LoggingUtils.SUCCESS);
        logger.info("Error: {}", LoggingUtils.ERROR);
        logger.info("Warning: {}", LoggingUtils.WARNING);
        logger.info("Info: {}", LoggingUtils.INFO);
    }
    
    @Test
    void testGetIcon() {
        String result = LoggingUtils.getIcon("🔍", "[SEARCH]");
        assertNotNull(result);
        
        // On Windows, should return text alternative
        // On non-Windows, should return emoji
        if (LoggingUtils.isWindows()) {
            assertEquals("[SEARCH]", result);
        } else {
            assertEquals("🔍", result);
        }
        
        logger.info("Icon test result: {}", result);
    }
    
    @Test
    void testFormatMessage() {
        String formatted = LoggingUtils.formatMessage(LoggingUtils.SUCCESS, "Test message");
        assertNotNull(formatted);
        assertTrue(formatted.contains("Test message"));
        
        logger.info("Formatted message: {}", formatted);
    }
    
    @Test
    void testPlatformInfo() {
        String platformInfo = LoggingUtils.getPlatformInfo();
        assertNotNull(platformInfo);
        assertFalse(platformInfo.isEmpty());
        
        logger.info("Platform info: {}", platformInfo);
    }
    
    @Test
    void testEmojiSupportInfo() {
        String emojiInfo = LoggingUtils.getEmojiSupportInfo();
        assertNotNull(emojiInfo);
        assertFalse(emojiInfo.isEmpty());
        
        logger.info("Emoji support info: {}", emojiInfo);
    }
    
    @Test
    void testAllEmojiConstants() {
        logger.info("Testing all emoji constants:");
        logger.info(LoggingUtils.formatMessage(LoggingUtils.SEARCH, "Search functionality"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.CONFIG, "Configuration setup"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.SUCCESS, "Operation successful"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.ERROR, "Error occurred"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.WARNING, "Warning message"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.INFO, "Information message"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.REPORT, "Report generated"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.TARGET, "Target achieved"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.LAUNCH, "Application launched"));
        logger.info(LoggingUtils.formatMessage(LoggingUtils.TEST, "Test completed"));
    }
}
