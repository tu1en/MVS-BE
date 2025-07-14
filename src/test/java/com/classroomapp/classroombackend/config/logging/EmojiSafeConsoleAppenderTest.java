package com.classroomapp.classroombackend.config.logging;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test for EmojiSafeConsoleAppender
 */
class EmojiSafeConsoleAppenderTest {

    @Test
    void testEmojiReplacements() {
        Map<String, String> replacements = EmojiSafeConsoleAppender.getEmojiReplacements();
        
        // Verify key emoji replacements
        assertEquals("[SEARCH]", replacements.get("🔍"));
        assertEquals("[CONFIG]", replacements.get("🔧"));
        assertEquals("[OK]", replacements.get("✅"));
        assertEquals("[ERROR]", replacements.get("❌"));
        assertEquals("[WARN]", replacements.get("⚠️"));
        assertEquals("[INFO]", replacements.get("ℹ️"));
        
        // Verify we have a reasonable number of replacements
        assertTrue(replacements.size() >= 10, "Should have at least 10 emoji replacements");
    }
    
    @Test
    void testCustomEmojiReplacement() {
        // Add a custom replacement
        EmojiSafeConsoleAppender.addEmojiReplacement("🎯", "[CUSTOM_TARGET]");
        
        Map<String, String> replacements = EmojiSafeConsoleAppender.getEmojiReplacements();
        assertEquals("[CUSTOM_TARGET]", replacements.get("🎯"));
    }
    
    @Test
    void testAppenderCreation() {
        // Test that appender can be created without errors
        EmojiSafeConsoleAppender<Object> appender = new EmojiSafeConsoleAppender<>();
        assertNotNull(appender);
        
        // Test emoji replacement setting
        appender.setEmojiReplacementEnabled(true);
        appender.setEmojiReplacementEnabled(false);
        
        // Should not throw any exceptions
        assertTrue(true);
    }
}
