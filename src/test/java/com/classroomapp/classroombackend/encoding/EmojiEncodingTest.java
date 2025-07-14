package com.classroomapp.classroombackend.encoding;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Focused test to isolate emoji rendering issues
 */
@SpringBootTest
class EmojiEncodingTest {

    private static final Logger logger = LoggerFactory.getLogger(EmojiEncodingTest.class);

    @Test
    void testEmojiCharacters() {
        logger.info("🧪 Testing emoji character encoding...");
        
        // Test specific emojis that are causing issues
        String magnifyingGlass = "🔍";
        String wrench = "🔧";
        String checkMark = "✅";
        String crossMark = "❌";
        String warning = "⚠️";
        String info = "ℹ️";
        
        logger.info("Direct emoji logging:");
        logger.info("🔍 Magnifying glass emoji");
        logger.info("🔧 Wrench emoji");
        logger.info("✅ Check mark emoji");
        logger.info("❌ Cross mark emoji");
        logger.info("⚠️ Warning emoji");
        logger.info("ℹ️ Info emoji");
        
        // Test Unicode escape sequences
        logger.info("Unicode escape sequence logging:");
        logger.info("\uD83D\uDD0D Magnifying glass (Unicode escape)");
        logger.info("\uD83D\uDD27 Wrench (Unicode escape)");
        logger.info("\u2705 Check mark (Unicode escape)");
        logger.info("\u274C Cross mark (Unicode escape)");
        logger.info("\u26A0\uFE0F Warning (Unicode escape)");
        logger.info("\u2139\uFE0F Info (Unicode escape)");
        
        // Test byte representation
        testEmojiBytes(magnifyingGlass, "🔍");
        testEmojiBytes(wrench, "🔧");
        testEmojiBytes(checkMark, "✅");
        testEmojiBytes(crossMark, "❌");
        testEmojiBytes(warning, "⚠️");
        testEmojiBytes(info, "ℹ️");
    }
    
    private void testEmojiBytes(String emoji, String description) {
        byte[] utf8Bytes = emoji.getBytes(StandardCharsets.UTF_8);
        String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
        
        logger.info("Emoji: {} | UTF-8 bytes: {} | Length: {} chars | Byte length: {} | Reconstructed: {}", 
            description, 
            bytesToHex(utf8Bytes), 
            emoji.length(), 
            utf8Bytes.length, 
            reconstructed);
        
        assertEquals(emoji, reconstructed, "Emoji should be preserved through UTF-8 encoding");
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
    
    @Test
    void testSystemEncodingProperties() {
        logger.info("🔍 System encoding properties:");
        logger.info("file.encoding: {}", System.getProperty("file.encoding"));
        logger.info("console.encoding: {}", System.getProperty("console.encoding"));
        logger.info("sun.jnu.encoding: {}", System.getProperty("sun.jnu.encoding"));
        logger.info("Default charset: {}", StandardCharsets.UTF_8.name());
        logger.info("JVM default charset: {}", java.nio.charset.Charset.defaultCharset().name());
    }
    
    @Test
    void testLogbackConfiguration() {
        // Test if logback is properly configured
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.LoggerContext loggerContext = rootLogger.getLoggerContext();
        
        logger.info("🔍 Logback configuration:");
        logger.info("Logger context: {}", loggerContext.getName());
        logger.info("Configuration file: {}", loggerContext.getConfigurationLock());
        
        // Check appenders
        rootLogger.iteratorForAppenders().forEachRemaining(appender -> {
            logger.info("Appender: {} | Class: {}", appender.getName(), appender.getClass().getSimpleName());
            
            if (appender instanceof ch.qos.logback.core.ConsoleAppender) {
                ch.qos.logback.core.ConsoleAppender<?> consoleAppender = (ch.qos.logback.core.ConsoleAppender<?>) appender;
                ch.qos.logback.core.encoder.Encoder<?> encoder = consoleAppender.getEncoder();
                if (encoder instanceof ch.qos.logback.classic.encoder.PatternLayoutEncoder) {
                    ch.qos.logback.classic.encoder.PatternLayoutEncoder patternEncoder = (ch.qos.logback.classic.encoder.PatternLayoutEncoder) encoder;
                    logger.info("Console encoder charset: {}", patternEncoder.getCharset());
                    logger.info("Console encoder pattern: {}", patternEncoder.getPattern());
                }
            }
        });
    }
    
    @Test
    void testWindowsConsoleCompatibility() {
        // Test Windows-specific console issues
        String osName = System.getProperty("os.name");
        logger.info("🔍 Operating System: {}", osName);
        
        if (osName.toLowerCase().contains("windows")) {
            logger.info("🔍 Windows-specific encoding tests:");
            
            // Test console code page
            try {
                Process process = Runtime.getRuntime().exec("chcp");
                process.waitFor();
                logger.info("Console code page command executed");
            } catch (Exception e) {
                logger.warn("Could not check console code page: {}", e.getMessage());
            }
            
            // Test different emoji representations
            logger.info("Windows emoji test: 🔍🔧✅❌⚠️ℹ️");
            logger.info("Windows Unicode test: \\uD83D\\uDD0D\\uD83D\\uDD27\\u2705\\u274C\\u26A0\\u2139");
        }
    }
    
    @Test
    void testCharacterRanges() {
        logger.info("🔍 Testing different Unicode character ranges:");
        
        // Basic Latin
        logger.info("Basic Latin: ABC abc 123");
        
        // Vietnamese diacritics
        logger.info("Vietnamese: Tiếng Việt với các ký tự đặc biệt");
        
        // Symbols and punctuation
        logger.info("Symbols: ©®™€£¥§¶†‡•…‰");
        
        // Emoji (various ranges)
        logger.info("Emoji faces: 😀😃😄😁😆😅😂🤣");
        logger.info("Emoji objects: 🔍🔧⚙️🛠️📱💻🖥️");
        logger.info("Emoji symbols: ✅❌⚠️ℹ️🔴🟢🟡");
        
        // Mathematical symbols
        logger.info("Math symbols: ∑∏∫∆∇∂√∞≈≠≤≥");
        
        // Box drawing characters
        logger.info("Box drawing: ┌─┐│└─┘");
    }
}
