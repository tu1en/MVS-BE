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
        logger.info("ðŸ§ª Testing emoji character encoding...");
        
        // Test specific emojis that are causing issues
        String magnifyingGlass = "ðŸ”";
        String wrench = "ðŸ”§";
        String checkMark = "âœ…";
        String crossMark = "âŒ";
        String warning = "âš ï¸";
        String info = "â„¹ï¸";
        
        logger.info("Direct emoji logging:");
        logger.info("ðŸ” Magnifying glass emoji");
        logger.info("ðŸ”§ Wrench emoji");
        logger.info("âœ… Check mark emoji");
        logger.info("âŒ Cross mark emoji");
        logger.info("âš ï¸ Warning emoji");
        logger.info("â„¹ï¸ Info emoji");
        
        // Test Unicode escape sequences
        logger.info("Unicode escape sequence logging:");
        logger.info("\uD83D\uDD0D Magnifying glass (Unicode escape)");
        logger.info("\uD83D\uDD27 Wrench (Unicode escape)");
        logger.info("\u2705 Check mark (Unicode escape)");
        logger.info("\u274C Cross mark (Unicode escape)");
        logger.info("\u26A0\uFE0F Warning (Unicode escape)");
        logger.info("\u2139\uFE0F Info (Unicode escape)");
        
        // Test byte representation
        testEmojiBytes(magnifyingGlass, "ðŸ”");
        testEmojiBytes(wrench, "ðŸ”§");
        testEmojiBytes(checkMark, "âœ…");
        testEmojiBytes(crossMark, "âŒ");
        testEmojiBytes(warning, "âš ï¸");
        testEmojiBytes(info, "â„¹ï¸");
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
        logger.info("ðŸ” System encoding properties:");
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
        
        logger.info("ðŸ” Logback configuration:");
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
        logger.info("ðŸ” Operating System: {}", osName);
        
        if (osName.toLowerCase().contains("windows")) {
            logger.info("ðŸ” Windows-specific encoding tests:");
            
            // Test console code page
            try {
                Process process = Runtime.getRuntime().exec("chcp");
                process.waitFor();
                logger.info("Console code page command executed");
            } catch (Exception e) {
                logger.warn("Could not check console code page: {}", e.getMessage());
            }
            
            // Test different emoji representations
            logger.info("Windows emoji test: ðŸ”ðŸ”§âœ…âŒâš ï¸â„¹ï¸");
            logger.info("Windows Unicode test: \\uD83D\\uDD0D\\uD83D\\uDD27\\u2705\\u274C\\u26A0\\u2139");
        }
    }
    
    @Test
    void testCharacterRanges() {
        logger.info("ðŸ” Testing different Unicode character ranges:");
        
        // Basic Latin
        logger.info("Basic Latin: ABC abc 123");
        
        // Vietnamese diacritics
        logger.info("Vietnamese: Tiáº¿ng Viá»‡t vá»›i cÃ¡c kÃ½ tá»± Ä‘áº·c biá»‡t");
        
        // Symbols and punctuation
        logger.info("Symbols: Â©Â®â„¢â‚¬Â£Â¥Â§Â¶â€ â€¡â€¢â€¦â€°");
        
        // Emoji (various ranges)
        logger.info("Emoji faces: ðŸ˜€ðŸ˜ƒðŸ˜„ðŸ˜ðŸ˜†ðŸ˜…ðŸ˜‚ðŸ¤£");
        logger.info("Emoji objects: ðŸ”ðŸ”§âš™ï¸ðŸ› ï¸ðŸ“±ðŸ’»ðŸ–¥ï¸");
        logger.info("Emoji symbols: âœ…âŒâš ï¸â„¹ï¸ðŸ”´ðŸŸ¢ðŸŸ¡");
        
        // Mathematical symbols
        logger.info("Math symbols: âˆ‘âˆâˆ«âˆ†âˆ‡âˆ‚âˆšâˆžâ‰ˆâ‰ â‰¤â‰¥");
        
        // Box drawing characters
        logger.info("Box drawing: â”Œâ”€â”â”‚â””â”€â”˜");
    }
}
