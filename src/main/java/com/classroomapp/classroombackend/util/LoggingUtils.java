package com.classroomapp.classroombackend.util;

/**
 * Utility class for logging with cross-platform emoji support
 * Provides text alternatives for emoji characters to ensure compatibility across different console environments
 */
public class LoggingUtils {

    // Platform detection
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("windows");
    
    // Emoji constants with text alternatives
    public static final String SEARCH = IS_WINDOWS ? "[SEARCH]" : "🔍";
    public static final String CONFIG = IS_WINDOWS ? "[CONFIG]" : "🔧";
    public static final String SUCCESS = IS_WINDOWS ? "[OK]" : "✅";
    public static final String ERROR = IS_WINDOWS ? "[ERROR]" : "❌";
    public static final String WARNING = IS_WINDOWS ? "[WARN]" : "⚠️";
    public static final String INFO = IS_WINDOWS ? "[INFO]" : "ℹ️";
    public static final String REPORT = IS_WINDOWS ? "[REPORT]" : "📋";
    public static final String TARGET = IS_WINDOWS ? "[TARGET]" : "🎯";
    public static final String LAUNCH = IS_WINDOWS ? "[LAUNCH]" : "🚀";
    public static final String SAVE = IS_WINDOWS ? "[SAVE]" : "💾";
    public static final String REFRESH = IS_WINDOWS ? "[REFRESH]" : "🔄";
    public static final String STAR = IS_WINDOWS ? "[STAR]" : "⭐";
    public static final String CELEBRATION = IS_WINDOWS ? "[SUCCESS]" : "🎉";
    public static final String HOT = IS_WINDOWS ? "[HOT]" : "🔥";
    public static final String IDEA = IS_WINDOWS ? "[IDEA]" : "💡";
    public static final String TOOLS = IS_WINDOWS ? "[TOOLS]" : "🛠️";
    public static final String CHART = IS_WINDOWS ? "[CHART]" : "📊";
    public static final String SECURE = IS_WINDOWS ? "[SECURE]" : "🔒";
    public static final String FEATURE = IS_WINDOWS ? "[FEATURE]" : "🌟";
    public static final String FAST = IS_WINDOWS ? "[FAST]" : "⚡";
    public static final String TEST = IS_WINDOWS ? "[TEST]" : "🧪";
    public static final String NOTE = IS_WINDOWS ? "[NOTE]" : "📝";
    public static final String FOLDER = IS_WINDOWS ? "[FOLDER]" : "📁";
    public static final String MEMO = IS_WINDOWS ? "[MEMO]" : "📝";
    public static final String FILE = IS_WINDOWS ? "[FILE]" : "📁";
    public static final String EXPERIMENT = IS_WINDOWS ? "[EXPERIMENT]" : "🧪";
    public static final String PARTY = IS_WINDOWS ? "[PARTY]" : "🎉";
    public static final String CHECKMARK = IS_WINDOWS ? "[CHECK]" : "✅";
    
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
