package com.classroomapp.classroombackend.util;

/**
 * Utility class for logging with cross-platform emoji support
 * Provides text alternatives for emoji characters to ensure compatibility across different console environments
 */
public class LoggingUtils {

    // Platform detection
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("windows");
    
    // Emoji constants with text alternatives
    public static final String SEARCH = IS_WINDOWS ? "[SEARCH]" : "ðŸ”";
    public static final String CONFIG = IS_WINDOWS ? "[CONFIG]" : "ðŸ”§";
    public static final String SUCCESS = IS_WINDOWS ? "[OK]" : "âœ…";
    public static final String ERROR = IS_WINDOWS ? "[ERROR]" : "âŒ";
    public static final String WARNING = IS_WINDOWS ? "[WARN]" : "âš ï¸";
    public static final String INFO = IS_WINDOWS ? "[INFO]" : "â„¹ï¸";
    public static final String REPORT = IS_WINDOWS ? "[REPORT]" : "ðŸ“‹";
    public static final String TARGET = IS_WINDOWS ? "[TARGET]" : "ðŸŽ¯";
    public static final String LAUNCH = IS_WINDOWS ? "[LAUNCH]" : "ðŸš€";
    public static final String SAVE = IS_WINDOWS ? "[SAVE]" : "ðŸ’¾";
    public static final String REFRESH = IS_WINDOWS ? "[REFRESH]" : "ðŸ”„";
    public static final String STAR = IS_WINDOWS ? "[STAR]" : "â­";
    public static final String CELEBRATION = IS_WINDOWS ? "[SUCCESS]" : "ðŸŽ‰";
    public static final String HOT = IS_WINDOWS ? "[HOT]" : "ðŸ”¥";
    public static final String IDEA = IS_WINDOWS ? "[IDEA]" : "ðŸ’¡";
    public static final String TOOLS = IS_WINDOWS ? "[TOOLS]" : "ðŸ› ï¸";
    public static final String CHART = IS_WINDOWS ? "[CHART]" : "ðŸ“Š";
    public static final String SECURE = IS_WINDOWS ? "[SECURE]" : "ðŸ”’";
    public static final String FEATURE = IS_WINDOWS ? "[FEATURE]" : "ðŸŒŸ";
    public static final String FAST = IS_WINDOWS ? "[FAST]" : "âš¡";
    public static final String TEST = IS_WINDOWS ? "[TEST]" : "ðŸ§ª";
    public static final String NOTE = IS_WINDOWS ? "[NOTE]" : "ðŸ“";
    public static final String FOLDER = IS_WINDOWS ? "[FOLDER]" : "ðŸ“";
    public static final String MEMO = IS_WINDOWS ? "[MEMO]" : "ðŸ“";
    public static final String FILE = IS_WINDOWS ? "[FILE]" : "ðŸ“";
    public static final String EXPERIMENT = IS_WINDOWS ? "[EXPERIMENT]" : "ðŸ§ª";
    public static final String PARTY = IS_WINDOWS ? "[PARTY]" : "ðŸŽ‰";
    public static final String CHECKMARK = IS_WINDOWS ? "[CHECK]" : "âœ…";
    
    /**
     * Get platform-appropriate emoji or text alternative
     * @param emoji the emoji character
     * @param textAlternative the text alternative
     * @return emoji on non-Windows platforms, text alternative on Windows
     */
    public static String getIcon(String emoji, String textAlternative) {
        return IS_WINDOWS ? textAlternative : emoji;
    }
    
    /**
     * Format a log message with platform-appropriate icons
     * @param icon the icon constant from this class
     * @param message the log message
     * @return formatted message with icon
     */
    public static String formatMessage(String icon, String message) {
        return icon + " " + message;
    }
    
    /**
     * Check if running on Windows platform
     * @return true if Windows, false otherwise
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }
    
    /**
     * Get platform information for logging
     * @return platform description
     */
    public static String getPlatformInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        
        return String.format("%s %s (%s)", osName, osVersion, osArch);
    }
    
    /**
     * Get emoji support information
     * @return description of emoji support
     */
    public static String getEmojiSupportInfo() {
        if (IS_WINDOWS) {
            return "Text alternatives enabled for Windows console compatibility";
        } else {
            return "Native emoji support enabled for Unix/Linux terminals";
        }
    }
}
