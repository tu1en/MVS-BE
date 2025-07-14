package com.classroomapp.classroombackend.config.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test for LoggingConfiguration
 */
@SpringBootTest
class LoggingConfigurationTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfigurationTest.class);

    @Test
    void testLoggingConfiguration() {
        LoggingConfiguration config = new LoggingConfiguration();
        assertNotNull(config);
        
        // Test that configuration can be created without errors
        logger.info("LoggingConfiguration test started");
        
        // Test platform-specific recommendations
        String recommendations = LoggingConfiguration.getLoggingRecommendations();
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        logger.info("Platform recommendations: {}", recommendations);
        logger.info("LoggingConfiguration test completed successfully");
    }
    
    @Test
    void testUnicodeCharacters() {
        logger.info("Testing Unicode characters:");
        
        // Test Vietnamese text with Unicode escapes
        String vietnamese = "Ti\u1EBFng Vi\u1EC7t"; // "Tiếng Việt"
        logger.info("Vietnamese text: {}", vietnamese);
        assertEquals("Tiếng Việt", vietnamese);
        
        // Test special characters with Unicode escapes
        String specialChars = "\u00A9\u00AE\u2122\u20AC\u00A3\u00A5"; // ©®™€£¥
        logger.info("Special characters: {}", specialChars);
        assertEquals("©®™€£¥", specialChars);
        
        logger.info("Unicode character test completed");
    }
}
