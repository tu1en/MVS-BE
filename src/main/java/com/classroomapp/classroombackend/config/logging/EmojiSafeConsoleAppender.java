package com.classroomapp.classroombackend.config.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.ConsoleAppender;

/**
 * Custom console appender that handles emoji characters properly on Windows
 * Replaces problematic emoji with text alternatives for better console compatibility
 */
public class EmojiSafeConsoleAppender<E> extends ConsoleAppender<E> {

    private static final Map<String, String> EMOJI_REPLACEMENTS = new HashMap<>();
    
    static {
        // Map problematic emojis to text alternatives
        EMOJI_REPLACEMENTS.put("ðŸ”", "[SEARCH]");
        EMOJI_REPLACEMENTS.put("ðŸ”§", "[CONFIG]");
        EMOJI_REPLACEMENTS.put("âœ…", "[OK]");
        EMOJI_REPLACEMENTS.put("âŒ", "[ERROR]");
        EMOJI_REPLACEMENTS.put("âš ï¸", "[WARN]");
        EMOJI_REPLACEMENTS.put("â„¹ï¸", "[INFO]");
        EMOJI_REPLACEMENTS.put("ðŸ“‹", "[REPORT]");
        EMOJI_REPLACEMENTS.put("ðŸŽ¯", "[TARGET]");
        EMOJI_REPLACEMENTS.put("ðŸš€", "[LAUNCH]");
        EMOJI_REPLACEMENTS.put("ðŸ’¾", "[SAVE]");
        EMOJI_REPLACEMENTS.put("ðŸ”„", "[REFRESH]");
        EMOJI_REPLACEMENTS.put("â­", "[STAR]");
        EMOJI_REPLACEMENTS.put("ðŸŽ‰", "[SUCCESS]");
        EMOJI_REPLACEMENTS.put("ðŸ”¥", "[HOT]");
        EMOJI_REPLACEMENTS.put("ðŸ’¡", "[IDEA]");
        EMOJI_REPLACEMENTS.put("ðŸ› ï¸", "[TOOLS]");
        EMOJI_REPLACEMENTS.put("ðŸ“Š", "[CHART]");
        EMOJI_REPLACEMENTS.put("ðŸ”’", "[SECURE]");
        EMOJI_REPLACEMENTS.put("ðŸŒŸ", "[FEATURE]");
        EMOJI_REPLACEMENTS.put("âš¡", "[FAST]");
    }
    
    private boolean emojiReplacementEnabled = false;
    
    public EmojiSafeConsoleAppender() {
        super();
        // Detect if we're on Windows and enable emoji replacement
        String osName = System.getProperty("os.name", "").toLowerCase();
        this.emojiReplacementEnabled = osName.contains("windows");
        
        if (emojiReplacementEnabled) {
            addInfo("EmojiSafeConsoleAppender: Windows detected, emoji replacement enabled");
        } else {
            addInfo("EmojiSafeConsoleAppender: Non-Windows OS, emoji replacement disabled");
        }
    }
    
    @Override
    protected void writeOut(E event) throws IOException {
        if (emojiReplacementEnabled && encoder != null) {
            // Get the encoded bytes
            byte[] byteArray = encoder.encode(event);

            // Convert bytes to string, replace emojis, then back to bytes
            String logMessage = new String(byteArray, StandardCharsets.UTF_8);
            String processedMessage = replaceEmojis(logMessage);
            byte[] processedBytes = processedMessage.getBytes(StandardCharsets.UTF_8);

            // Write processed bytes to output stream
            OutputStream outputStream = getOutputStream();
            outputStream.write(processedBytes);
            outputStream.flush();
        } else {
            // On non-Windows systems, use original behavior
            super.writeOut(event);
        }
    }
    
    private String replaceEmojis(String message) {
        String result = message;
        for (Map.Entry<String, String> entry : EMOJI_REPLACEMENTS.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    /**
     * Set whether emoji replacement is enabled
     * @param enabled true to enable emoji replacement, false to disable
     */
    public void setEmojiReplacementEnabled(boolean enabled) {
        this.emojiReplacementEnabled = enabled;
        addInfo("EmojiSafeConsoleAppender: Emoji replacement " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Add custom emoji replacement
     * @param emoji the emoji character to replace
     * @param replacement the text replacement
     */
    public static void addEmojiReplacement(String emoji, String replacement) {
        EMOJI_REPLACEMENTS.put(emoji, replacement);
    }
    
    /**
     * Get current emoji replacements map
     * @return map of emoji to text replacements
     */
    public static Map<String, String> getEmojiReplacements() {
        return new HashMap<>(EMOJI_REPLACEMENTS);
    }
}
