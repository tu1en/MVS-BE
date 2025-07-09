package com.classroomapp.classroombackend.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating and fixing Vietnamese text encoding issues
 * 
 * This service helps detect corrupted Vietnamese characters and provides
 * methods to validate and fix encoding problems.
 */
@Slf4j
@Service
public class VietnameseTextValidationService {

    // Pattern to detect Vietnamese characters
    private static final Pattern VIETNAMESE_PATTERN = Pattern.compile(
        "[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ" +
        "ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ]"
    );

    // Pattern to detect corrupted characters (question marks)
    private static final Pattern CORRUPTION_PATTERN = Pattern.compile("\\?+");

    // Common Vietnamese words for validation
    private static final String[] COMMON_VIETNAMESE_WORDS = {
        "học", "sinh", "giáo", "viên", "lớp", "bài", "tập", "kiểm", "tra",
        "thông", "báo", "hệ", "thống", "quản", "lý", "nội", "dung",
        "thời", "gian", "địa", "điểm", "kết", "quả", "phân", "tích",
        "cải", "thiện", "hoàn", "thiện", "thực", "hiện", "chất", "lượng"
    };

    /**
     * Validates if text contains proper Vietnamese characters
     * 
     * @param text Text to validate
     * @return ValidationResult containing validation status and details
     */
    public ValidationResult validateVietnameseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ValidationResult(true, "Text is empty", null);
        }

        log.debug("🔍 Validating Vietnamese text: {}", text.substring(0, Math.min(text.length(), 50)));

        ValidationResult result = new ValidationResult();
        result.setOriginalText(text);

        // Check for corruption patterns
        if (CORRUPTION_PATTERN.matcher(text).find()) {
            result.setValid(false);
            result.setIssue("Text contains corrupted characters (?)");
            result.setCorruptionDetected(true);
            
            // Count corrupted characters
            long corruptedCount = text.chars().filter(ch -> ch == '?').count();
            result.setCorruptedCharacterCount((int) corruptedCount);
            
            log.warn("❌ Corrupted Vietnamese text detected: {} corrupted characters", corruptedCount);
        }

        // Check encoding integrity
        byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
        String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
        
        if (!text.equals(reconstructed)) {
            result.setValid(false);
            result.setIssue("Text encoding is corrupted");
            result.setEncodingIssue(true);
            log.warn("❌ Encoding issue detected in Vietnamese text");
        }

        // Check for Vietnamese character presence
        boolean hasVietnameseChars = VIETNAMESE_PATTERN.matcher(text).find();
        result.setContainsVietnameseCharacters(hasVietnameseChars);

        if (result.isValid()) {
            result.setIssue("Text is valid");
            log.debug("✅ Vietnamese text validation passed");
        }

        return result;
    }

    /**
     * Attempts to fix corrupted Vietnamese text
     * 
     * @param corruptedText Text with potential corruption
     * @return Fixed text or original if no fix possible
     */
    public String fixCorruptedVietnameseText(String corruptedText) {
        if (corruptedText == null || !CORRUPTION_PATTERN.matcher(corruptedText).find()) {
            return corruptedText;
        }

        log.info("🔧 Attempting to fix corrupted Vietnamese text");

        String fixedText = corruptedText;

        // Apply common Vietnamese text fixes
        fixedText = fixedText
            .replace("?ng", "ứng")
            .replace("?c", "ức")
            .replace("?i", "ới")
            .replace("?n", "ần")
            .replace("?t", "ất")
            .replace("?m", "ầm")
            .replace("?p", "ập")
            .replace("?nh", "ành")
            .replace("?ch", "ách")
            .replace("?y", "ấy")
            .replace("?u", "ầu")
            .replace("h?", "hệ")
            .replace("th?", "thể")
            .replace("qu?", "quả")
            .replace("tr?", "trước")
            .replace("gi?", "giữa")
            .replace("l?", "lớp")
            .replace("h?c", "học")
            .replace("vi?n", "viên")
            .replace("thi?u", "thiếu")
            // Common phrases
            .replace("L?m r?t t?t", "Làm rất tốt")
            .replace("C?n c?i thi?n", "Cần cải thiện")
            .replace("B?i l?m t?t", "Bài làm tốt")
            .replace("K?t qu? t?t", "Kết quả tốt")
            .replace("Thi?u chi ti?t", "Thiếu chi tiết")
            .replace("C?n b? sung", "Cần bổ sung")
            .replace("R?t t?t", "Rất tốt")
            .replace("T?t l?m", "Tốt lắm")
            .replace("Xu?t s?c", "Xuất sắc")
            .replace("H?y ti?p t?c", "Hãy tiếp tục")
            .replace("C? g?ng h?n", "Cố gắng hơn")
            .replace("L?m t?t h?n", "Làm tốt hơn");

        if (!fixedText.equals(corruptedText)) {
            log.info("✅ Vietnamese text fix applied successfully");
            log.debug("Original: {}", corruptedText);
            log.debug("Fixed: {}", fixedText);
        } else {
            log.warn("⚠️ Could not fix Vietnamese text corruption");
        }

        return fixedText;
    }

    /**
     * Validates and fixes Vietnamese text in one operation
     * 
     * @param text Text to validate and fix
     * @return ProcessingResult with validation and fix results
     */
    public ProcessingResult processVietnameseText(String text) {
        ValidationResult validation = validateVietnameseText(text);
        String processedText = text;

        if (!validation.isValid() && validation.isCorruptionDetected()) {
            processedText = fixCorruptedVietnameseText(text);
            
            // Re-validate after fix
            ValidationResult postFixValidation = validateVietnameseText(processedText);
            
            return new ProcessingResult(
                validation,
                processedText,
                postFixValidation,
                !processedText.equals(text)
            );
        }

        return new ProcessingResult(validation, processedText, validation, false);
    }

    /**
     * Checks if text is likely Vietnamese based on character patterns
     * 
     * @param text Text to check
     * @return true if text appears to be Vietnamese
     */
    public boolean isVietnameseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // Check for Vietnamese characters
        if (VIETNAMESE_PATTERN.matcher(text).find()) {
            return true;
        }

        // Check for common Vietnamese words
        String lowerText = text.toLowerCase();
        for (String word : COMMON_VIETNAMESE_WORDS) {
            if (lowerText.contains(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Result class for Vietnamese text validation
     */
    public static class ValidationResult {
        private boolean valid = true;
        private String issue;
        private String originalText;
        private boolean corruptionDetected = false;
        private boolean encodingIssue = false;
        private boolean containsVietnameseCharacters = false;
        private int corruptedCharacterCount = 0;

        public ValidationResult() {}

        public ValidationResult(boolean valid, String issue, String originalText) {
            this.valid = valid;
            this.issue = issue;
            this.originalText = originalText;
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getIssue() { return issue; }
        public void setIssue(String issue) { this.issue = issue; }
        
        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }
        
        public boolean isCorruptionDetected() { return corruptionDetected; }
        public void setCorruptionDetected(boolean corruptionDetected) { this.corruptionDetected = corruptionDetected; }
        
        public boolean isEncodingIssue() { return encodingIssue; }
        public void setEncodingIssue(boolean encodingIssue) { this.encodingIssue = encodingIssue; }
        
        public boolean isContainsVietnameseCharacters() { return containsVietnameseCharacters; }
        public void setContainsVietnameseCharacters(boolean containsVietnameseCharacters) { 
            this.containsVietnameseCharacters = containsVietnameseCharacters; 
        }
        
        public int getCorruptedCharacterCount() { return corruptedCharacterCount; }
        public void setCorruptedCharacterCount(int corruptedCharacterCount) { 
            this.corruptedCharacterCount = corruptedCharacterCount; 
        }
    }

    /**
     * Result class for Vietnamese text processing
     */
    public static class ProcessingResult {
        private final ValidationResult originalValidation;
        private final String processedText;
        private final ValidationResult finalValidation;
        private final boolean wasFixed;

        public ProcessingResult(ValidationResult originalValidation, String processedText, 
                              ValidationResult finalValidation, boolean wasFixed) {
            this.originalValidation = originalValidation;
            this.processedText = processedText;
            this.finalValidation = finalValidation;
            this.wasFixed = wasFixed;
        }

        public ValidationResult getOriginalValidation() { return originalValidation; }
        public String getProcessedText() { return processedText; }
        public ValidationResult getFinalValidation() { return finalValidation; }
        public boolean wasFixed() { return wasFixed; }
    }
}
