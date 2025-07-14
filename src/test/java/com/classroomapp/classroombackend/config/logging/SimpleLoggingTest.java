package com.classroomapp.classroombackend.config.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.classroomapp.classroombackend.util.LoggingUtils;

/**
 * Simple test to verify logging with LoggingUtils
 */
class SimpleLoggingTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggingTest.class);

    @Test
    void testLoggingWithUtils() {
        logger.info("=== Testing LoggingUtils Integration ===");
        
        // Test all LoggingUtils constants
        logger.info(LoggingUtils.SEARCH + " Search functionality test");
        logger.info(LoggingUtils.CONFIG + " Configuration test");
        logger.info(LoggingUtils.SUCCESS + " Success test");
        logger.info(LoggingUtils.ERROR + " Error test");
        logger.info(LoggingUtils.WARNING + " Warning test");
        logger.info(LoggingUtils.INFO + " Info test");
        logger.info(LoggingUtils.REPORT + " Report test");
        logger.info(LoggingUtils.TARGET + " Target test");
        logger.info(LoggingUtils.NOTE + " Note test");
        logger.info(LoggingUtils.FOLDER + " Folder test");
        logger.info(LoggingUtils.TEST + " Test functionality");
        logger.info(LoggingUtils.CELEBRATION + " Celebration test");
        
        // Test Vietnamese text with Unicode escapes
        String vietnamese = "Ti\u1EBFng Vi\u1EC7t"; // "Tiếng Việt"
        logger.info("Vietnamese text test: {}", vietnamese);
        
        // Test special characters with Unicode escapes
        String specialChars = "\u00A9\u00AE\u2122\u20AC\u00A3\u00A5"; // ©®™€£¥
        logger.info("Special characters test: {}", specialChars);
        
        logger.info(LoggingUtils.SUCCESS + " All logging tests completed successfully!");
    }
}
