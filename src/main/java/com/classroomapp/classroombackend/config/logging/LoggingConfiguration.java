package com.classroomapp.classroombackend.config.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.classroomapp.classroombackend.util.LoggingUtils;

/**
 * Configuration class for logging setup
 * Handles platform-specific logging configurations and emoji compatibility
 */
@Configuration
public class LoggingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);

    @EventListener(ApplicationReadyEvent.class)
    public void configureLogging() {
        logger.info(LoggingUtils.CONFIG + " Configuring platform-specific logging...");
        
        // Detect operating system
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = osName.contains("windows");
        
        logger.info(LoggingUtils.SEARCH + " Operating System detected: {}", osName);
        logger.info(LoggingUtils.SEARCH + " Windows platform: {}", isWindows);
        
        if (isWindows) {
            logger.info(LoggingUtils.CONFIG + " Windows detected - emoji-safe logging enabled");
            logger.info(LoggingUtils.NOTE + " Console output will use text alternatives for emojis");
            logger.info(LoggingUtils.FOLDER + " Log files will preserve original emoji characters");
            
            // Test emoji display
            testEmojiDisplay();
        } else {
            logger.info(LoggingUtils.CONFIG + " Unix/Linux detected - standard emoji logging enabled");
            logger.info(LoggingUtils.NOTE + " Console output will display emojis natively");
        }
        
        // Log encoding information
        logEncodingInfo();
    }
    
    private void testEmojiDisplay() {
        logger.info(LoggingUtils.TEST + " Testing emoji display compatibility:");
        logger.info(LoggingUtils.SEARCH + " Search icon test");
        logger.info(LoggingUtils.CONFIG + " Configuration icon test");
        logger.info(LoggingUtils.SUCCESS + " Success icon test");
        logger.info(LoggingUtils.ERROR + " Error icon test");
        logger.info(LoggingUtils.WARNING + " Warning icon test");
        logger.info(LoggingUtils.INFO + " Info icon test");
        logger.info(LoggingUtils.REPORT + " Report icon test");
        logger.info(LoggingUtils.TARGET + " Target icon test");

        logger.info(LoggingUtils.CELEBRATION + " Emoji display test completed!");
    }
    
    private void logEncodingInfo() {
        logger.info(LoggingUtils.SEARCH + " Current encoding configuration:");
        logger.info("   - file.encoding: {}", System.getProperty("file.encoding"));
        logger.info("   - console.encoding: {}", System.getProperty("console.encoding"));
        logger.info("   - Default charset: {}", java.nio.charset.Charset.defaultCharset().name());
        logger.info("   - UTF-8 support: {}", java.nio.charset.StandardCharsets.UTF_8.name());
        
        // Test Vietnamese characters
        String vietnameseTest = "Ti\u1EBFng Vi\u1EC7t"; // "Tiếng Việt" using Unicode escapes
        logger.info("   - Vietnamese test: {}", vietnameseTest);
        
        // Test special characters
        String specialChars = "Special: \u00A9\u00AE\u2122\u20AC\u00A3\u00A5"; // ©®™€£¥ using Unicode escapes
        logger.info("   - Special chars test: {}", specialChars);
    }
    
    /**
     * Get platform-specific logging recommendations
     */
    public static String getLoggingRecommendations() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = osName.contains("windows");
        
        if (isWindows) {
            return """
                Windows Logging Recommendations:
                1. Use emoji-safe console appender for better compatibility
                2. Set console code page to 65001 (UTF-8): chcp 65001
                3. Use Windows Terminal or PowerShell for better Unicode support
                4. Log files will preserve original emoji characters
                5. Consider using text alternatives in console output
                """;
        } else {
            return """
                Unix/Linux Logging Recommendations:
                1. Ensure terminal supports UTF-8 encoding
                2. Set LANG environment variable: export LANG=en_US.UTF-8
                3. Use modern terminal emulators with emoji support
                4. Both console and file output will display emojis natively
                """;
        }
    }
}
